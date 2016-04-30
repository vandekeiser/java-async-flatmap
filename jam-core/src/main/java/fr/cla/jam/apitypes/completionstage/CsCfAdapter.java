package fr.cla.jam.apitypes.completionstage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class CsCfAdapter {

    public <S, T> Function<S, CompletableFuture<T>> toCompletableFuture(
        Function<S, CompletionStage<T>> adaptee
    ) {
        return input -> {
            CompletableFuture<T> cf = new CompletableFuture<>();
            adaptee.apply(input).whenComplete((success, failure) -> {
                if(failure != null) cf.completeExceptionally(failure);
                else cf.complete(success);
            });
            return cf;
        };
    }

}
