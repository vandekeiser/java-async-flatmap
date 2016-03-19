package fr.cla.jam.nonblocking.promise;

import java.util.function.Consumer;

public interface Promise<T> {

    void whenComplete(Consumer<T> onSuccess, Consumer<Throwable> onFailure);

}
