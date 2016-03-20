package fr.cla.jam.apitypes.completionstage;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class QuasarCsApi2CfApi {

    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public static <T, U> Function<
        Function<T, CompletionStage<U>>,
        Function<T, CompletableFuture<U>>
    > usingPool(Executor dedicatedPool) {
        return mapper -> input -> callInFiber(mapper, input, dedicatedPool);
    }

    private static <T, U> CompletableFuture<U> callInFiber(
        Function<T, CompletionStage<U>> mapper,
        T input,
        Executor dedicatedPool
    ) {
        FiberScheduler scheduler = new FiberExecutorScheduler("QuasarCsApi2CfApi scheduler" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(scheduler, () -> {
            mapper.apply(input).whenComplete((res, x) -> {
                if (x != null) fiberCf.completeExceptionally(x);
                else fiberCf.complete(res);
            });
        }).start();

        return fiberCf;
    }

}
