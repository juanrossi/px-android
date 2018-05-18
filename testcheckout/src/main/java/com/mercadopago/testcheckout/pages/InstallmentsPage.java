package com.mercadopago.testcheckout.pages;


import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;

import com.mercadopago.R;
import com.mercadopago.testcheckout.assertions.Validator;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class InstallmentsPage extends PageObject {

    public InstallmentsPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public InstallmentsPage(Validator validator) {
        super(validator);
    }

    @Override
    protected void validate() {
        validator.validate(this);
    }

    public ReviewAndConfirmPage selectInstallments(int installmentsOption) {

        Matcher<View> InstallmentsRecyclerViewMatcher = withId(R.id.mpsdkActivityInstallmentsView);

        onView(InstallmentsRecyclerViewMatcher)
                .perform(RecyclerViewActions.actionOnItemAtPosition(installmentsOption, click()));

        return new ReviewAndConfirmPage(validator);
    }
}
