package fr.cla.jam;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.apitypes.SetCfAdapter;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.sync.PoolSingleResultSyncCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;
import fr.cla.jam.util.containers.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Ccf<E, Es extends Collection<E>> {
    
    private static final CollectionCfAdapter collectionResultAdapter = new CollectionCfAdapter();
    private static final CsCfAdapter csCfAdapter = new CsCfAdapter();
    private static final CallbackCfAdapter callbackCfAdapter = new CallbackCfAdapter();
    private static final PromiseCfAdapter promiseCfAdapter = new PromiseCfAdapter();

    //The wrapped CF
    protected final CompletableFuture<Es> wrapped;
    public CompletableFuture<Es> asCf() { return wrapped; }

    //CompletableFuture functionnality
    public Es join() { return this.wrapped.join(); }

    //Monad Constructors
    protected Ccf(CompletableFuture<Es> wrapped) { this.wrapped = wrapped; }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ofSync(
        I input,
        Function<I, Es> syncFunction,
        Executor pool
    ) {
        return new Ccf<>(new PoolSingleResultSyncCfAdapter(pool).adapt(syncFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ofCs(
        I input,
        Function<I, CompletionStage<Es>> csFunction
    ) {
        return new Ccf<>(csCfAdapter.adapt(csFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ofCallback(
        I input,
        BiConsumer<I, Callback<Es>> callbackFunction
    ) {
        return new Ccf<>(callbackCfAdapter.adapt(callbackFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ofPromise(
        I input,
        Function<I, Promise<Es>> promiseFunction
    ) {
        return new Ccf<>(promiseCfAdapter.adapt(promiseFunction).apply(input));
    }

    //Monad flatmaps
    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMap(
        Function<E, Ccf<F, Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        CompletableFuture<Fs> newWrapped = wrapped.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, mapper.andThen(Ccf::asCf), collectionSupplier, collectionUnion
            )
        );
        return new Ccf<>(newWrapped);
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapCf(
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        CompletableFuture<Fs> newWrapped = wrapped.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, mapper, collectionSupplier, collectionUnion
            )
        );
        return new Ccf<>(newWrapped);
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapSync(
        Function<E, Fs> mapper,
        Executor pool,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > adapter = new PoolSingleResultSyncCfAdapter(pool)::adapt;

        return doFlatMapSync(mapper, adapter, collectionSupplier, collectionUnion);
    }
    protected final <F, Fs extends Collection<F>> Ccf<F, Fs> doFlatMapSync(
        Function<E, Fs> mapper,
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapCf(adapter.apply(mapper), collectionSupplier, collectionUnion);
    }

    //TODO: generaliser aux collections:
//    flatMapCs
//    flatMapCallback
//    flatMapPromise

}
