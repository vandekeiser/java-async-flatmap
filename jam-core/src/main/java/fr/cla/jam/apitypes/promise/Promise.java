package fr.cla.jam.apitypes.promise;

import java.util.function.Consumer;

public interface Promise<T> {

    void then(Consumer<T> onSuccess, Consumer<Throwable> onFailure);

}
