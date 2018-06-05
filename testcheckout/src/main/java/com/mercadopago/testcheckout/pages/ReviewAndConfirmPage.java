package com.mercadopago.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.test.espresso.action.ViewActions;
import com.mercadopago.R;
import com.mercadopago.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.testlib.pages.PageObject;
import com.mercadopago.testlib.utils.NestedScroll;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ReviewAndConfirmPage extends PageObject<CheckoutValidator> {

    public ReviewAndConfirmPage() {
    }

    protected ReviewAndConfirmPage(final CheckoutValidator validator) {
        super(validator);
    }

    public CongratsPage pressConfirmButton() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new CongratsPage(validator);
    }

    @Override
    public ReviewAndConfirmPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public NoCheckoutPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new NoCheckoutPage(validator);
    }

    @NonNull
    public PaymentMethodPage clickChangePaymentMethod() {
        onView(withText(R.string.mpsdk_change_payment)).perform(NestedScroll.nestedScrollTo()).perform(click());
        return new PaymentMethodPage(validator);
    }
}
