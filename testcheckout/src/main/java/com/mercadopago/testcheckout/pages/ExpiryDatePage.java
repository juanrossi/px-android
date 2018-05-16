package com.mercadopago.testcheckout.pages;

import android.view.View;

import com.mercadopago.testcheckout.assertions.Validator;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ExpiryDatePage extends PageObject {

    public ExpiryDatePage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public ExpiryDatePage(Validator validator) {
        super(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }

    public SecurityCodePage enterExpiryDate(final String s) {
        Matcher<View> cardExpiryDateEditTextMatcher = withId(com.mercadopago.R.id.mpsdkCardExpiryDate);
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.R.id.mpsdkNextButtonText);
        onView(cardExpiryDateEditTextMatcher).perform(typeText("0922"));
        onView(cardNextButtonTextMatcher).perform(click());
        return new SecurityCodePage(validator);
    }

}
