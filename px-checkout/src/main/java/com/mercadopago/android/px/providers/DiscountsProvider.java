package com.mercadopago.android.px.providers;

import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.mvp.ResourcesProvider;
import com.mercadopago.android.px.mvp.TaggedCallback;

public interface DiscountsProvider extends ResourcesProvider {
    void getDirectDiscount(String transactionAmount, String payerEmail, TaggedCallback<Discount> taggedCallback);

    void getCodeDiscount(String transactionAmount, String payerEmail, String discountCode,
        TaggedCallback<Discount> taggedCallback);

    String getApiErrorMessage(String error);

    String getStandardErrorMessage();
}
