package com.mercadopago.testcheckout.pages;

import android.support.test.espresso.action.ViewActions;
import android.view.View;

import com.mercadopago.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.testlib.pages.PageObject;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class IdentificationPage extends PageObject<CheckoutValidator> {

    public IdentificationPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public IdentificationPage(CheckoutValidator validator) {
        super(validator);
    }

    // Case credit card - a way to resolve this is with a card type
    public InstallmentsPage enterIdentificationNumberToInstallments(final String idNumber) {
        insertIdAndPressNext(idNumber);
        return new InstallmentsPage(validator);
    }

    // Case debit card - a way to resolve this is with a card type

    public ReviewAndConfirmPage enterIdentificationNumberToReviewAndConfirm(final String idNumber) {
        insertIdAndPressNext(idNumber);
        return new ReviewAndConfirmPage(validator);
    }

    public IssuerPage enterIdentificationNumberToIssuer(final String idNumber) {
        insertIdAndPressNext(idNumber);
        return new IssuerPage(validator);
    }

    private void insertIdAndPressNext(final String idNumber) {
        Matcher<View> cardIdentificationNumberEditTextMatcher = withId(com.mercadopago.R.id.mpsdkCardIdentificationNumber);
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.R.id.mpsdkNextButtonText);
        onView(cardIdentificationNumberEditTextMatcher).perform(typeText(idNumber));
        onView(cardNextButtonTextMatcher).perform(click());
    }

    @Override
    public IdentificationPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public NoCheckoutPage pressBackWithExclusions() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new NoCheckoutPage(validator);
    }

    public PaymentMethodPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new PaymentMethodPage(validator);
    }

    public SecurityCodePage pressPrevious() {
        onView(withId(com.mercadopago.R.id.mpsdkBackButton)).perform(click());
        return new SecurityCodePage(validator);
    }
}
