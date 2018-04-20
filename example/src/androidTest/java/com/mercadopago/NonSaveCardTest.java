package com.mercadopago;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.testcheckout.CheckoutResource;
import com.mercadopago.testcheckout.flows.CheckoutTestFlow;
import com.mercadopago.testcheckout.input.Amex;
import com.mercadopago.testcheckout.input.Card;
import com.mercadopago.testcheckout.input.Country;
import com.mercadopago.testcheckout.input.FakeCard;
import com.mercadopago.testcheckout.input.Maestro;
import com.mercadopago.testcheckout.input.Master;
import com.mercadopago.testcheckout.input.Visa;
import com.mercadopago.testcheckout.input.VisaDebit;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testlib.HttpResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NonSaveCardTest {
    @Rule
    public HttpResource httpResource = new CheckoutResource();

    @Rule
    public ActivityTestRule<CheckoutExampleActivity> activityRule =
            new ActivityTestRule<>(CheckoutExampleActivity.class);

    private CheckoutTestFlow checkoutTestFlow;

    @Before
    public void setUp() {
        MercadoPagoCheckout.Builder builder = new MercadoPagoCheckout.Builder();
        builder.setCheckoutPreference(new CheckoutPreference("243966003-d0be0be0-6fd8-4769-bf2f-7f2d979655f5"));
        builder.setPublicKey("APP_USR-648a260d-6fd9-4ad7-9284-90f22262c18d");
        builder.setActivity(activityRule.getActivity());
        checkoutTestFlow = CheckoutTestFlow.createFlowWithCheckout(builder);
    }

    @Test
    public void withValidVisaCreditCardNoInstallmentsFlowIsOk() {
        Card card = new Visa(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowInstallmentsFirstOption(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidMasterCreditCardNoInstallmentsFlowIsOk() {
        Card card = new Master(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowInstallmentsFirstOption(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidAmericanExpressCreditCardNoInstallmentsFlowIsOk() {
        Card card = new Amex(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowWithInstallments(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidVisaCreditCardInstallmentsFlowIsOk() {
        Card card = new Visa(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowWithInstallments(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidMasterCreditCardInstallmentsFlowIsOk() {
        Card card = new Master(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowWithInstallments(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidAmericanExpressCreditCardInstallmentsFlowIsOk() {
        Card card = new Amex(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runCreditCardPaymentFlowWithInstallments(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidMaestroDebitCardFlowIsOk() {
        Card card = new Maestro(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runDebitCardPaymentFlow(card);
        assertNotNull(congratsPage);
    }

    @Test
    public void withValidVisaDebitCardFlowIsOk() {
        Card card = new VisaDebit(FakeCard.CardState.APRO, Country.ARGENTINA);
        CongratsPage congratsPage = checkoutTestFlow.runDebitCardPaymentFlow(card);
        assertNotNull(congratsPage);
    }
}
