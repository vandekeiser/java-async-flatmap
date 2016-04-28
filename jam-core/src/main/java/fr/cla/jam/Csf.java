package fr.cla.jam;

import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.sync.PoolSingleResultSyncCfAdapter;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.checkedSet;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toSet;

public class Csf<E> extends Ccf<E, Set<E>>{

    //Monad Constructors
    public Csf(CompletableFuture<Set<E>> underlyingCf) { super(underlyingCf); }
    
    @SuppressWarnings("unchecked") //We use a checked collection so no pb
    public static <E> Csf<E> of(Class<E> type, E... values) {
        Set<E> _values = checkedSet(new HashSet<E>(), type);
        Stream.of(values).forEach(v -> _values.add(v));
        return of(_values);
    }
    public static <E> Csf<E> of(Set<E> success) {
        return new Csf<>(completedFuture(success));
    }
    public static <E> Csf<E> of(Throwable failure) {
        CompletableFuture<Set<E>> underlyingFailure = new CompletableFuture<>();
        underlyingFailure.completeExceptionally(failure);
        return new Csf<>(underlyingFailure);
    }

    public static <I, E> Csf<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        Executor pool
    ) {
        return new Csf<>(new PoolSingleResultSyncCfAdapter(pool).adapt(syncFunction).apply(input));
    }

    public static <I, E> Csf<E> ofCs(
        I input,
        Function<I, CompletionStage<Set<E>>> csFunction
    ) {
        return new Csf<>(csCfAdapter.adapt(csFunction).apply(input));
    }

    public static <I, E> Csf<E> ofCallback(
        I input,
        BiConsumer<I, Callback<Set<E>>> callbackFunction
    ) {
        return new Csf<>(callbackCfAdapter.adapt(callbackFunction).apply(input));
    }

    public static <I, E> Csf<E> ofPromise(
        I input,
        Function<I, Promise<Set<E>>> promiseFunction
    ) {
        return new Csf<>(promiseCfAdapter.adapt(promiseFunction).apply(input));
    }

    //Monad flatmaps
    public <F> Csf<F> flatMap(
        Function<E, Csf<F>> mapper
    ) {
        return new Csf<>(doFlatMap(mapper));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMap(
        Function<E, ? extends Ccf<F, Set<F>>> mapper
    ) {
        return doFlatMap(mapper, Collections::emptySet, Sets::union);
    }

    public <F> Csf<F> flatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        return new Csf<>(doFlatMapCf(mapper));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        return doFlatMapCf(mapper, Collections::emptySet, Sets::union);
    }

    public <F> Csf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Executor pool
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new PoolSingleResultSyncCfAdapter(pool)::adapt;

        return new Csf<>(doFlatMapSync(mapper, adapter));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapSync(
        Function<E, Set<F>> mapper,
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return doFlatMapSync(mapper, adapter, Collections::emptySet, Sets::union);
    }

    public <F> Csf<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper
    ) {
        return new Csf<>(doFlatMapCs(mapper, csCfAdapter::adapt));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper,
        Function<
            Function<E, CompletionStage<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return doFlatMapCs(mapper, adapter, Collections::emptySet, Sets::union);
    }

    public <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper
    ) {
        return new Csf<>(doFlatMapCallback(mapper, callbackCfAdapter::adapt));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return doFlatMapCallback(mapper, adapter, Collections::emptySet, Sets::union);
    }

    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper
    ) {
        return new Csf<>(doFlatMapPromise(mapper, promiseCfAdapter::adapt, Collections::emptySet, Sets::union));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapPromise(
        Function<E, Promise<Set<F>>> mapper,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return doFlatMapPromise(mapper, adapter, Collections::emptySet, Sets::union);
    }

    public Csf<E> filter(Predicate<? super E> criterion) {
        return new Csf<>(asCf().thenApply(contents -> 
            contents.stream().filter(criterion).collect(toSet()) 
        ));
    }
    
    public <F> Csf<F> map(Function<? super E, ? extends F> mapper) {
        return new Csf<>(asCf().thenApply(contents -> 
            contents.stream().map(mapper).collect(toSet()) 
        ));
    }

    public Csf<E> concat(Csf<? extends E> that) {
        return new Csf<>(asCf().thenCombine(that.asCf(), Sets::union));
    }
    
}
