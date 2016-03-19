package fr.cla.jam.nonblocking.callback;

public interface Callback<T> {
    
    void onSuccess(T success);

    void onFailure(Throwable failure);
    
}
