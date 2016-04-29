package fr.cla.jam;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.QuasarCallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarCsf<E> extends CfOfSet<E> {

    //Monad Constructors
    private QuasarCsf(CompletableFuture<Set<E>> underlyingCf) { super(underlyingCf); }

    public static <I, E> QuasarCsf<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(
            new QuasarSyncCfAdapter(quasar).toCompletableFuture(syncFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofCs(
        I input,
        Function<I, CompletionStage<Set<E>>> csFunction,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(
            new QuasarCsCfAdapter(quasar).toCompletableFuture(csFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofCallback(
        I input,
        BiConsumer<I, Callback<Set<E>>> callbackFunction,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(
            new QuasarCallbackCfAdapter(quasar).toCompletableFuture(callbackFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofPromise(
        I input,
        Function<I, Promise<Set<E>>> promiseFunction,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(
            new QuasarPromiseCfAdapter(quasar).toCompletableFuture(promiseFunction).apply(input)
        );
    }

    //Monad flatmaps
    public <F> QuasarCsf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        FiberScheduler quasar
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new QuasarSyncCfAdapter(quasar)::toCompletableFuture;

        return new QuasarCsf<>(doFlatMapSync(mapper, adapter));
    }

    public <F> QuasarCsf<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(doFlatMapCs(
            mapper, 
            new QuasarCsCfAdapter(quasar)::toCompletableFuture
        ));
    }

    public <F> QuasarCsf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(doFlatMapCallback(
            mapper, 
            new QuasarCallbackCfAdapter(quasar)::toCompletableFuture
        ));
    }

    public <F> QuasarCsf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper,
        FiberScheduler quasar
    ) {
        return new QuasarCsf<>(doFlatMapPromise(
            mapper, 
            new QuasarPromiseCfAdapter(quasar)::toCompletableFuture
        ));
    }

}
