package com.mercadopago.viewmodel;

import android.support.annotation.NonNull;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.model.Card;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.Token;
import java.io.Serializable;

public final class CheckoutStateModel implements Serializable {

    /**
     * Mutable paymentResultInput.
     * //TODO remove
     */
    public PaymentResult paymentResultInput;

    /**
     * Mutable paymentResultInput.
     * //TODO remove
     */
    public PaymentData paymentDataInput;

    //TODO 21/06/2017 - Hack for credits, should remove payer access token.
    @Deprecated
    public final String privateKey;

    public final MercadoPagoCheckout config;

    public final int requestedResult;

    public PaymentMethodSearch paymentMethodSearch;
    public Issuer selectedIssuer;
    public Token createdToken;
    public Card selectedCard;
    public Payment createdPayment;
    public Payer collectedPayer;
    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public String customerId;
    public String currentPaymentIdempotencyKey;
    public String merchantPublicKey;

    public CheckoutStateModel(final int requestedResult,
        @NonNull final MercadoPagoCheckout config,
        @NonNull final String privateKey) {
        this.requestedResult = requestedResult;
        paymentResultInput = config.getPaymentResult();
        paymentDataInput = config.getPaymentData();
        this.privateKey = privateKey;
        this.config = config;
    }
}
