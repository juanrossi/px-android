package com.mercadopago.testcheckout.pages;

import com.mercadopago.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ReviewAndConfirmPage extends PageObject {

    public CongratsPage pressConfirmButton() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new CongratsPage();
    }
}
