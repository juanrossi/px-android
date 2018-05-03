package com.mercadopago.testcheckout.pages;


import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CashPage extends PageObject {

    public ReviewAndConfirmPage selectMethod(final String paymentMethodName) {
        Matcher<View> paymentCell = withText(paymentMethodName);
        onView(paymentCell).perform(click());
        return new ReviewAndConfirmPage();
    }
}
