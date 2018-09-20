package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.PaymentMethod;
import java.util.List;

public interface CardPaymentMethodRepository {
    MPCall<List<PaymentMethod>> getCardPaymentMethods(@NonNull final String accessToken);
}
