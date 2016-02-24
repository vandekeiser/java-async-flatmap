package cla.completablefuture;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class QuasarCfAdapter {
    
    public static <T> CompletableFuture<T> supplyQuasar(Supplier<T> task) {
        return CompletableFuture.supplyAsync(quasarify(task));
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

}
