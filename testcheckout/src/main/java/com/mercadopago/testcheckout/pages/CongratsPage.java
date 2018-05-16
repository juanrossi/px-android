package com.mercadopago.testcheckout.pages;

import com.mercadopago.testcheckout.assertions.Validator;

public class CongratsPage extends PageObject {


    public CongratsPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CongratsPage(Validator validator) {
        super(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }
}
