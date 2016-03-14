package cla.completablefuture.jira.nonblocking.callback;

import java.util.function.Consumer;

public interface Callback<T> {
    
    void handle(Consumer<T> onSuccess, Consumer<Throwable> onFailure);
    
}
