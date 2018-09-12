package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.core.Settings;
import com.mercadopago.android.px.internal.repository.PaymentMethodRepository;
import com.mercadopago.android.px.internal.services.PaymentService;
import com.mercadopago.android.px.model.PaymentMethod;
import java.util.List;

public class PaymentMethodService implements PaymentMethodRepository {

    @NonNull private final PaymentService mPaymentService;

    public PaymentMethodService(@NonNull final PaymentService mPaymentService) {
        this.mPaymentService = mPaymentService;
    }

    @Override
    public MPCall<List<PaymentMethod>> getCardPaymentMethods(@NonNull final String accessToken) {
        return mPaymentService.getCardPaymentMethods(Settings.servicesVersion, accessToken);
    }
}
