package fr.cla.jam.apitypes.sync;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

/**
 * POOR USE OF QUASAR, since it blocks
 *
 * http://docs.paralleluniverse.co/quasar/
 * Quasar’s chief contribution is that of the lightweight thread, called fiber in Quasar.
 * Fibers provide functionality similar to threads, and a similar API, but they’re not managed by the OS. 
 * They are lightweight in terms of RAM (an idle fiber occupies ~400 bytes of RAM) 
 * and put a far lesser burden on the CPU when task-switching. 
 * You can have millions of fibers in an application. 
 * If you are familiar with Go, fibers are like goroutines. 
 * Fibers in Quasar are scheduled by one or more ForkJoinPools. 
 * 
 * MAIS:
 * A fiber that is stuck in a loop without sync,
 * or is sync the thread its running on (by directly or indirectly performing a thread-sync operation)
 * is called a runaway fiber. 
 * It is perfectly OK for fibers to do that sporadically (as the work stealing scheduler will deal with that), 
 * but doing so frequently may severely impact system performance 
 * (as most of the scheduler’s threads might be tied up by runaway fibers). 
 * Quasar detects runaway fibers, and notifies you about which fibers are problematic, 
 * whether they’re sync the thread or hogging the CPU, and gives you their stack trace,
 * by printing this information to the console as well as reporting it to the runtime fiber monitor.
 */
public class QuasarSyncCfAdapter {

    public static <T> Function<Supplier<T>, CompletableFuture<T>> adapt() {
        return s -> CompletableFuture.supplyAsync(quasarify(s));
    }
    
    private static <T> Supplier<T> quasarify(Supplier<T> task) {
        Fiber<T> quasarFiber = new Fiber<>(()->task.get()).start();

        return () -> {
            try {
                return quasarFiber.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            } 
        };
    }

    public static <S, T> Function<S, CompletableFuture<T>> adaptUsingScheduler(
        Function<S, T> adaptee,
        FiberExecutorScheduler dedicatedScheduler
    ) {
        return s -> {
            CompletableFuture<T> cf = new CompletableFuture<>();
            new Fiber<>(dedicatedScheduler, () -> {
                try {
                    T success = adaptee.apply(s);
                    cf.complete(success);
                } catch (Throwable t) {
                    cf.completeExceptionally(t);
                }
            }).start();
            return cf;
        };
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> mapper
    ) {
        return SyncCfAdapter.flatMapAdapt(
            inputs,
            mapper,
            QuasarSyncCfAdapter.adapt()
        );
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAdaptUsingScheduler(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        FiberExecutorScheduler dedicatedScheduler
    ) {
        return SyncCfAdapter.flatMapAdapt(
            inputs,
            mapper,
            mappingResultSupplier -> {
                CompletableFuture<Set<F>> cf = new CompletableFuture<>();
                new Fiber<>(dedicatedScheduler, () -> {
                    try {
                        Set<F> success = mappingResultSupplier.get();
                        cf.complete(success);
                    } catch (Throwable t) {
                        cf.completeExceptionally(t);
                    }
                }).start();
                return cf;
            }
        );
    }

}
