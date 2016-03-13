package cla.completablefuture.jenkins.nonblocking;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import co.paralleluniverse.fibers.*;
import co.paralleluniverse.strands.SuspendableRunnable;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class QuasarCfAdapter {

    public static <T, U> UnaryOperator<Function<T, CompletionStage<U>>> supplyQuasar(Executor dedicatedPool) {
        return blocking -> t -> {
            //return callAsyncFiber(blocking, t);
            return callFiber(blocking, t, dedicatedPool);
        };
    }

    public static <T, U> Function<
                            Function<T, CompletionStage<U>>,
                            Function<T, CompletableFuture<U>>
                    > supplyQuasar2(Executor dedicatedPool) {
            return blocking -> t -> {
                //return callAsyncFiber(blocking, t);
                return callFiber(blocking, t, dedicatedPool);
            };
        }

    private static <T, U> CompletableFuture<U> callFiber(Function<T, CompletionStage<U>> blocking, T input, Executor dedicatedPool) {
        FiberScheduler scheduler = new FiberExecutorScheduler("quasar", dedicatedPool);

        CompletableFuture<U> ret = new CompletableFuture<>();

        final Fiber<?> fiber = new Fiber<>(scheduler, new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                blocking.apply(input).whenComplete((t, x) -> {
                    if (x != null) ret.completeExceptionally(x);
                    else ret.complete(t);
                });
            }
        }).start();

        return ret;
    }

    private static <T, U> CompletionStage<U> callAsyncFiber(Function<T, CompletionStage<U>> blocking, T t) {
        FiberAsync<CompletionStage<U>, CompletionException>
            fiber = new CfFiberAsync<>(blocking, t);

//        try {
//            fiber.run();//**
//        } catch (InterruptedException | SuspendExecution e) {
//            CompletableFuture<U> failed = new CompletableFuture<>();
//            failed.completeExceptionally(e);
//            return failed;
//        }

        return fiber.getResult();
    }

}
