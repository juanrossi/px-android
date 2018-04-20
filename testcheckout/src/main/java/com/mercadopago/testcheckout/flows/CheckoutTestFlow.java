package com.mercadopago.testcheckout.flows;

import android.support.annotation.NonNull;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.testcheckout.input.Card;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testcheckout.pages.CreditCardPage;
import com.mercadopago.testcheckout.pages.PaymentMethodPage;

public class CheckoutTestFlow {

    private MercadoPagoCheckout.Builder builder;

    public static CheckoutTestFlow createFlow() {
        return new CheckoutTestFlow();
    }

    public static CheckoutTestFlow createFlowWithCheckout(@NonNull MercadoPagoCheckout.Builder builder) {
        return new CheckoutTestFlow(builder);
    }

    private CheckoutTestFlow() {
    }

    private CheckoutTestFlow(@NonNull MercadoPagoCheckout.Builder builder) {
        this.builder = builder;
    }


    public CongratsPage runCreditCardPaymentFlowWithInstallments(Card card, int installmentsOption) {
        PaymentMethodPage paymentMethodPage = new PaymentMethodPage();

        if (builder != null) {
            paymentMethodPage.start(builder);
        }

        return paymentMethodPage.selectCard()
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

        if (builder != null) {
            paymentMethodPage.start(builder);
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
        PaymentMethodPage paymentMethodPage = new PaymentMethodPage();

        if (builder != null) {
            paymentMethodPage.start(builder);
        }

        return paymentMethodPage
                .selectCash()
                .selectMethod(paymentMethodName)
                .pressConfirmButton();

    }

    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(final Card card, final int bankOption) {
        PaymentMethodPage paymentMethodPage = new PaymentMethodPage();

        if (builder != null) {
            paymentMethodPage.start(builder);
        }

        return paymentMethodPage.selectCard()
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
        CreditCardPage creditCardPage = new CreditCardPage();

        if (builder != null) {
            creditCardPage.start(builder);
        }

        return creditCardPage.enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
                .selectInstallments(0)
                .pressConfirmButton();
    }
}
