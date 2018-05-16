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
import com.mercadopago.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.testcheckout.pages.ReviewAndConfirmPage;
import com.mercadopago.testcheckout.pages.SecurityCodePage;

public interface Validator {

    void validate(@NonNull final CardPage cardPage);

    void validate(@NonNull final CashPage cashPage);

    void validate(@NonNull final CongratsPage congratsPage);

    void validate(@NonNull final CreditCardPage creditCardPage);

    void validate(@NonNull final DebitCardPage debitCardPage);

    void validate(@NonNull final ExpiryDatePage expiryDatePage);

    void validate(@NonNull final IdentificationPage identificationPage);

    void validate(@NonNull final InstallmentsPage installmentsPage);

    void validate(@NonNull final IssuerPage issuerPage);

    void validate(@NonNull final NamePage namePage);

    void validate(@NonNull final PaymentMethodPage paymentMethodPage);

    void validate(@NonNull final ReviewAndConfirmPage reviewAndConfirmPage);

    void validate(@NonNull final SecurityCodePage securityCodePage);

}
