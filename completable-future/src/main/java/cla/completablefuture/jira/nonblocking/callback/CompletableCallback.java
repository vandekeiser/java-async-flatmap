package cla.completablefuture.jira.nonblocking.callback;

public interface CompletableCallback<T> extends Callback<T> {
    
    void complete(T success);

    void completeExceptionnally(Throwable failure);
    
}
