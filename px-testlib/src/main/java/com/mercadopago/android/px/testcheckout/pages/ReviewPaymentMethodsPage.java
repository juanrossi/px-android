package com.mercadopago.android.px.testcheckout.pages;

import android.view.View;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ReviewPaymentMethodsPage extends PageObject<CheckoutValidator> {

    public ReviewPaymentMethodsPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public ReviewPaymentMethodsPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public PageObject validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public CreditCardPage clickEnterCardButton() {
        final Matcher<View> enterOtherCardButtonMatcher = withId(com.mercadopago.android.px.R.id.tryOtherCardButton);
        onView(enterOtherCardButtonMatcher).perform(click());
        return new CreditCardPage(validator);
    }
}
