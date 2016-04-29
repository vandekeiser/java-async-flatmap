package fr.cla.jam;

import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.util.containers.Sets;

import java.util.Arrays;
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

public class CfOfSet<E> extends CfOfCollection<E, Set<E>> {

    //Monad Constructors
    public CfOfSet(CompletableFuture<Set<E>> underlyingCf) { super(underlyingCf); }
    
    @SuppressWarnings("unchecked") //We use a checked collection so no problem
    public static <E> CfOfSet<E> of(Class<E> type, E... values) {
        Set<E> _values = checkedSet(new HashSet<>(), type);
        _values.addAll(Arrays.asList(values));
        return of(_values);
    }
    public static <E> CfOfSet<E> of(Set<E> success) {
        return new CfOfSet<>(completedFuture(success));
    }
    public static <E> CfOfSet<E> of(Throwable failure) {
        CompletableFuture<Set<E>> underlyingFailure = new CompletableFuture<>();
        underlyingFailure.completeExceptionally(failure);
        return new CfOfSet<>(underlyingFailure);
    }

    public static <I, E> CfOfSet<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        Executor pool
    ) {
        return new CfOfSet<>(new SyncCfAdapter(pool).toCompletableFuture(syncFunction).apply(input));
    }

    public static <I, E> CfOfSet<E> ofCs(
        I input,
        Function<I, CompletionStage<Set<E>>> csFunction
    ) {
        return new CfOfSet<>(fromCompletionStage.toCompletableFuture(csFunction).apply(input));
    }

    public static <I, E> CfOfSet<E> ofCallback(
        I input,
        BiConsumer<I, Callback<Set<E>>> callbackFunction
    ) {
        return new CfOfSet<>(fromCallback.toCompletableFuture(callbackFunction).apply(input));
    }

    public static <I, E> CfOfSet<E> ofPromise(
        I input,
        Function<I, Promise<Set<E>>> promiseFunction
    ) {
        return new CfOfSet<>(fromPromise.toCompletableFuture(promiseFunction).apply(input));
    }

    //Monad flatmaps
    public <F> CfOfSet<F> flatMap(
        Function<E, CfOfSet<F>> mapper
    ) {
        return new CfOfSet<>(doFlatMap(mapper));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMap(
        Function<E, ? extends CfOfCollection<F, Set<F>>> mapper
    ) {
        return doFlatMap(mapper, Collections::emptySet, Sets::union);
    }

    public <F> CfOfSet<F> flatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        return new CfOfSet<>(doFlatMapCf(mapper));
    }
    protected final <F> CompletableFuture<Set<F>> doFlatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        return doFlatMapCf(mapper, Collections::emptySet, Sets::union);
    }

    public <F> CfOfSet<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Executor pool
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new SyncCfAdapter(pool)::toCompletableFuture;

        return new CfOfSet<>(doFlatMapSync(mapper, adapter));
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

    public <F> CfOfSet<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper
    ) {
        return new CfOfSet<>(doFlatMapCs(mapper, fromCompletionStage::toCompletableFuture));
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

    public <F> CfOfSet<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper
    ) {
        return new CfOfSet<>(doFlatMapCallback(mapper, fromCallback::toCompletableFuture));
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

    public <F> CfOfSet<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper
    ) {
        return new CfOfSet<>(doFlatMapPromise(mapper, fromPromise::toCompletableFuture, Collections::emptySet, Sets::union));
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

    public CfOfSet<E> filter(Predicate<? super E> criterion) {
        return new CfOfSet<>(asCompletableFuture().thenApply(contents -> 
            contents.stream().filter(criterion).collect(toSet()) 
        ));
    }
    
    public <F> CfOfSet<F> map(Function<? super E, ? extends F> mapper) {
        return new CfOfSet<>(asCompletableFuture().thenApply(contents -> 
            contents.stream().map(mapper).collect(toSet()) 
        ));
    }

    public CfOfSet<E> concat(CfOfSet<? extends E> that) {
        return new CfOfSet<>(asCompletableFuture().thenCombine(that.asCompletableFuture(), Sets::union));
    }
    
}
