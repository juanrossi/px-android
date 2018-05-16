package com.mercadopago.testcheckout.pages;


import com.mercadopago.testcheckout.assertions.DefaultValidator;
import com.mercadopago.testcheckout.assertions.Validator;

public abstract class PageObject {

    protected final Validator validator;

    protected PageObject() {
        this(null);
    }

    protected PageObject(Validator validator) {
        this.validator = validator == null ? new DefaultValidator() : validator;
        //Page objects validate themselves, at least using DefaultValidator.
        validate();
    }

    protected abstract void validate();
}
