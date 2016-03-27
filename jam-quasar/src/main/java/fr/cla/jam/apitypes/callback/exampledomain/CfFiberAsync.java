package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.fibers.FiberAsync;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CfFiberAsync<I, O> extends FiberAsync<O, RuntimeException> {

    private I input;
    private Function<I, CompletableFuture<O>> futureProducer;

    public CfFiberAsync(I bundleName, Function<I, CompletableFuture<O>> futureProducer) {
        this.input = bundleName;
        this.futureProducer = futureProducer;
    }

    @Override
    protected void requestAsync() {
        futureProducer.apply(input).whenCompleteAsync((r, x) -> {
            if(x != null) super.asyncFailed(x);
            else super.asyncCompleted(r);
        });
    }

}
