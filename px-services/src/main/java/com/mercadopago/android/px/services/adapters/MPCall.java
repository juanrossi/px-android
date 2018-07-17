package com.mercadopago.android.px.services.adapters;

import com.mercadopago.android.px.services.callbacks.Callback;

public interface MPCall<T> {

    void enqueue(Callback<T> callback);

    void execute(Callback<T> callback);
}

