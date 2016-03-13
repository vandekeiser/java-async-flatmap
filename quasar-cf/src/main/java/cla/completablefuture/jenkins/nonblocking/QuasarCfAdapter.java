package cla.completablefuture.jenkins.nonblocking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

public class QuasarCfAdapter {

    public static <T, U> Function<
        Function<T, CompletionStage<U>>,
        Function<T, CompletableFuture<U>>
    > supplyQuasar2(Executor dedicatedPool) {
        return blocking -> t -> callInFiber(blocking, t, dedicatedPool);
    }

    private static <T, U> CompletableFuture<U> callInFiber(
        Function<T, CompletionStage<U>> blocking,
        T input,
        Executor dedicatedPool
    ) {
        FiberScheduler scheduler = new FiberExecutorScheduler("quasar", dedicatedPool);
        CompletableFuture<U> ret = new CompletableFuture<>();

        new Fiber<>(scheduler, () -> {
            blocking.apply(input).whenComplete((t, x) -> {
                if (x != null) ret.completeExceptionally(x);
                else ret.complete(t);
            });
        }).start();

        return ret;
    }

}
