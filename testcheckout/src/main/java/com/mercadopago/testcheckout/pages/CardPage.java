package com.mercadopago.testcheckout.pages;


import android.support.test.espresso.contrib.RecyclerViewActions;

import com.mercadopago.testcheckout.assertions.Validator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class CardPage extends PageObject {

    public CardPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CardPage(Validator validator) {
        super(validator);
    }

    public CreditCardPage selectCreditCard() {
        onView(withId(com.mercadopago.R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new CreditCardPage(validator);
    }

    public DebitCardPage selectDebitCard() {
        onView(withId(com.mercadopago.R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        return new DebitCardPage(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }
}
