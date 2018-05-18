package com.mercadopago.testcheckout.pages;


import android.view.View;

import com.mercadopago.testcheckout.assertions.Validator;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class DebitCardPage extends PageObject {

    public DebitCardPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public DebitCardPage(Validator validator) {
        super(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }

    public NamePage enterCreditCardNumber(final String cardNumber) {
        Matcher<View> cardNumberEditTextMatcher = withId(com.mercadopago.R.id.mpsdkCardNumber);
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.R.id.mpsdkNextButtonText);
        onView(cardNumberEditTextMatcher).perform(typeText(cardNumber));
        onView(cardNextButtonTextMatcher).perform(click());
        return new NamePage(validator);
    }
}
