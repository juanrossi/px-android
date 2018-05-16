package com.mercadopago.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.testcheckout.assertions.Validator;
import com.mercadopago.testcheckout.input.Card;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testcheckout.pages.CreditCardPage;
import com.mercadopago.testcheckout.pages.PaymentMethodPage;

public class CreditCardTestFlow extends TestFlow {

    private final static int NO_INSTALLMENTS_OPTION = 0;
    private final static int TWO_INSTALLMENTS_OPTION = 2;

    public CreditCardTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CreditCardTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card, @NonNull final int installmentsOption, final Validator validator) {
        super.startCheckout();

        return new PaymentMethodPage(validator).selectCard()
                .selectCreditCard()
                .enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
                .selectInstallments(installmentsOption)
                .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card, @NonNull final int installmentsOption) {
        return runCreditCardPaymentFlowWithInstallments(card, installmentsOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowInstallmentsFirstOption(@NonNull final Card card,final Validator validator) {
        return runCreditCardPaymentFlowWithInstallments(card, NO_INSTALLMENTS_OPTION, validator);
    }


    @NonNull
    public CongratsPage runCreditCardPaymentFlowInstallmentsFirstOption(@NonNull final Card card) {
        return runCreditCardPaymentFlowInstallmentsFirstOption(card, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card) {
        return runCreditCardPaymentFlowWithInstallments(card, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card, final Validator validator) {
        return runCreditCardPaymentFlowWithInstallments(card, TWO_INSTALLMENTS_OPTION, validator);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(@NonNull final Card card, @NonNull final int bankOption, final Validator validator) {
        super.startCheckout();

        return new PaymentMethodPage(validator).selectCard()
                .selectCreditCard()
                .enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToIssuer(card.cardHolderIdentityNumber())
                .enterBankOption(bankOption)
                .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(@NonNull final Card card, @NonNull final int bankOption) {
        return runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(card, bankOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardOnlyPaymentAvailable(@NonNull final Card card, final Validator validator) {
        super.startCheckout();

        return new CreditCardPage(validator).enterCreditCardNumber(card.cardNumber())
                .enterCardholderName(card.cardHolderName())
                .enterExpiryDate(card.expDate())
                .enterSecurityCode(card.escNumber())
                .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
                .selectInstallments(NO_INSTALLMENTS_OPTION)
                .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardOnlyPaymentAvailable(@NonNull final Card card) {
        return runCreditCardOnlyPaymentAvailable(card, null);
    }
}
