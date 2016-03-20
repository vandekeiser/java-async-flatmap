package fr.cla.jam.nonblocking.callback;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CallbackApi2CfApi {

    public static <T, U> Function<
        BiConsumer<T, Callback<U>>,
        Function<T, CompletableFuture<U>>
    > waitToBeCalledBack() {
        return callback -> input -> cfThatWaitsToBeCalledBack(callback, input);
    }

    private static <T, U> CompletableFuture<U> cfThatWaitsToBeCalledBack(
        BiConsumer<T, Callback<U>> call,
        T input
    ) {
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        call.accept(input, new Callback<U>() {
            @Override public void onSuccess(U success) {
                //System.out.println("startWaitingForCallbackInFiber 2");
                fiberCf.complete(success);
            }
            @Override public void onFailure(Throwable failure) {
                //System.out.println("startWaitingForCallbackInFiber 3");
                fiberCf.completeExceptionally(failure);
            }
        });

        return fiberCf;
    }

}
