package fr.cla.jam;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.apitypes.SetCfAdapter;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class Csf<E>{

    private static final CollectionCfAdapter collectionResultAdapter = new CollectionCfAdapter();
    private static final SetCfAdapter apiTypeAgnosticAdapter = new SetCfAdapter();
    private static final CallbackCfAdapter callbackCfAdapter = new CallbackCfAdapter();
    private static final PromiseCfAdapter promiseCfAdapter = new PromiseCfAdapter();

    private final CompletableFuture<Set<E>> cf;
    public Csf(CompletableFuture<Set<E>> cf) { this.cf = cf; }
    public Csf(Set<E> r) { this.cf = completedFuture(r); }
    public Csf(Throwable x) { this.cf = new CompletableFuture<>(); cf.completeExceptionally(x); }
    public Set<E> join() { return this.cf.join(); }

//    public <F> Csf<F> flatMap(
//        Function<E, Csf<F>> mapper
//    ) {
//        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> collectionResultAdapter.flatMapAdapt(
//            inputs, mapper, Collections::emptySet, Sets::union
//        ));
//        return new Csf<>(xxx);
//    }

    public <F> Csf<F> flatMapCf(
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> collectionResultAdapter.flatMapAdapt(
            inputs, mapper, Collections::emptySet, Sets::union
        ));
        return new Csf<>(xxx);
    }

    public <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> apiTypeAgnosticAdapter.flatMapAdapt(
            inputs, adapter.apply(mapper)
        ));
        return new Csf<>(xxx);
    }
    public <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper
    ) {
        return flatMapCallback(mapper, callbackCfAdapter::adapt);
    }

    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> adaptee,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> apiTypeAgnosticAdapter.flatMapAdapt(
            inputs, adapter.apply(adaptee))
        );
        return new Csf<>(xxx);
    }
    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper
    ) {
        return flatMapPromise(mapper, promiseCfAdapter::adapt);
    }

}
