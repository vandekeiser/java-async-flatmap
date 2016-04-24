package fr.cla.jam;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.apitypes.SetCfAdapter;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.sync.PoolSingleResultSyncCfAdapter;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class Csf<E>{

    private static final CollectionCfAdapter collectionResultAdapter = new CollectionCfAdapter();
    private static final SetCfAdapter apiTypeAgnosticAdapter = new SetCfAdapter();
    private static final CsCfAdapter csCfAdapter = new CsCfAdapter();
    private static final CallbackCfAdapter callbackCfAdapter = new CallbackCfAdapter();
    private static final PromiseCfAdapter promiseCfAdapter = new PromiseCfAdapter();

    //The wrapped CF
    private final CompletableFuture<Set<E>> wrapped;
    private CompletableFuture<Set<E>> getWrapped() { return wrapped; }

    //CompletableFuture functionnality
    public Set<E> join() { return this.wrapped.join(); }

    //Monad Constructors
    protected Csf(CompletableFuture<Set<E>> wrapped) { this.wrapped = wrapped; }
    protected static <E> CompletableFuture<Set<E>> exceptionnallyCompletedFuture(Throwable x) {
        CompletableFuture<Set<E>> cf = new CompletableFuture<>();
        cf.completeExceptionally(x);
        return cf;
    }

    public static <I, E> Csf<E> ofSync(
        I input,
        Function<I, Set<E>> adaptee,
        Executor pool
    ) {
        return new Csf<>(new PoolSingleResultSyncCfAdapter(pool).adapt(adaptee).apply(input));
    }

    public static <I, E> Csf<E> ofCs(
        I input,
        Function<I, CompletionStage<Set<E>>> adaptee
    ) {
        return new Csf<>(csCfAdapter.adapt(adaptee).apply(input));
    }

    public static <I, E> Csf<E> ofCallback(
        I input,
        BiConsumer<I, Callback<Set<E>>> adaptee
    ) {
        return new Csf<>(callbackCfAdapter.adapt(adaptee).apply(input));
    }

    public static <I, E> Csf<E> ofPromise(
        I input,
        Function<I, Promise<Set<E>>> adaptee
    ) {
        return new Csf<>(promiseCfAdapter.adapt(adaptee).apply(input));
    }

    //Monad flatmaps
    public <F> Csf<F> flatMap(
        Function<E, Csf<F>> mapper
    ) {
        CompletableFuture<Set<F>> newWrapped = wrapped.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, mapper.andThen(Csf::getWrapped), Collections::emptySet, Sets::union
            )
        );
        return new Csf<>(newWrapped);
    }

    public <F> Csf<F> flatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        CompletableFuture<Set<F>> newWrapped = wrapped.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, mapper, Collections::emptySet, Sets::union
            )
        );
        return new Csf<>(newWrapped);
    }

    public <F> Csf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Executor pool
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new PoolSingleResultSyncCfAdapter(pool)::adapt;

        return flatMapSync(mapper, adapter);
    }
    protected final <F> Csf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return flatMapCf(adapter.apply(mapper));
    }

    public <F> Csf<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper
    ) {
        return flatMapCs(mapper, csCfAdapter::adapt);
    }

    protected final <F> Csf<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper,
        Function<
            Function<E, CompletionStage<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> newWrapped = wrapped.thenCompose(
            inputs -> csCfAdapter.flatMapAdapt(
                inputs, mapper, adapter
            )
        );
        return new Csf<>(newWrapped);
    }

    public <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper
    ) {
        return flatMapCallback(mapper, callbackCfAdapter::adapt);
    }
    protected final <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> newWrapped = wrapped.thenCompose(
            inputs -> apiTypeAgnosticAdapter.flatMapAdapt(
                inputs, adapter.apply(mapper)
            )
        );
        return new Csf<>(newWrapped);
    }

    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper
    ) {
        return flatMapPromise(mapper, promiseCfAdapter::adapt);
    }
    protected final <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> newWrapped = wrapped.thenCompose(
            inputs -> apiTypeAgnosticAdapter.flatMapAdapt(
                inputs, adapter.apply(mapper)
            )
        );
        return new Csf<>(newWrapped);
    }

}
