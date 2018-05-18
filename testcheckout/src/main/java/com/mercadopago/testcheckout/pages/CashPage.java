package com.mercadopago.testcheckout.pages;


import android.view.View;

import com.mercadopago.testcheckout.assertions.Validator;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CashPage extends PageObject {

    public CashPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CashPage(Validator validator) {
        super(validator);
    }

    public ReviewAndConfirmPage selectMethod(final String paymentMethodName) {
        Matcher<View> paymentCell = withText(paymentMethodName);
        onView(paymentCell).perform(click());
        return new ReviewAndConfirmPage(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }

}
