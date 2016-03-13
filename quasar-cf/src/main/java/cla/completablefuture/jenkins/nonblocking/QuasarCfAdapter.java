package cla.completablefuture.jenkins.nonblocking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;

public class QuasarCfAdapter {

    public static <T, U> UnaryOperator<Function<T, CompletionStage<U>>> supplyQuasar() {
        return blocking -> t -> {
            //return callAsyncFiber(blocking, t);
            return callFiber(blocking, t);
        };
    }

    private static <T, U> CompletionStage<U> callFiber(Function<T, CompletionStage<U>> blocking, T t) {

        AtomicReference<CompletionStage<U>> xxx = new AtomicReference<>();

        final Fiber fiber = new Fiber(new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                CompletionStage<U> res = callAsyncFiber(blocking, t);
                xxx.set(res);
            }
        }).start();

        try {
            fiber.join();
        } catch (InterruptedException | ExecutionException e) {
            CompletableFuture<U> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }

        return xxx.get();
    }

    private static <T, U> CompletionStage<U> callAsyncFiber(Function<T, CompletionStage<U>> blocking, T t) {
        FiberAsync<CompletionStage<U>, CompletionException>
            fiber = new CfFiberAsync<>(blocking, t);

        try {
            fiber.run();
        } catch (InterruptedException | SuspendExecution e) {
            CompletableFuture<U> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }

        return fiber.getResult();
    }

}
