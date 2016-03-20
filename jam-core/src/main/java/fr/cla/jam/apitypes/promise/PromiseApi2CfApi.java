package fr.cla.jam.apitypes.promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class PromiseApi2CfApi {

    public static <T, U> Function<
        Function<T, Promise<U>>,
        Function<T, CompletableFuture<U>>
    > waitToBeCalledBack(Executor dedicatedPool) {
        return callback -> input -> waitToBeCalledBack(callback, input, dedicatedPool);
    }

    private static <T, U> CompletableFuture<U> waitToBeCalledBack(
        Function<T, Promise<U>> promise,
        T input,
        Executor dedicatedPool
    ) {
        CompletableFuture<U> cf = new CompletableFuture<>();

        dedicatedPool.execute(() -> {
            promise.apply(input).whenComplete(
                res -> cf.complete(res),
                x -> cf.completeExceptionally(x)
            );
        });

        return cf;
    }

}
