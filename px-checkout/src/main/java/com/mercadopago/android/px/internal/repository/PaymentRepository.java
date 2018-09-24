package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;

public interface PaymentRepository {

    void startPayment();

    void startOneTapPayment(@NonNull final OneTapModel oneTapModel);

    boolean isExplodingAnimationCompatible();

    @NonNull
    PaymentData getPaymentData();

    @NonNull
    PaymentResult createPaymentResult(IPayment genericPayment);

    int getPaymentTimeout();

    void attach(@NonNull final PaymentServiceHandler handler);

    void detach();

    void storePayment(@NonNull final IPayment iPayment);

    @Nullable
    IPayment getPayment();

    boolean hasPayment();

    @NonNull
    PaymentRecovery createRecoveryForInvalidESC();

    @NonNull
    PaymentRecovery createPaymentRecovery();
}
