package com.mercadopago.android.px.testcheckout.pages;

import android.support.test.espresso.action.ViewActions;
import android.view.View;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.testlib.pages.PageObject;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class NamePage extends PageObject<CheckoutValidator> {

    public NamePage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public NamePage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public NamePage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public ExpiryDatePage enterCardholderName(final String cardHolderName) {
        Matcher<View> cardCardholderNameEditTextMatcher = withId(com.mercadopago.android.px.R.id.mpsdkCardholderName);
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.android.px.R.id.mpsdkNextButton);
        onView(cardCardholderNameEditTextMatcher).perform(typeText(cardHolderName));
        onView(cardNextButtonTextMatcher).perform(click());
        return new ExpiryDatePage(validator);
    }

    public NoCheckoutPage pressBackWithExclusion() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new NoCheckoutPage(validator);
    }

    public PaymentMethodPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new PaymentMethodPage(validator);
    }

    public CreditCardPage pressPrevious() {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkScrollViewContainer))
                .perform(swipeUp());
        onView(withId(com.mercadopago.android.px.R.id.mpsdkBackButton)).perform(click());
        return new CreditCardPage(validator);
    }
}
