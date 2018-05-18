package com.mercadopago.testcheckout.pages;


import android.support.test.espresso.contrib.RecyclerViewActions;
import com.mercadopago.R;
import com.mercadopago.testcheckout.assertions.Validator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


public class PaymentMethodPage extends PageObject {

    public PaymentMethodPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public PaymentMethodPage(Validator validator) {
        super(validator);
    }

    public CardPage selectCard() {
        onView(withId(R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new CardPage(validator);
    }


    public CashPage selectCash() {
        onView(withId(R.id.mpsdkGroupsList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        return new CashPage(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }
}
