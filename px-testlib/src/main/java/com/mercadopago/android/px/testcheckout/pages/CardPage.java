package com.mercadopago.android.px.testcheckout.pages;


import android.support.test.espresso.contrib.RecyclerViewActions;

import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class CardPage extends PageObject<CheckoutValidator> {

    public CardPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CardPage(CheckoutValidator validator) {
        super(validator);
    }

    public CreditCardPage selectCreditCard() {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new CreditCardPage(validator);
    }

    public DebitCardPage selectDebitCard() {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        return new DebitCardPage(validator);
    }

    @Override
    public CardPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
