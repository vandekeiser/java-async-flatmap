package fr.cla.jam.apitypes;

import fr.cla.jam.Csf;
import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.util.containers.Sets;
import io.vertx.core.Vertx;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class VertxCsf<E> extends Csf<E> {

    //Monad Constructors
    private VertxCsf(CompletableFuture<Set<E>> wrapped) { super(wrapped); }

    public static <I, E> VertxCsf<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        Vertx quasarScheduler
    ) {
        return new VertxCsf<>(new VertxSyncCfAdapter(quasarScheduler).adapt(syncFunction).apply(input));
    }

    //Monad flatmaps
    public <F> Csf<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Vertx quasarScheduler
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new VertxSyncCfAdapter(quasarScheduler)::adapt;

        return new VertxCsf<>(doFlatMapSync(mapper, adapter));
    }

}
