package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.internal.services.PaymentService;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.tracking.internal.Settings;
import java.util.List;

public class CardPaymentMethodService implements CardPaymentMethodRepository {

    @NonNull private final PaymentService mPaymentService;

    public CardPaymentMethodService(@NonNull final PaymentService mPaymentService) {
        this.mPaymentService = mPaymentService;
    }

    @Override
    public MPCall<List<PaymentMethod>> getCardPaymentMethods(@NonNull final String accessToken) {
        return mPaymentService.getCardPaymentMethods(Settings.servicesVersion, accessToken);
    }
}
