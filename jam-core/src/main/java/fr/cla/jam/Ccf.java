package fr.cla.jam;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.sync.PoolSingleResultSyncCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class Ccf<E, Es extends Collection<E>> {

    private static final CollectionCfAdapter collectionResultAdapter = new CollectionCfAdapter();
    protected static final CsCfAdapter csCfAdapter = new CsCfAdapter();
    protected static final CallbackCfAdapter callbackCfAdapter = new CallbackCfAdapter();
    protected static final PromiseCfAdapter promiseCfAdapter = new PromiseCfAdapter();

    private final CompletableFuture<Es> underlyingCf;
    public CompletableFuture<Es> asCf() { return underlyingCf; }

    //CompletableFuture functionnality
    public Es join() { return underlyingCf.join(); }

    //Monad Constructors
    protected Ccf(CompletableFuture<Es> underlyingCf) { this.underlyingCf = underlyingCf; }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ccfOfSync(
        I input,
        Function<I, Es> syncFunction,
        Executor pool
    ) {
        return new Ccf<>(new PoolSingleResultSyncCfAdapter(pool).adapt(syncFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ccfOfCs(
        I input,
        Function<I, CompletionStage<Es>> csFunction
    ) {
        return new Ccf<>(csCfAdapter.adapt(csFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ccfOfCallback(
        I input,
        BiConsumer<I, Callback<Es>> callbackFunction
    ) {
        return new Ccf<>(callbackCfAdapter.adapt(callbackFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> Ccf<E, Es> ccfOfPromise(
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
        return new Ccf<>(doFlatMap(mapper, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMap(
        Function<E, ? extends Ccf<F, Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) { 
        return underlyingCf.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, i -> mapper.apply(i).asCf(), collectionSupplier, collectionUnion
            )
        );
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapCf(
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new Ccf<>(doFlatMapCf(mapper, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapCf(
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return underlyingCf.thenCompose(
            inputs -> collectionResultAdapter.flatMapAdapt(
                inputs, mapper, collectionSupplier, collectionUnion
            )
        );
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

        return new Ccf<>(doFlatMapSync(mapper, adapter, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapSync(
        Function<E, Fs> mapper,
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return doFlatMapCf(adapter.apply(mapper), collectionSupplier, collectionUnion);
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapCs(
        Function<E, CompletionStage<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new Ccf<>(doFlatMapCs(mapper, csCfAdapter::adapt, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapCs(
        Function<E, CompletionStage<Fs>> mapper,
        Function<
            Function<E, CompletionStage<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return underlyingCf.thenCompose(
            inputs -> csCfAdapter.flatMapAdapt(
                inputs, mapper, adapter, collectionSupplier, collectionUnion
            )
        );
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapCallback(
        BiConsumer<E, Callback<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new Ccf<>(doFlatMapCallback(mapper, callbackCfAdapter::adapt, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapCallback(
        BiConsumer<E, Callback<Fs>> mapper,
        Function<
            BiConsumer<E, Callback<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return underlyingCf.thenCompose(
            inputs -> callbackCfAdapter.flatMapAdapt(
                inputs, mapper, adapter, collectionSupplier, collectionUnion
            )
        );
    }

    public <F, Fs extends Collection<F>> Ccf<F, Fs> flatMapPromise(
        Function<E, Promise<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new Ccf<>(doFlatMapPromise(mapper, promiseCfAdapter::adapt, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapPromise(
        Function<E, Promise<Fs>> mapper,
        Function<
            Function<E, Promise<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return underlyingCf.thenCompose(
            inputs -> promiseCfAdapter.flatMapAdapt(
                inputs, mapper, adapter, collectionSupplier, collectionUnion
            )
        );
    }

}
