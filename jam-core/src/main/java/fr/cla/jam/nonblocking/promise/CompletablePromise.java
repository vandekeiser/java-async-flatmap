package fr.cla.jam.nonblocking.promise;

import java.util.function.Consumer;

public interface CompletablePromise<T> extends Promise<T> {

    void complete(T success);

    void completeExceptionnally(Throwable failure);

    static <T> CompletablePromise<T> basic() {
        return new BasicCompletablePromise<>();
    }

    class BasicCompletablePromise<T> implements CompletablePromise<T> {
        private boolean consumersReady;
        private Consumer<T> onSuccess;
        private Consumer<Throwable> onFailure;

        private boolean succeeded, failed;
        private T success;
        private Throwable failure;

        @Override
        public synchronized void complete(T success) {
            if (consumersReady) onSuccess.accept(success);
            else {
                this.success = success;
                this.succeeded = true;
            }
        }

        @Override
        public synchronized void completeExceptionnally(Throwable failure) {
            if (consumersReady) onFailure.accept(failure);
            else {
                this.failure = failure;
                this.failed = true;
            }
        }

        @Override
        public synchronized void whenComplete(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
            if (this.consumersReady) throw new IllegalStateException("cant call whenComplete twice");

            if (this.succeeded) {
                onSuccess.accept(success);
            } else if (this.failed) {
                onFailure.accept(failure);
            } else {
                this.onSuccess = onSuccess;
                this.onFailure = onFailure;
                this.consumersReady = true;
            }
        }

    }


}
