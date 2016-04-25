package fr.cla.jam.apitypes;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.Csf;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.apitypes.callback.QuasarCallbackCfAdapter;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarCsf<E> extends Csf<E> {

    //Monad Constructors
    private QuasarCsf(CompletableFuture<Set<E>> underlyingCf) { super(underlyingCf); }

    public static <I, E> QuasarCsf<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(
            new QuasarSyncCfAdapter(quasarScheduler).adapt(syncFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofCs(
        I input,
        Function<I, CompletionStage<Set<E>>> csFunction,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(
            new QuasarCsCfAdapter(quasarScheduler).adapt(csFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofCallback(
        I input,
        BiConsumer<I, Callback<Set<E>>> callbackFunction,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(
            new QuasarCallbackCfAdapter(quasarScheduler).adapt(callbackFunction).apply(input)
        );
    }

    public static <I, E> QuasarCsf<E> ofPromise(
        I input,
        Function<I, Promise<Set<E>>> promiseFunction,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(
            new QuasarPromiseCfAdapter(quasarScheduler).adapt(promiseFunction).apply(input)
        );
    }

    //Monad flatmaps
    public <F> Csf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        FiberScheduler quasarScheduler
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new QuasarSyncCfAdapter(quasarScheduler)::adapt;

        return new QuasarCsf<>(doFlatMapSync(mapper, adapter));
    }

    public <F> Csf<F> flatMapCs(
        Function<E, CompletionStage<Set<F>>> mapper,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(doFlatMapCs(
            mapper, 
            new QuasarCsCfAdapter(quasarScheduler)::adapt
        ));
    }

    public <F> Csf<F> flatMapCallback(
        BiConsumer<E, Callback<Set<F>>> mapper,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(doFlatMapCallback(
            mapper, 
            new QuasarCallbackCfAdapter(quasarScheduler)::adapt
        ));
    }

    public <F> Csf<F> flatMapPromise(
        Function<E, Promise<Set<F>>> mapper,
        FiberScheduler quasarScheduler
    ) {
        return new QuasarCsf<>(doFlatMapPromise(
            mapper, 
            new QuasarPromiseCfAdapter(quasarScheduler)::adapt
        ));
    }

}
