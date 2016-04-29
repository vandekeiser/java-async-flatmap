package fr.cla.jam;

import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.*;

import static fr.cla.jam.util.collectors.FlatteningCollectionCollector.flattening;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

public class CfOfCollection<E, Es extends Collection<E>> {

    protected static final CsCfAdapter fromCompletionStage = new CsCfAdapter();
    protected static final CallbackCfAdapter fromCallback = new CallbackCfAdapter();
    protected static final PromiseCfAdapter fromPromise = new PromiseCfAdapter();

    private final CompletableFuture<Es> underlyingCf;
    public CompletableFuture<Es> asCompletableFuture() { return underlyingCf; }

    //CompletableFuture functionnality
    public Es join() { return underlyingCf.join(); }

    //Monad Constructors
    protected CfOfCollection(CompletableFuture<Es> underlyingCf) { this.underlyingCf = underlyingCf; }

    public static <I, E, Es extends Collection<E>> CfOfCollection<E, Es> ccfOfSync(
        I input,
        Function<I, Es> syncFunction,
        Executor pool
    ) {
        return new CfOfCollection<>(new SyncCfAdapter(pool).toCompletableFuture(syncFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> CfOfCollection<E, Es> ccfOfCs(
        I input,
        Function<I, CompletionStage<Es>> csFunction
    ) {
        return new CfOfCollection<>(fromCompletionStage.toCompletableFuture(csFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> CfOfCollection<E, Es> ccfOfCallback(
        I input,
        BiConsumer<I, Callback<Es>> callbackFunction
    ) {
        return new CfOfCollection<>(fromCallback.toCompletableFuture(callbackFunction).apply(input));
    }

    public static <I, E, Es extends Collection<E>> CfOfCollection<E, Es> ccfOfPromise(
        I input,
        Function<I, Promise<Es>> promiseFunction
    ) {
        return new CfOfCollection<>(fromPromise.toCompletableFuture(promiseFunction).apply(input));
    }

    //Monad flatmaps
    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMap(
        Function<E, CfOfCollection<F, Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new CfOfCollection<>(doFlatMap(mapper, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMap(
        Function<E, ? extends CfOfCollection<F, Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) { 
        return underlyingCf.thenCompose(inputs -> flatMap(
            inputs, i -> mapper.apply(i).asCompletableFuture(), collectionSupplier, collectionUnion
        ));
    }

    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMapCf(
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new CfOfCollection<>(doFlatMapCf(mapper, collectionSupplier, collectionUnion));
    }
    protected final <F, Fs extends Collection<F>> CompletableFuture<Fs> doFlatMapCf(
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return underlyingCf.thenCompose(inputs -> flatMap(
            inputs, mapper, collectionSupplier, collectionUnion
        ));
    }

    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMapSync(
        Function<E, Fs> mapper,
        Executor pool,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > adapter = new SyncCfAdapter(pool)::toCompletableFuture;

        return new CfOfCollection<>(doFlatMapSync(mapper, adapter, collectionSupplier, collectionUnion));
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

    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMapCs(
        Function<E, CompletionStage<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new CfOfCollection<>(doFlatMapCs(mapper, fromCompletionStage::toCompletableFuture, collectionSupplier, collectionUnion));
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
        return underlyingCf.thenCompose(inputs -> flatMap(
            inputs, adapter.apply(mapper), collectionSupplier, collectionUnion
        ));
    }

    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMapCallback(
        BiConsumer<E, Callback<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new CfOfCollection<>(doFlatMapCallback(mapper, fromCallback::toCompletableFuture, collectionSupplier, collectionUnion));
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
        return underlyingCf.thenCompose(inputs -> flatMap(
            inputs, adapter.apply(mapper), collectionSupplier, collectionUnion
        ));
    }

    public <F, Fs extends Collection<F>> CfOfCollection<F, Fs> flatMapPromise(
        Function<E, Promise<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new CfOfCollection<>(doFlatMapPromise(mapper, fromPromise::toCompletableFuture, collectionSupplier, collectionUnion));
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
        return underlyingCf.thenCompose(inputs -> flatMap(
            inputs, adapter.apply(mapper), collectionSupplier, collectionUnion
        ));
    }

    
    
    public CfOfCollection<E, Es> filter(
        Predicate<? super E> criterion, 
        CollectionSupplier<E, Es> collectionSupplier
    ) {
        return new CfOfCollection<>(asCompletableFuture().thenApply(contents ->
            contents.stream().filter(criterion).collect(toCollection(collectionSupplier))
        ));
    }

    private <E, Es extends Collection<E>, F, Fs extends Collection<F>>
    CompletableFuture<Fs> flatMap(
        Es inputs,
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(mapper)
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));
    }
    
}
