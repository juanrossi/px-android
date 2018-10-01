package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.input.Country;
import com.mercadopago.android.px.testcheckout.input.FakeCard;
import com.mercadopago.android.px.testcheckout.input.Visa;
import com.mercadopago.android.px.testcheckout.pages.CallForAuthPage;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.OneTapPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.android.px.testcheckout.pages.PendingPage;
import com.mercadopago.android.px.testcheckout.pages.RejectedPage;
import org.junit.Test;

public class OneTapTestFlow extends TestFlow {

    public OneTapTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public OneTapTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    public CongratsPage runCardWithOneTapWithoutESCApprovedPaymentFlow(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();
        return new OneTapPage(validator)
            .pressConfirmButton()
            .enterSecurityCodeToCongratsPage(card.escNumber());
    }

    public PendingPage runSavedCardWithOneTapWithoutESCPendingPaymentFlow(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new OneTapPage(validator)
            .pressConfirmButton()
            .enterSecurityCodeToPendingPage(card.escNumber());
    }

    public CallForAuthPage runSavedCardWithOneTapWithoutESCCallForAuthPaymentFlow(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new OneTapPage(validator)
            .pressConfirmButton()
            .enterSecurityCodeToCallForAuthPage(card.escNumber());
    }

    public CallForAuthPage runSavedCardWithOneTapWithoutESCCallForAuthPaymentRetryCVVFlow(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new OneTapPage(validator)
            .pressConfirmButton()
            .enterSecurityCodeToCallForAuthPage(card.escNumber())
            .pressAlreadyAuthorizedButton()
            .enterSecurityCodeToCallForAuthPage(card.escNumber());
    }

    public PaymentMethodPage runSavedCardWithOneTapWithoutESCCallForAuthPaymentAndChangePaymentMethodFlow(
        @NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new OneTapPage(validator)
            .pressConfirmButton()
            .enterSecurityCodeToCallForAuthPage(card.escNumber())
            .pressChangePaymentMethodButton();
    }

    public OneTapPage runSavedCardWithOneTapWithoutESCCallForAuthPaymentChangePaymentMethodAndBackToOneTapFlow(
        @NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return runSavedCardWithOneTapWithoutESCCallForAuthPaymentAndChangePaymentMethodFlow(card, validator)
            .pressBack();
    }

    public CongratsPage runSavedCardWithOneTapWithoutESCCallForAuthPaymentChangePaymentMethodAndCardPaymentGetCongratsFlow(
        @NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        final PaymentMethodPage paymentMethodPage =
            runSavedCardWithOneTapWithoutESCCallForAuthPaymentAndChangePaymentMethodFlow(card, validator);

        return new CreditCardTestFlow(checkout, context)
            .runCreditCardPaymentFlowWithInstallmentsFromPaymentMethodPage(paymentMethodPage, card,
                CreditCardTestFlow.NO_INSTALLMENTS_OPTION);
    }

    //TODO
    public PaymentMethodPage runSavedCardWithOneTapWithoutESCCallForAuthCancelPaymentAndGetOutFlow(
        @NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return null;
    }

    //TODO
    public CongratsPage runSavedCardWithOneTapWithESCApprovedPaymentFlow(final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new CongratsPage(validator);
    }

    //TODO
    public CongratsPage runAccountMoneyWithOneTapApprovedPaymentFlow(final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new CongratsPage(validator);
    }

    //TODO
    public RejectedPage runAccountMoneyWithOneTapRejectedPaymentFlow(final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new RejectedPage(validator);
    }
}