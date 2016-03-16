package cla.completablefuture.jenkins.nonblocking.callback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import cla.completablefuture.jira.nonblocking.callback.Callback;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

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
        FiberScheduler dedicatedScheduler = new FiberExecutorScheduler("callInFiber scheduler", dedicatedPool);
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(dedicatedScheduler, () -> callback.apply(input).whenComplete(
            res -> fiberCf.complete(res),
            x -> fiberCf.completeExceptionally(x)
        )).start();

        return fiberCf;
    }

}
