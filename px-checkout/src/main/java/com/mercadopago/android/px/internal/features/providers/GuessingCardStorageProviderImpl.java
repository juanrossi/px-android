package com.mercadopago.android.px.internal.features.providers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.repository.PaymentMethodRepository;
import com.mercadopago.android.px.model.PaymentMethod;
import java.util.List;

public class GuessingCardStorageProviderImpl implements GuessingCardStorageProvider {

    @NonNull final PaymentMethodRepository mPaymentMethodRepository;

    public GuessingCardStorageProviderImpl(
        @NonNull final PaymentMethodRepository mPaymentMethodRepository) {
        this.mPaymentMethodRepository = mPaymentMethodRepository;
    }

    @Override
    public void getCardPaymentMethods(final String accessToken,
        final TaggedCallback<List<PaymentMethod>> taggedCallback) {
        mPaymentMethodRepository.getCardPaymentMethods(accessToken).enqueue(taggedCallback);
    }
}
