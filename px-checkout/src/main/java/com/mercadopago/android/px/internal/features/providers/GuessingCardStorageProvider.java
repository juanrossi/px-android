package com.mercadopago.android.px.internal.features.providers;

import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.model.PaymentMethod;
import java.util.List;

public interface GuessingCardStorageProvider extends ResourcesProvider {

    void getCardPaymentMethods(String accessToken, final TaggedCallback<List<PaymentMethod>> taggedCallback);
}
