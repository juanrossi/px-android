package com.mercadopago.model;

import com.google.gson.annotations.SerializedName;

public class OneTapMetadata {
    @SerializedName("payment_method_id")
    public String paymentMethodId;
    @SerializedName("payment_type_id")
    public String paymentTypeId;
    public CardPaymentMetadata card;
}
