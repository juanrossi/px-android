package com.mercadopago.android.px.utils;

import com.mercadopago.android.px.services.adapters.MPCall;
import com.mercadopago.android.px.services.callbacks.Callback;
import com.mercadopago.android.px.services.exceptions.ApiException;

public class StubFailMpCall<T> implements MPCall<T> {

    private final ApiException apiException;

    public StubFailMpCall(final ApiException apiException) {
        this.apiException = apiException;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        callback.failure(apiException);
    }

    @Override
    public void execute(final Callback<T> callback) {
        callback.failure(apiException);
    }
}
