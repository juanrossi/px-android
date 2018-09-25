package com.mercadopago.android.px.internal.adapters;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.services.Callback;

public abstract class MPCallWrapper<T> {

    protected abstract MPCall<T> method();

    public MPCall<T> wrap() {

        return new MPCall<T>() {
            @Override
            public void enqueue(final Callback<T> callback) {
                method().enqueue(callback);
            }

            @Override
            public void execute(final Callback<T> callback) {
                method().execute(callback);
            }
        };
    }
}
