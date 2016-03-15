package cla.completablefuture.jira.nonblocking.callback;

import java.util.function.Consumer;

public interface Callback<T> {
    
    void whenComplete(Consumer<T> onSuccess, Consumer<Throwable> onFailure);
    
}
