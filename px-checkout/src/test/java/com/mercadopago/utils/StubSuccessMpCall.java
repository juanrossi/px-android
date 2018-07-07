package com.mercadopago.utils;

import com.mercadopago.services.adapters.MPCall;
import com.mercadopago.services.callbacks.Callback;

public class StubSuccessMpCall<T> implements MPCall<T> {

    private final T value;

    public StubSuccessMpCall(T value) {
        this.value = value;
    }


    @Override
    public void enqueue(final Callback<T> callback) {
        callback.success(value);
    }

    @Override
    public void execute(final Callback<T> callback) {
        callback.success(value);
    }
}
