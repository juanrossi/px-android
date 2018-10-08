package com.mercadopago.android.px.testcheckout.pages;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

public class PendingPage extends PageObject<CheckoutValidator> {

    public PendingPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public PendingPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public PendingPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
