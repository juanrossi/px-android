package com.mercadopago.android.px.testcheckout.pages;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

public class NoCheckoutPage extends PageObject<CheckoutValidator> {

    public NoCheckoutPage() {
    }

    public NoCheckoutPage(final CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public NoCheckoutPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
