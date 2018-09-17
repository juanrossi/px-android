package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentResult;

public interface PaymentRepository {

    void startPayment();

    void startOneTapPayment(@NonNull final OneTapModel oneTapModel);

    @NonNull
    PaymentData getPaymentData();

    @NonNull
    PaymentResult createPaymentResult(IPayment genericPayment);

    int getPaymentTimeout();

    void attach(@NonNull final PaymentServiceHandler handler);

    void detach();
}
