package fr.cla.jam.apitypes.promise;

import java.util.function.Consumer;

public interface Promise<T> {

    void whenComplete(Consumer<T> onSuccess, Consumer<Throwable> onFailure);

}
