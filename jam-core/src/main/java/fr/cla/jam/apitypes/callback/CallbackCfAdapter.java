package fr.cla.jam.apitypes.callback;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CallbackCfAdapter {

    public <T, U> Function<T, CompletableFuture<U>> toCompletableFuture(
        BiConsumer<T, Callback<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.accept(input, Callback.either(
                success -> cf.complete(success),
                failure -> cf.completeExceptionally(failure)
            ));
            return cf;
        };
    }

}
