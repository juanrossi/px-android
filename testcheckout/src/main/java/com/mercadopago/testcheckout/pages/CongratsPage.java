package com.mercadopago.testcheckout.pages;

import com.mercadopago.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.testlib.pages.PageObject;

public class CongratsPage extends PageObject<CheckoutValidator> {


    public CongratsPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CongratsPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public CongratsPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
