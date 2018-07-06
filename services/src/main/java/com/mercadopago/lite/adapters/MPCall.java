package com.mercadopago.lite.adapters;

import com.mercadopago.lite.callbacks.Callback;

public interface MPCall<T> {

    void enqueue(Callback<T> callback);

    void execute(Callback<T> callback);
}

