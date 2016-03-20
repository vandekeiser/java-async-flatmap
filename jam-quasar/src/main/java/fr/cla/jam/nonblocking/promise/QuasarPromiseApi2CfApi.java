package fr.cla.jam.nonblocking.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QuasarPromiseApi2CfApi {

    public static <T, U> Function<
        Function<T, Promise<U>>,
        Function<T, CompletableFuture<U>>
    > usingFiberScheduler(FiberScheduler dedicatedScheduler) {
        return callback -> input -> startWaitingForCallbackInFiberScheduler(callback, input, dedicatedScheduler);
    }

    private static <T, U> CompletableFuture<U> startWaitingForCallbackInFiberScheduler(
        Function<T, Promise<U>> promise,
        T input,
        FiberScheduler dedicatedScheduler
    ) {
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        //DOES MORE HARM THAN GOOD!
//        new Fiber<>(dedicatedScheduler, () -> promise.apply(input).whenComplete(
//            res -> fiberCf.complete(res),
//            x -> fiberCf.completeExceptionally(x)
//        )).start();

        promise.apply(input).whenComplete(
            res -> fiberCf.complete(res),
            x -> fiberCf.completeExceptionally(x)
        );

        return fiberCf;
    }

}
