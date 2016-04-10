package fr.cla.jam.apitypes.callback;

import java.util.function.Consumer;

public interface Callback<T> {
    
    void onSuccess(T success);

    void onFailure(Throwable failure);

    static <S> Callback<S> either(Consumer<S> s, Consumer<Throwable> f) {
        return new Callback<S>() {
            @Override public void onSuccess(S success) {
                s.accept(success);
            }
            @Override public void onFailure(Throwable failure) {
                f.accept(failure);
            }
        };
    }

}
