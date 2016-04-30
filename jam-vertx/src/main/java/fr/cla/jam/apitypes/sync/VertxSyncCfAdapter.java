package fr.cla.jam.apitypes.sync;

import io.vertx.core.Vertx;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * This impl is not satisfying at all since the call is sync and doesn't use the event loop.
 * Apparently node/vertx can only adapt a given set of calls (ws requests, ..)
 */
public class VertxSyncCfAdapter {

    private final Vertx vertx;

    public VertxSyncCfAdapter(Vertx vertx) {
        this.vertx = vertx;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            vertx.executeBlocking(
                (io.vertx.core.Future<U> vertxFuture) -> {
                    try {
                        U success = adaptee.apply(input);
                        vertxFuture.complete(success);
                    } catch (Throwable failure) {
                        vertxFuture.fail(failure);
                    }
                },
                false,
                (io.vertx.core.AsyncResult<U> r) -> {
                    if(r.succeeded()) cf.complete(r.result());
                    if(r.failed()) cf.completeExceptionally(r.cause());
                }
            );
            return cf;
        };
    }

}
