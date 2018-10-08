package com.mercadopago.android.px.testcheckout.pages;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

public class RejectedPage extends PageObject<CheckoutValidator> {

    public RejectedPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public RejectedPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public RejectedPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
