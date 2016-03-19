package fr.cla.jam.nonblocking.callback;

import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarifyCallback {

    public static <T, U> Function<
        BiConsumer<T, Callback<U>>,
        Function<T, CompletableFuture<U>>
    > usingFiberScheduler(FiberScheduler dedicatedScheduler) {
        return callback -> input -> startWaitingForCallbackInFiberScheduler(callback, input, dedicatedScheduler);
    }

    private static <T, U> CompletableFuture<U> startWaitingForCallbackInFiberScheduler(
        BiConsumer<T, Callback<U>> call,
        T input,
        FiberScheduler dedicatedScheduler
    ) {
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

//        System.out.println("startWaitingForCallbackInFiber 0");// ON PASSE LA QUE poolSize FOIS!!!
//        new Fiber<>(dedicatedScheduler, () -> {
//            System.out.println("startWaitingForCallbackInFiber 1");//ON ARRIVE MM PAS LA 1 fois!!!
//            call.accept(input, new Callback<U>() {
//                @Override public void onSuccess(U success) {
//                    System.out.println("startWaitingForCallbackInFiber 2");
//                    fiberCf.complete(success);
//                }
//                @Override public void onFailure(Throwable failure) {
//                    System.out.println("startWaitingForCallbackInFiber 3");
//                    fiberCf.completeExceptionally(failure);
//                }
//            });
//        }).start();

        call.accept(input, new Callback<U>() {
            @Override public void onSuccess(U success) {
                //System.out.println("startWaitingForCallbackInFiber 2");
                fiberCf.complete(success);
            }
            @Override public void onFailure(Throwable failure) {
                //System.out.println("startWaitingForCallbackInFiber 3");
                fiberCf.completeExceptionally(failure);
            }
        });

        return fiberCf;
    }

}
