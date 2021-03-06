package fr.cla.jam.apitypes.promise;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PromiseCfAdapter {

    public <T, U> Function<T, CompletableFuture<U>> toCompletableFuture(
        Function<T, Promise<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.apply(input).then(
                success -> cf.complete(success),
                failure -> cf.completeExceptionally(failure)
            );
            return cf;
        };
    }

}
