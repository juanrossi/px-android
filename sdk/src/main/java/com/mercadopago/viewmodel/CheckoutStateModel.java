package com.mercadopago.viewmodel;

import android.support.annotation.NonNull;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.model.Card;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.PaymentResultScreenPreference;
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

    public final PaymentResultScreenPreference paymentResultScreenPreference;

    public final boolean isBinary;

    public final int requestedResult;

    public Issuer selectedIssuer;
    public Token createdToken;
    public Card selectedCard;
    public Payment createdPayment;
    public Payer collectedPayer;
    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public String currentPaymentIdempotencyKey;
    public String merchantPublicKey;
    public boolean isUniquePaymentMethod;
    public boolean isOneTap;

    public CheckoutStateModel(final int requestedResult, @NonNull final MercadoPagoCheckout config) {
        this.requestedResult = requestedResult;
        paymentResultInput = config.getPaymentResult();
        paymentDataInput = config.getPaymentData();
        paymentResultScreenPreference = config.getPaymentResultScreenPreference();
        isBinary = config.isBinaryMode();
    }
}
