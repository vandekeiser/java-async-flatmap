package fr.cla.jam.apitypes.sync;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This impl is not satisfying at all since the call is sync and doesn't use the event loop.
 * Apparently node/vertx can only adapt a given set of calls (ws requests, ..)
 */
public class VertxSyncApi2CfApi {

    private static final Vertx vertx =  Vertx.vertx();
    
    public static <T> Function<Supplier<T>, CompletableFuture<T>> supplyVertx() {
        return s -> {
            CompletableFuture<T> cf = new CompletableFuture<>();
            vertx.executeBlocking(
                f -> f.complete(s.get()), 
                (AsyncResult<T> r) -> {
                    if(r.succeeded()) cf.complete(r.result());
                    if(r.failed()) cf.completeExceptionally(r.cause());
                }
            );
            return cf;    
        };
    }

}
