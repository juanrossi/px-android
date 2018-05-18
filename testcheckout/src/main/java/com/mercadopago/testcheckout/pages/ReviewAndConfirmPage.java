package com.mercadopago.testcheckout.pages;

import com.mercadopago.R;
import com.mercadopago.testcheckout.assertions.Validator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ReviewAndConfirmPage extends PageObject {

    public ReviewAndConfirmPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public ReviewAndConfirmPage(Validator validator) {
        super(validator);
    }

    public CongratsPage pressConfirmButton() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new CongratsPage(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }
}
