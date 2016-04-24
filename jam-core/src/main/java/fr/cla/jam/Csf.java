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
    private Csf(CompletableFuture<Set<E>> cf) { this.cf = cf; }
    private CompletableFuture<Set<E>> getCf() { return cf; }
    private Csf(Set<E> r) { this.cf = completedFuture(r); }
    private Csf(Throwable x) { this.cf = new CompletableFuture<>(); cf.completeExceptionally(x); }
    public static <T> Csf<T> of(CompletableFuture<Set<T>> cf) { return new Csf<>(cf);}
    public static <T> Csf<T> succeeded(Set<T> r) { return new Csf<>(r);}
    public static <T> Csf<T> failed(Throwable x) { return new Csf<>(x);}

    public Set<E> join() { return this.cf.join(); }

    public <F> Csf<F> flatMap(
        Function<E, Csf<F>> mapper
    ) {
        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> collectionResultAdapter.flatMapAdapt(
            inputs, mapper.andThen(Csf::getCf), Collections::emptySet, Sets::union
        ));
        return new Csf<>(xxx);
    }

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
        Function<E, Promise<Set<F>>> mapper,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        CompletableFuture<Set<F>> xxx = cf.thenCompose(inputs -> apiTypeAgnosticAdapter.flatMapAdapt(
            inputs, adapter.apply(mapper))
        );
        return new Csf<>(xxx);
    }
    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper
    ) {
        return flatMapPromise(mapper, promiseCfAdapter::adapt);
    }

}
