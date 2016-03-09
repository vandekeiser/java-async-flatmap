package cla.completablefuture.jenkins.blocking;

import co.paralleluniverse.fibers.FiberAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CfFiberAsync<T> extends FiberAsync<T, CompletionException> {

    private final CompletableFuture<T> cf;

    public CfFiberAsync(CompletableFuture<T> cf) {
        this.cf = cf.whenComplete((t,x) -> {
            if(x != null) super.asyncFailed(x);
            else super.asyncCompleted(t);
        });
    }

    @Override
    protected void requestAsync() {
        
    }
}
