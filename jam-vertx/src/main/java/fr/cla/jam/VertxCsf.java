package fr.cla.jam;

import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import io.vertx.core.Vertx;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class VertxCsf<E> extends CfOfSet<E> {

    //Monad Constructors
    private VertxCsf(CompletableFuture<Set<E>> wrapped) { super(wrapped); }

    public static <I, E> VertxCsf<E> ofSync(
        I input,
        Function<I, Set<E>> syncFunction,
        Vertx vertx
    ) {
        return new VertxCsf<>(new VertxSyncCfAdapter(vertx).adapt(syncFunction).apply(input));
    }

    //Monad flatmaps
    public <F> CfOfSet<F> flatMapSync(
        Function<E, Set<F>> mapper,
        Vertx vertx
    ) {
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter = new VertxSyncCfAdapter(vertx)::adapt;

        return new VertxCsf<>(doFlatMapSync(mapper, adapter));
    }

}
