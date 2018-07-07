package com.mercadopago.testcheckout.assertions;

import android.support.annotation.NonNull;

import com.mercadopago.testcheckout.pages.CardPage;
import com.mercadopago.testcheckout.pages.CashPage;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testcheckout.pages.CreditCardPage;
import com.mercadopago.testcheckout.pages.DebitCardPage;
import com.mercadopago.testcheckout.pages.ExpiryDatePage;
import com.mercadopago.testcheckout.pages.IdentificationPage;
import com.mercadopago.testcheckout.pages.InstallmentsPage;
import com.mercadopago.testcheckout.pages.IssuerPage;
import com.mercadopago.testcheckout.pages.NamePage;
import com.mercadopago.testcheckout.pages.NoCheckoutPage;
import com.mercadopago.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.testcheckout.pages.ReviewAndConfirmPage;
import com.mercadopago.testcheckout.pages.ReviewPaymentMethodsPage;
import com.mercadopago.testcheckout.pages.SecurityCodePage;

public class DefaultValidator implements CheckoutValidator {
    @Override
    public void validate(@NonNull final IssuerPage issuerPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final NamePage namePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final PaymentMethodPage paymentMethodPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final ReviewAndConfirmPage reviewAndConfirmPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final SecurityCodePage securityCodePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final NoCheckoutPage noCheckoutPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CardPage cardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CashPage cashPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CongratsPage congratsPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CreditCardPage creditCardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final DebitCardPage debitCardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final ExpiryDatePage expiryDatePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final IdentificationPage identificationPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final InstallmentsPage installmentsPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final ReviewPaymentMethodsPage reviewPaymentMethodsPage) {
        //TODO implement default PX Validations
    }
}
