package com.mercadopago.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.testcheckout.input.Card;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testcheckout.pages.CreditCardPage;
import com.mercadopago.testcheckout.pages.PaymentMethodPage;

public class CheckoutTestFlow {

    private Context context;
    private MercadoPagoCheckout checkout;


    /**
     * If you already started the checkout you can use an empty constructor
     */
    private CheckoutTestFlow() {
    }

    private CheckoutTestFlow(final MercadoPagoCheckout mercadoPagoCheckout, final Context context) {
        checkout = mercadoPagoCheckout;
        this.context = context;
    }

    /**
     * If you already started the checkout you can use an empty constructor
     */
    public static CheckoutTestFlow createFlow() {
        return new CheckoutTestFlow();
    }

    /**
     * If you want to run with an instrumented context
     * you can use this static method.
     *
     * @param mercadoPagoCheckout the checkout configuration.
     * @param context             context that will start the checkout.
     * @return
     */
    public static CheckoutTestFlow createFlowWithCheckout(@NonNull final MercadoPagoCheckout mercadoPagoCheckout,
                                                          @NonNull final Context context) {
        return new CheckoutTestFlow(mercadoPagoCheckout, context);
    }

    public CongratsPage runCreditCardPaymentFlowWithInstallments(Card card, int installmentsOption) {
        if (checkout != null && context != null) {
            checkout.startForPayment(context);
        }

        return new PaymentMethodPage().selectCard()
                .selectCreditCard()
                .enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
                .selectInstallments(installmentsOption)
                .pressConfirmButton();
    }

    public CongratsPage runCreditCardPaymentFlowInstallmentsFirstOption(Card card) {
        return runCreditCardPaymentFlowWithInstallments(card, 0);
    }

    public CongratsPage runCreditCardPaymentFlowWithInstallments(Card card) {
        return runCreditCardPaymentFlowWithInstallments(card, 2);
    }

    public CongratsPage runDebitCardPaymentFlow(final Card card) {
        PaymentMethodPage paymentMethodPage = new PaymentMethodPage();

        if (checkout != null && context != null) {
            checkout.startForPayment(context);
        }

        return paymentMethodPage.selectCard()
                .selectDebitCard()
                .enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToReviewAndConfirm(card.cardHolderIdentityNumber())
                .pressConfirmButton();
    }

    public CongratsPage runOff(final String paymentMethodName) {

        if (checkout != null && context != null) {
            checkout.startForPayment(context);
        }

        return new PaymentMethodPage()
                .selectCash()
                .selectMethod(paymentMethodName)
                .pressConfirmButton();

    }

    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(final Card card, final int bankOption) {
        if (checkout != null && context != null) {
            checkout.startForPayment(context);
        }

        return new PaymentMethodPage().selectCard()
                .selectCreditCard()
                .enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToIssuer(card.cardHolderIdentityNumber())
                .enterBankOption(bankOption)
                .pressConfirmButton();

    }

    public CongratsPage runCreditCardOnlyPaymentAvailable(final Card card) {
        if (checkout != null && context != null) {
            checkout.startForPayment(context);
        }

        return new CreditCardPage().enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
                .selectInstallments(0)
                .pressConfirmButton();
    }
}
