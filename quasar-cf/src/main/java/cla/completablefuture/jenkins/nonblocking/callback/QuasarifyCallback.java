package cla.completablefuture.jenkins.nonblocking.callback;

import cla.completablefuture.jira.nonblocking.callback.Callback;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class QuasarifyCallback {

    public static <T, U> Function<
        Function<T, Callback<U>>,
        Function<T, CompletableFuture<U>>
    > usingPool(Executor dedicatedPool) {
        return callback -> input -> startWaitingForCallbackInFiber(callback, input, dedicatedPool);
    }

    private static <T, U> CompletableFuture<U> startWaitingForCallbackInFiber(
        Function<T, Callback<U>> callback,
        T input,
        Executor dedicatedPool
    ) {
        FiberScheduler scheduler = new FiberExecutorScheduler("callInFiber scheduler", dedicatedPool);
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(scheduler, () -> {
            try {
                callback.apply(input).whenComplete(
                        res -> fiberCf.complete(res),
                        x -> fiberCf.completeExceptionally(x)
                );
            } catch (Throwable x) {
                fiberCf.completeExceptionally(x);
            } 
        }).start();

        return fiberCf;
    }

}
