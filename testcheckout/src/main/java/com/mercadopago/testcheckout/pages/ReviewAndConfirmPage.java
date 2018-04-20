package com.mercadopago.testcheckout.pages;

import android.support.test.espresso.PerformException;

import com.mercadopago.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mercadopago.testlib.utils.NestedScroll.nestedScrollTo;

public class ReviewAndConfirmPage extends PageObject {

    public CongratsPage pressConfirmButton(){
        try {
            onView(withId(R.id.floating_confirm)).perform(click());
        } catch (PerformException e) {
            onView(withId(R.id.text_button_blue)).perform(nestedScrollTo(), click());
        }
        return new CongratsPage();
    }
}
