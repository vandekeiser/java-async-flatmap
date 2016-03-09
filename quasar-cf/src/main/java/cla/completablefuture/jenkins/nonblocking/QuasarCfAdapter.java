package cla.completablefuture.jenkins.nonblocking;

import cla.completablefuture.jenkins.blocking.CfFiberAsync;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.SuspendExecution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class QuasarCfAdapter {

    public static <T> Function<Supplier<T>, CompletableFuture<T>> supplyQuasar() {
        return s -> CompletableFuture.supplyAsync(quasarify(s));
    }
    
    private static <T> Supplier<T> quasarify(Supplier<T> task) {
        Fiber<T> quasarFiber = new Fiber<T>() {
            @Override protected T run() throws SuspendExecution, InterruptedException {
                return task.get();
            }
        }.start();

        return () -> {
            try {
                return quasarFiber.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            } 
        };
    }

    private static <T> FiberAsync<T, CompletionException> quasarify(CompletableFuture<T> cf) {
        return new CfFiberAsync(cf);
    }
}
