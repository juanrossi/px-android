package com.mercadopago.android.px.testcheckout.pages;


import android.view.View;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class DebitCardPage extends PageObject<CheckoutValidator> {

    public DebitCardPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public DebitCardPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public DebitCardPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public NamePage enterCreditCardNumber(final String cardNumber) {
        Matcher<View> cardNumberEditTextMatcher = withId(com.mercadopago.android.px.R.id.mpsdkCardNumber);
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.android.px.R.id.mpsdkNextButton);
        onView(cardNumberEditTextMatcher).perform(typeText(cardNumber));
        onView(cardNextButtonTextMatcher).perform(click());
        return new NamePage(validator);
    }
}
