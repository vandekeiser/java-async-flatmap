package fr.cla.jam.apitypes.callback;

public interface Callback<T> {
    
    void onSuccess(T success);

    void onFailure(Throwable failure);
    
}
