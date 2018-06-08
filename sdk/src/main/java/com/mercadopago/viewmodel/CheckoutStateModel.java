package com.mercadopago.viewmodel;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import java.io.Serializable;
import java.math.BigDecimal;

public final class CheckoutStateModel implements Serializable {

    /**
     * If preference id is set then the checkout did not start
     * with a custom CheckoutPreference created by hand.
     */
    public String checkoutPreferenceId;

    /**
     * If preference is set then the checkout have just started
     * with a custom CheckoutPreference created by hand.
     */
    public CheckoutPreference checkoutPreference;

    public FlowPreference flowPreference;
    public ServicePreference servicePreference;
    public PaymentResultScreenPreference paymentResultScreenPreference;
    public Discount discount;
    public Campaign campaign;
    public PaymentData paymentDataInput;
    public PaymentResult paymentResultInput;
    public boolean binaryMode;
    public int requestedResult;
    public PaymentMethodSearch paymentMethodSearch;
    public Issuer selectedIssuer;
    public PayerCost selectedPayerCost;
    public Token createdToken;
    public Card selectedCard;
    public PaymentMethod selectedPaymentMethod;
    public Payment createdPayment;
    public Payer collectedPayer;
    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public String customerId;
    public String idempotencyKeySeed;
    public String currentPaymentIdempotencyKey;
    public String merchantPublicKey;

    private CheckoutStateModel() {
    }

    public static CheckoutStateModel from(final int requestedResult, MercadoPagoCheckout mercadoPagoCheckout) {
        final CheckoutStateModel checkoutStateModel = new CheckoutStateModel();
        checkoutStateModel.checkoutPreferenceId = mercadoPagoCheckout.getPreferenceId();
        checkoutStateModel.campaign = mercadoPagoCheckout.getCampaign();
        checkoutStateModel.checkoutPreference = mercadoPagoCheckout.getCheckoutPreference();
        checkoutStateModel.discount = mercadoPagoCheckout.getDiscount();
        checkoutStateModel.servicePreference = mercadoPagoCheckout.getServicePreference();
        checkoutStateModel.flowPreference = mercadoPagoCheckout.getFlowPreference();
        checkoutStateModel.paymentResultInput = mercadoPagoCheckout.getPaymentResult();
        checkoutStateModel.paymentDataInput = mercadoPagoCheckout.getPaymentData();
        checkoutStateModel.binaryMode = mercadoPagoCheckout.isBinaryMode();
        checkoutStateModel.paymentResultScreenPreference = mercadoPagoCheckout.getPaymentResultScreenPreference();
        checkoutStateModel.requestedResult = requestedResult;
        checkoutStateModel.idempotencyKeySeed = mercadoPagoCheckout.getMerchantPublicKey();
        checkoutStateModel.merchantPublicKey = mercadoPagoCheckout.getMerchantPublicKey();
        return checkoutStateModel;
    }

    public BigDecimal getTransactionAmount() {
        BigDecimal amount;

        if (discount != null) {
            amount = discount.getAmountWithDiscount(checkoutPreference.getTotalAmount());
        } else {
            amount = checkoutPreference.getTotalAmount();
        }

        return amount;
    }
}
