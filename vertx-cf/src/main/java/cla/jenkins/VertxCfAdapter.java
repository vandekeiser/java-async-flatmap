package cla.jenkins;

import org.vertx.java.core.Vertx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class VertxCfAdapter {

    private static final Vertx vertx =  Vertx.vertx(options);
    
    public static <T> Function<Supplier<T>, CompletableFuture<T>> supplyVertx() {
        return s -> CompletableFuture.supplyAsync(vertxify(s));
    }

    private static <T> Supplier<T> vertxify(Supplier<T> s) {
        return null;
    }


}
