package com.mercadopago.android.px.testcheckout.pages;

import com.mercadopago.android.px.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CallForAuthPage extends PageObject<CheckoutValidator> {

    public CallForAuthPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CallForAuthPage(CheckoutValidator validator) {
        super(validator);
    }

    public SecurityCodeToResultsPage pressAlreadyAuthorizedButton(){
        onView(withId(R.id.paymentResultBodyErrorAction)).perform(click());
        return new SecurityCodeToResultsPage(validator);
    }

    public PaymentMethodPage pressChangePaymentMethodButton() {
        onView(withId(R.id.px_button_primary)).perform(scrollTo(), click());
        return new PaymentMethodPage(validator);
    }

    @Override
    public CallForAuthPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public NoCheckoutPage pressCancelButton() {
        onView(withText(R.string.px_cancel_payment)).perform(scrollTo(), click());
        return new NoCheckoutPage(validator);
    }
}
