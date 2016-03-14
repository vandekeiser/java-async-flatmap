package cla.completablefuture.jenkins.nonblocking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

public class Quasarify {

    public static <T, U> Function<
        Function<T, CompletionStage<U>>,
        Function<T, CompletableFuture<U>>
    > usingPool(Executor dedicatedPool) {
        return blocking -> input -> callInFiber(blocking, input, dedicatedPool);
    }

    private static <T, U> CompletableFuture<U> callInFiber(
        Function<T, CompletionStage<U>> blocking,
        T input,
        Executor dedicatedPool
    ) {
        FiberScheduler scheduler = new FiberExecutorScheduler("callInFiber scheduler", dedicatedPool);
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(scheduler, () -> {
            try {
                blocking.apply(input).whenComplete((t, x) -> {
                    if (x != null) fiberCf.completeExceptionally(x);
                    else fiberCf.complete(t);
                });    
            } catch (Throwable x) {
                fiberCf.completeExceptionally(x);
            } 
        }).start();

        return fiberCf;
    }

}
