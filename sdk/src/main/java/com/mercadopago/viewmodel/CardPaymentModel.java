package com.mercadopago.viewmodel;

import android.support.annotation.NonNull;
import com.mercadopago.model.Card;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Token;

public class CardPaymentModel {

    @NonNull public final Card card;
    @NonNull public Token token;
    @NonNull public PayerCost payerCost;
    @NonNull public Issuer issuer;

    public CardPaymentModel(@NonNull final Card card,
        @NonNull final Token token,
        @NonNull final PayerCost payerCost,
        @NonNull final Issuer issuer) {
        this.card = card;
        this.token = token;
        this.payerCost = payerCost;
        this.issuer = issuer;
    }
}
