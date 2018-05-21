package com.mercadopago.testcheckout.pages;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.mercadopago.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class IssuerPage extends PageObject<CheckoutValidator> {

    public IssuerPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    protected IssuerPage(CheckoutValidator validator) {
        super(validator);
    }

    public ReviewAndConfirmPage enterBankOptionToReviewAndConfirm(final int bankOption) {
        selectOption(bankOption);
        return new ReviewAndConfirmPage(validator);
    }

    public InstallmentsPage enterBankOptionToInstallments(final int backOption) {
        selectOption(backOption);
        return new InstallmentsPage(validator);
    }

    private void selectOption(final int bankOption) {
        ViewInteraction recyclerView = onView(withId(com.mercadopago.R.id.mpsdkActivityIssuersView));
        recyclerView.perform(scrollToPosition(bankOption));
        recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition(bankOption, click()));
    }

    @Override
    public IssuerPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

}
