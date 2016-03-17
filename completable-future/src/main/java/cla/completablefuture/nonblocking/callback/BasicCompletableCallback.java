package cla.completablefuture.nonblocking.callback;

import java.util.function.Consumer;

public class BasicCompletableCallback<T> implements CompletableCallback<T> {

    private boolean consumersReady;
    private Consumer<T> onSuccess;
    private Consumer<Throwable> onFailure;

    private boolean successReady, failureReady;
    private T success;
    private Throwable failure;

    @Override
    public synchronized void complete(T success) {
        if(consumersReady) onSuccess.accept(success);
        else {
            this.success = success;
            successReady = true;
        }
    }

    @Override
    public synchronized void completeExceptionnally(Throwable failure) {
        if(consumersReady) onFailure.accept(failure);
        else {
            this.failure = failure;
            failureReady = true;
        }
    }

    @Override
    public synchronized void whenComplete(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        if(this.consumersReady) throw new IllegalStateException("cant call whenComplete twice");

        if(this.successReady) {
            onSuccess.accept(success);
        } else if(this.failureReady) {
            onFailure.accept(failure);
        } else {
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
            this.consumersReady = true;
        }
    }

}
