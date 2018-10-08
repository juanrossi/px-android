package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.pages.CardPage;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.CreditCardPage;
import com.mercadopago.android.px.testcheckout.pages.NamePage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewAndConfirmPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewPaymentMethodsPage;

public class CreditCardTestFlow extends TestFlow {

    public static final int NO_INSTALLMENTS_OPTION = 0;
    public static final int TWO_INSTALLMENTS_OPTION = 2;

    public CreditCardTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CreditCardTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card, final int installmentsOption,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator)
            .selectCard()
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
    public CongratsPage runCreditCardPaymentFlowWithBankSelectionAndInstallmentsOption(@NonNull final Card card,
        final int bankOption, final int installmentsOption, final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCode(card.escNumber())
            .enterIdentificationNumberToIssuer(card.cardHolderIdentityNumber())
            .enterBankOptionToInstallments(bankOption)
            .selectInstallments(installmentsOption)
            .pressConfirmButton();
    }

    @NonNull
    public CreditCardPage runCreditCardPaymentFlowWithUniquePaymentMethod(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();
        return new PaymentMethodPage(validator).selectCard()
            .selectCreditCard();
    }

    @NonNull
    public CreditCardPage runCreditCardPaymentFlowWithUniquePaymentMethod(@NonNull final Card card) {
        return runCreditCardPaymentFlowWithUniquePaymentMethod(card, null);
    }

    @NonNull
    public NamePage runCreditCardPaymentFlowWithUniquePaymentMethodAndEnterNumber(@NonNull final Card card) {
        return runCreditCardPaymentFlowWithUniquePaymentMethodAndEnterNumber(card, null);
    }

    @NonNull
    public NamePage runCreditCardPaymentFlowWithUniquePaymentMethodAndEnterNumber(@NonNull final Card card,
        final CheckoutValidator validator) {
        CreditCardPage creditCardPage = runCreditCardPaymentFlowWithUniquePaymentMethod(card, validator);
        return creditCardPage.enterCreditCardNumber(card.cardNumber());
    }

    @NonNull
    public ReviewPaymentMethodsPage runCreditCardPaymentFlowWithPaymentMethodExcluded(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();
        return new PaymentMethodPage(validator).selectCard()
            .selectCreditCard()
            .enterExcludedCreditCardNumber(card.cardNumber())
            .clickPaymentMethodNotSupportedSnackbar();
    }

    @NonNull
    public ReviewPaymentMethodsPage runCreditCardPaymentFlowWithPaymentMethodExcluded(@NonNull final Card card) {
        return runCreditCardPaymentFlowWithPaymentMethodExcluded(card, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithPaymentMethodExcludedAndTryAnotherCard(
        @NonNull final Card excludedCard,
        @NonNull final Card supportedCard) {
        final ReviewPaymentMethodsPage reviewPaymentMethodsPage =
            runCreditCardPaymentFlowWithPaymentMethodExcluded(excludedCard);
        return reviewPaymentMethodsPage.clickEnterCardButton()
            .enterCreditCardNumber(supportedCard.cardNumber())
            .enterCardholderName(supportedCard.cardHolderName())
            .enterExpiryDate(supportedCard.expDate())
            .enterSecurityCode(supportedCard.escNumber())
            .enterIdentificationNumberToInstallments(supportedCard.cardHolderIdentityNumber())
            .selectInstallments(TWO_INSTALLMENTS_OPTION)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithBankSelectionAndInstallmentsOption(@NonNull final Card card,
        final int bankOption, final int installmentsOption) {
        return runCreditCardPaymentFlowWithBankSelectionAndInstallmentsOption(card, bankOption, installmentsOption,
            null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card,
        final int installmentsOption) {
        return runCreditCardPaymentFlowWithInstallments(card, installmentsOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowInstallmentsFirstOption(@NonNull final Card card,
        final CheckoutValidator validator) {
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
    public CongratsPage runCreditCardPaymentFlowWithInstallments(@NonNull final Card card,
        final CheckoutValidator validator) {
        return runCreditCardPaymentFlowWithInstallments(card, TWO_INSTALLMENTS_OPTION, validator);
    }

    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(@NonNull final Card card,
        final int bankOption, final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCode(card.escNumber())
            .enterIdentificationNumberToIssuer(card.cardHolderIdentityNumber())
            .enterBankOptionToReviewAndConfirm(bankOption)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(@NonNull final Card card,
        final int bankOption) {
        return runCreditCardPaymentFlowNoInstallmentsOptionAndBankSelection(card, bankOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardOnlyPaymentAvailable(@NonNull final Card card, final CheckoutValidator validator) {
        startCheckout();

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

    @NonNull
    public ReviewAndConfirmPage runCreditCardUntilReviewAndConfirm(@NonNull final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator)
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCode(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .selectInstallments(NO_INSTALLMENTS_OPTION);
    }

    @NonNull
    public ReviewAndConfirmPage runCreditCardUntilReviewAndConfirm(@NonNull final Card card) {
        return runCreditCardUntilReviewAndConfirm(card, null);
    }

    @NonNull
    public CongratsPage runCreditCardFlowFromPaymentMethod(@NonNull final Card card,
        @NonNull final PaymentMethodPage paymentMethodPage, final CheckoutValidator validator) {
        return paymentMethodPage
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCode(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .selectInstallments(NO_INSTALLMENTS_OPTION)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardFlowFromPaymentMethod(@NonNull final Card card,
        @NonNull final PaymentMethodPage paymentMethodPage) {
        return runCreditCardFlowFromPaymentMethod(card, paymentMethodPage, null);
    }
}
