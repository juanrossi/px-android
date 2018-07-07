package com.mercadopago;

import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import com.mercadopago.android.px.testcheckout.assertions.DefaultValidator;
import com.mercadopago.android.px.testcheckout.flows.CreditCardTestFlow;
import com.mercadopago.android.px.testcheckout.idleresources.CheckoutResource;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.input.Country;
import com.mercadopago.android.px.testcheckout.input.FakeCard;
import com.mercadopago.android.px.testcheckout.input.Visa;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.NamePage;
import com.mercadopago.android.testlib.HttpResource;
import com.mercadopago.example.R;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleTest {

    @Rule
    public HttpResource httpResource = new CheckoutResource();

    @Rule
    public ActivityTestRule<CheckoutExampleActivity> mActivityRule =
        new ActivityTestRule<>(CheckoutExampleActivity.class);

    @Before
    public void setUp() {
        //Go to checkout.
        Matcher<View> startCho = withId(R.id.continueButton);
        onView(startCho).check(matches(isDisplayed()));
        onView(startCho).perform(click());
    }

    @Test
    public void withValidVisaCreditCardFlowIsOk() {
        CreditCardTestFlow creditCardTestFlow = new CreditCardTestFlow();
        Card card = new Visa(FakeCard.CardState.APRO, Country.ARGENTINA);

        CongratsPage congratsPage =
            creditCardTestFlow.runCreditCardPaymentFlowInstallmentsFirstOption(card, new DefaultValidator() {

                @Override
                public void validate(@NonNull NamePage namePage) {
                    super.validate(namePage);
                    Matcher<View> cardCardholderNameEditTextMatcher = withId(R.id.mpsdkCardholderName);
                    onView(cardCardholderNameEditTextMatcher)
                        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
                }
            });

        assertNotNull(congratsPage);
    }
}