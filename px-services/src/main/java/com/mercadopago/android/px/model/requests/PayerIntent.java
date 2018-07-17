package com.mercadopago.android.px.model.requests;

import com.mercadopago.android.px.model.Payer;

public class PayerIntent {
    private Payer payer;

    public PayerIntent(Payer payer) {
        this.payer = payer;
    }
}
