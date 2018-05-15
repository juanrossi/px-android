package com.mercadopago.presenters;

import android.support.annotation.NonNull;

import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.lite.exceptions.CheckoutPreferenceException;
import com.mercadopago.mocks.Cards;
import com.mercadopago.mocks.Customers;
import com.mercadopago.mocks.Discounts;
import com.mercadopago.mocks.Installments;
import com.mercadopago.mocks.Issuers;
import com.mercadopago.mocks.PaymentMethodSearchs;
import com.mercadopago.mocks.PaymentMethods;
import com.mercadopago.mocks.Payments;
import com.mercadopago.mocks.Tokens;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.Cause;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Identification;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.plugins.DataInitializationTask;
import com.mercadopago.plugins.PaymentMethodPlugin;
import com.mercadopago.plugins.PaymentProcessor;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.providers.CheckoutProvider;
import com.mercadopago.util.TextUtils;
import com.mercadopago.views.CheckoutView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mercadopago.core.MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE;
import static com.mercadopago.core.MercadoPagoCheckout.PAYMENT_RESULT_CODE;
import static com.mercadopago.plugins.PaymentProcessor.PAYMENT_PROCESSOR_KEY;
import static com.mercadopago.utils.StubCheckoutPreferenceUtils.stubExpiredPreference;
import static com.mercadopago.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItem;
import static com.mercadopago.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItemAndPayer;
import static com.mercadopago.utils.StubCheckoutPreferenceUtils.stubPreferenceWithAccessToken;
import static com.mercadopago.utils.StubPaymentResult.stubApprovedOffPaymentResult;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPresenterTest {

    private static final String PREF_ID = "123";

    private static final int REQUESTED_RESULT = 0x20;

    @Mock
    private MercadoPagoCheckout mercadoPagoCheckout;

    @Mock
    private CheckoutView checkoutView;

    @Mock
    private CheckoutProvider checkoutProvider;

    private MockedView view;
    private MockedProvider provider;

    @Before
    public void setUp() {
        view = new MockedView();
        provider = new MockedProvider();
    }

    @NonNull
    private CheckoutPresenter getPresenter(int resultCode) {
        return getBasePresenter(resultCode, checkoutView, checkoutProvider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenterWithDefaultFlowPreferenceMla() {
        CheckoutPreference preference = stubPreferenceOneItem();
        FlowPreference flowPreference = new FlowPreference.Builder()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setCheckoutPreferenceResponse(preference);
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        return getBasePresenter(PAYMENT_RESULT_CODE, view, provider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenter(final FlowPreference flowPreference) {
        CheckoutPreference preference = stubPreferenceOneItem();
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        return getBasePresenter(PAYMENT_RESULT_CODE, view, provider);
    }

    @NonNull
    private CheckoutPresenter getBasePresenter(int resultCode, CheckoutView view, CheckoutProvider provider) {
        CheckoutPresenter.PersistentDataModel model = CheckoutPresenter.PersistentDataModel.createWith(resultCode, mercadoPagoCheckout);
        CheckoutPresenter presenter = new CheckoutPresenter(model);
        presenter.attachResourcesProvider(provider);
        presenter.attachView(view);
        return presenter;
    }

    @Test
    public void whenChoHasPrefIdSetRetrievePreferenceFromMercadoPago() {
        when(mercadoPagoCheckout.getPreferenceId()).thenReturn(PREF_ID);
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.initialize();
        verify(checkoutProvider).getCheckoutPreference(any(String.class), any(TaggedCallback.class));
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenChoHasCompletePrefSetDoNotCallProviderToGetPreference() {
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItemAndPayer());
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.initialize();
        verify(checkoutProvider).fetchFonts();
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenPreferenceIsExpiredThenShowErrorInView() {
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubExpiredPreference());
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.initialize();
        verify(checkoutProvider).getCheckoutExceptionMessage(any(CheckoutPreferenceException.class));
        verify(checkoutView).showError(any(MercadoPagoError.class));
    }

    @Test
    public void whenChoHasPreferenceAndPaymentMethodRetrivedShowPaymentMethodSelection() {
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItemAndPayer());
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.initialize();

        verify(checkoutView).isActive();
        verify(checkoutView).showProgress();
        verify(checkoutView).initializeMPTracker();
        verify(checkoutView).trackScreen();
        // TODO we assume that plugin init ok.
        // TODO remove, we assume that the request went ok.
        presenter.startFlow();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }


    @Test
    public void whenAPaymentMethodIsSelectedThenShowReviewAndConfirm() {
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItemAndPayer());
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(new FlowPreference.Builder().build());
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        verify(checkoutView).showReviewAndConfirm();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentMethodCanceledThenCancelCheckout() {
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        presenter.onPaymentMethodSelectionCancel();
        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenChoFlowPrefDisableReviewAndConfirmAndPaymentMethodIsSelectedThenFinishWithDataResult() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .build();
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        CheckoutPresenter presenter = getPresenter(PAYMENT_DATA_RESULT_CODE);
        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOff();
        presenter.onPaymentMethodSelectionResponse(paymentMethod, null, null, null, null, null, null);
        //TODO should not be any payment data but equality by hashing is applying
        verify(checkoutView).finishWithPaymentDataResult(any(PaymentData.class), any(boolean.class));
    }

    @Test
    public void ifPaymentRequestedAndReviewConfirmDisabledThenStartPaymentResultScreen() {
        CheckoutPreference preference = stubPreferenceOneItem();
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setPaymentResponse(Payments.getApprovedPayment());

        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.onPaymentMethodSelectionResponse(null,
                null, null, null,
                null, null, null);
        assertTrue(view.showingPaymentResult);
    }

    //
    @Test
    public void whenPaymentRequestedAndOnReviewAndConfirmOkResponseThenCreatePayment() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .build();

        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());

        CheckoutPresenter checkoutPresenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);
        //Real preference, without items
        CheckoutPreference preference = stubPreferenceOneItem();
        provider.setCheckoutPreferenceResponse(preference);
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        provider.setPaymentResponse(Payments.getApprovedPayment());


        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        checkoutPresenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        checkoutPresenter.onPaymentConfirmation();
        assertTrue(provider.paymentRequested);
    }

    @Test
    public void whenPaymentCreatedThenShowResultScreen() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        //Real preference, without items
        provider.setPaymentResponse(Payments.getApprovedPayment());

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertTrue(view.showingPaymentResult);
    }

    @Test
    public void onPaymentResultScreenResponseThenFinishWithPaymentResponse() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        Payment payment = Payments.getApprovedPayment();
        provider.setPaymentResponse(payment);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        presenter.onPaymentConfirmation();

        //On Payment Result Screen
        assertEquals(view.paymentFinalResponse, null);

        presenter.onPaymentResultResponse();

        assertEquals(view.paymentFinalResponse.getId(), payment.getId());
    }


    @Test
    public void whenDiscountDisabledThenDoNotMakeDiscountsAPICall() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableDiscount()
                .build();
        CheckoutPreference preference = stubPreferenceOneItem();
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setCheckoutPreferenceResponse(preference);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        Payment payment = Payments.getApprovedPayment();
        provider.setPaymentResponse(payment);
        presenter.initialize();

        assertFalse(provider.campaignsRequested);
    }

    @Test
    public void whenPaymentCreatedAndResultScreenDisabledThenFinishWithPaymentResponse() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disablePaymentResultScreen()
                .build();

        CheckoutPresenter presenter = getPaymentPresenter(flowPreference);

        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());

        Payment payment = Payments.getApprovedPayment();
        provider.setPaymentResponse(payment);
        presenter.initialize();
        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertEquals(view.paymentFinalResponse.getId(), payment.getId());
    }


    @Test
    public void whenApprovedPaymentCreatedAndApprovedResultScreenDisabledThenFinishWithPaymentResponse() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disablePaymentApprovedScreen()
                .build();
        CheckoutPresenter presenter = getPaymentPresenter(flowPreference);

        Payment payment = Payments.getApprovedPayment();
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        provider.setPaymentResponse(payment);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertEquals(view.paymentFinalResponse.getId(), payment.getId());
    }

    @Test
    public void whenApprovedPaymentCreatedAndCongratsDisplayIsZeroThenFinishWithPaymentResponse() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .setCongratsDisplayTime(0)
                .build();
        CheckoutPresenter presenter = getPaymentPresenter(flowPreference);
        Payment payment = Payments.getApprovedPayment();
        provider.setPaymentResponse(payment);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        presenter.onPaymentConfirmation();
        assertEquals(view.paymentFinalResponse.getId(), payment.getId());
    }

    @Test
    public void whenRejectedPaymentCreatedAndRejectedResultScreenDisabledThenFinishWithPaymentResponse() {

        FlowPreference flowPreference = new FlowPreference.Builder()
                .disablePaymentRejectedScreen()
                .build();

        CheckoutPresenter presenter = getPaymentPresenter(flowPreference);
        Payment rejectedPayment = Payments.getRejectedPayment();
        provider.setPaymentResponse(rejectedPayment);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertEquals(view.paymentFinalResponse.getId(), rejectedPayment.getId());
    }

    @Test
    public void whenPendingPaymentCreatedAndPendingResultScreenDisabledThenFinishWithPaymentResponse() {
        FlowPreference flowPreference = new FlowPreference.Builder()
                .disablePaymentPendingScreen()
                .build();

        CheckoutPresenter presenter = getPaymentPresenter(flowPreference);

        Payment pendingPayment = Payments.getPendingPayment();
        provider.setPaymentResponse(pendingPayment);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertEquals(view.paymentFinalResponse.getId(), pendingPayment.getId());
    }


    // Forwarded flows
    @Test
    public void whenPaymentDataSetThenStartRyCScreen() {
        CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(PaymentMethods.getPaymentMethodOff());

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getPaymentData()).thenReturn(paymentData);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(new FlowPreference.Builder().build());

        CheckoutPresenter presenter = getBasePresenter(REQUESTED_RESULT, view, provider);

        provider.setCampaignsResponse(Discounts.getCampaigns());
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());

        presenter.initialize();

        assertFalse(view.showingPaymentMethodSelection);
        assertTrue(view.showingReviewAndConfirm);
    }


    @Test
    public void whenPaymentResultSetThenStartResultScreen() {
        CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(stubApprovedOffPaymentResult());
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .build());

        provider.setCampaignsResponse(Discounts.getCampaigns());
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());

        CheckoutPresenter presenter = getBasePresenter(REQUESTED_RESULT, view, provider);

        presenter.initialize();

        assertFalse(view.showingPaymentMethodSelection);
        assertFalse(view.showingReviewAndConfirm);
        assertTrue(view.showingPaymentResult);
    }

    @Test
    public void whenPaymentResultSetAndUserLeavesScreenThenRespondWithoutPayment() {
        CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(stubApprovedOffPaymentResult());
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(new FlowPreference.Builder()
                .build());

        provider.setCampaignsResponse(Discounts.getCampaigns());
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());

        CheckoutPresenter presenter = getBasePresenter(REQUESTED_RESULT, view, provider);

        presenter.initialize();
        assertTrue(view.showingPaymentResult);

        presenter.onPaymentResultResponse();

        assertTrue(view.finishedCheckoutWithoutPayment);
    }

    @Test
    public void ifPaymentRecoveryRequiredThenStartPaymentRecoveryFlow() {
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        provider.setPaymentResponse(Payments.getCallForAuthPayment());
        CheckoutPresenter presenter = getPaymentPresenter(new FlowPreference.Builder().build());
        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();
        assertTrue(view.showingPaymentResult);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        assertTrue(view.showingPaymentRecoveryFlow);
        assertEquals(view.paymentRecoveryRequested.getPaymentMethod().getId(), paymentMethod.getId());
    }

    @Test
    public void onTokenRecoveryFlowOkResponseThenCreatePayment() {

        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        provider.setPaymentResponse(Payments.getCallForAuthPayment());

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getVisaToken();

        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();
        assertTrue(view.showingPaymentResult);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        assertTrue(view.showingPaymentRecoveryFlow);
        assertEquals(view.paymentRecoveryRequested.getPaymentMethod().getId(), paymentMethod.getId());

        presenter.onCardFlowResponse(paymentMethod, issuer, payerCost, token, null);
        assertTrue(view.showingPaymentResult);

        assertTrue(paymentMethod.getId().equals(provider.paymentMethodPaid.getId()));
    }

    @Test
    public void ifPaymentRecoveryRequiredWithInvalidPaymentMethodThenShowError() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        provider.setPaymentResponse(Payments.getCallForAuthPayment());

        presenter.initialize();

        presenter.onPaymentMethodSelectionResponse(PaymentMethods
                .getPaymentMethodOff(), null, null, null, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();
        assertTrue(view.showingPaymentResult);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        assertTrue(view.showingError);
    }

    //Backs
    @Test
    public void ifCheckoutInitiatedAndUserPressesBackCancelCheckout() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        presenter.initialize();
        assertTrue(view.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionCancel();
        assertTrue(view.checkoutCanceled);
    }

    //
    @Ignore
    @Test
    public void ifReviewAndConfirmShownAndUserPressesBackThenRestartPaymentMethodSelection() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        presenter.initialize();
        assertTrue(view.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onReviewAndConfirmCancel();
        assertTrue(view.showingPaymentMethodSelection);
    }

    @Test
    public void ifPaymentRecoveryShownAndUserPressesBackThenRestartPaymentMethodSelection() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        provider.setPaymentResponse(Payments.getCallForAuthPayment());
        presenter.initialize();
        assertTrue(view.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOnVisa(),
                Issuers.getIssuers().get(0),
                Installments.getInstallments().getPayerCosts().get(0), Tokens.getVisaToken(), null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();
        assertTrue(view.showingPaymentResult);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        assertTrue(view.showingPaymentRecoveryFlow);
        presenter.onCardFlowCancel();
        assertTrue(view.showingPaymentMethodSelection);
    }

    @Test
    public void ifPaymentMethodEditionRequestedAndUserPressesBackTwiceCancelCheckout() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();

        presenter.initialize();
        assertTrue(view.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        assertTrue(view.showingReviewAndConfirm);

        presenter.changePaymentMethod();
        assertTrue(view.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(view.showingReviewAndConfirm);

        presenter.onReviewAndConfirmCancel();
        assertTrue(view.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(view.checkoutCanceled);
    }

    //Payment tests
    @Test
    public void whenPaymentCreationRequestedThenGenerateTransactionId() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        provider.setPaymentResponse(Payments.getApprovedPayment());

        presenter.initialize();

        //Payment method off, no issuer, installments or token
        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();

        assertTrue(provider.paymentRequested);
        assertFalse(TextUtils.isEmpty(provider.transactionId));
    }

    @Test
    public void whenCustomerAvailableAndPaymentCreationRequestedThenCreatePaymentWithCustomerId() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        provider.setPaymentResponse(Payments.getApprovedPayment());
        provider.setCustomerResponse(Customers.getCustomerWithCards());
        presenter.initialize();

        //Payment method off, no issuer, installments or token
        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        assertTrue(view.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();

        assertTrue(provider.paymentRequested);
        assertFalse(TextUtils.isEmpty(provider.paymentCustomerId));
    }


    @Test
    public void ifPaymentResultApprovedSetAndTokenWithESCAndESCEnabledThenSaveESC() {
        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with card Id and ESC
        Token token = Tokens.getTokenWithESC();

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .build();

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(1234L)
                .setPaymentStatus(Payment.StatusCodes.STATUS_APPROVED)
                .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_ACCREDITED)
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(paymentResult);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        assertTrue(provider.saveESCRequested);
    }

    @Test
    public void ifPaymentResultApprovedSetAndESCEnabledButTokenHasNoESCThenDontSaveESC() {

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token without card id or ESC
        Token token = Tokens.getVisaToken();

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .build();

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(1234L)
                .setPaymentStatus(Payment.StatusCodes.STATUS_APPROVED)
                .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_ACCREDITED)
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(paymentResult);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();


        assertFalse(provider.saveESCRequested);
    }

    @Test
    public void ifPaymentResultApprovedSetAndESCEnabledThenShowPaymentResultScreen() {

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with card Id and ESC
        Token token = Tokens.getTokenWithESC();

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .build();

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(1234L)
                .setPaymentStatus(Payment.StatusCodes.STATUS_APPROVED)
                .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_ACCREDITED)
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(paymentResult);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);
        presenter.initialize();

        assertTrue(view.showingPaymentResult);
    }

    @Test
    public void onCreatePaymentWithESCTokenErrorThenDeleteESC() {
        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        provider.setCheckoutPreferenceResponse(checkoutPreference);
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());

        ApiException apiException = Payments.getInvalidESCPayment();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .disableReviewAndConfirmScreen()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setPaymentResponse(mpException);


        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getTokenWithESC();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertTrue(provider.paymentRequested);

        Cause cause = provider.failedResponse.getApiException().getCause().get(0);
        assertEquals(cause.getCode(), ApiException.ErrorCodes.INVALID_PAYMENT_WITH_ESC);
        assertTrue(provider.deleteESCRequested);
    }

    @Test
    public void onCreatePaymentWithESCTokenErrorThenRequestSecurityCode() {

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        ApiException apiException = Payments.getInvalidESCPayment();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setPaymentResponse(mpException);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .disableReviewAndConfirmScreen()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getTokenWithESC();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertTrue(provider.paymentRequested);

        provider.paymentRequested = false;

        assertTrue(view.showingPaymentRecoveryFlow);
        PaymentRecovery paymentRecovery = view.paymentRecoveryRequested;
        assertTrue(paymentRecovery.isStatusDetailInvalidESC());
        assertTrue(paymentRecovery.isTokenRecoverable());

        //Response from Card Vault with new Token
        presenter.onCardFlowResponse(paymentMethod, issuer, payerCost, token, null);
        assertTrue(provider.paymentRequested);

        provider.setPaymentResponse(Payments.getApprovedPayment());
        assertNotNull(provider.paymentResponse);

    }

    @Test
    public void createPaymentWithESCTokenThenSaveESC() {

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        provider.setPaymentResponse(Payments.getApprovedPayment());

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .disableReviewAndConfirmScreen()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);

        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getTokenWithESC();

        Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, mockedCard, null);

        //Response from Review And confirm
        assertTrue(provider.paymentRequested);
        assertNotNull(provider.paymentResponse);
        assertTrue(provider.saveESCRequested);
    }

    @Test
    public void ifPaymentResultInvalidESCSetAndESCEnabledThenDontSaveESC() {
        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token without card id or ESC
        Token token = Tokens.getVisaToken();

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .build();

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(1234L)
                .setPaymentStatus(Payment.StatusCodes.STATUS_REJECTED)
                .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC)
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(paymentResult);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        assertFalse(provider.saveESCRequested);
    }

    @Test
    public void ifPaymentResultInvalidESCSetAndESCEnabledThenDeleteESCSaved() {

        CheckoutPreference checkoutPreference = stubPreferenceWithAccessToken();
        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with Card ID (because it was created with ESC enabled)
        Token token = Tokens.getTokenWithESC();

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .enableESC()
                .build();

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(1234L)
                .setPaymentStatus(Payment.StatusCodes.STATUS_REJECTED)
                .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC)
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        when(mercadoPagoCheckout.getPaymentResult()).thenReturn(paymentResult);
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        assertTrue(provider.deleteESCRequested);
    }

    @Test
    public void ifPayerDataCollectedAndPayerInPreferenceThenUseBothForPayment() {
        String firstName = "FirstName";
        String lastName = "LastName";
        Identification identification = new Identification();
        identification.setType("cpf");
        identification.setNumber("111");

        Payer collectedPayer = new Payer();
        collectedPayer.setFirstName(firstName);
        collectedPayer.setLastName(lastName);
        collectedPayer.setIdentification(identification);

        provider.setPaymentResponse(Payments.getCallForAuthPayment());
        CheckoutPreference preference = stubPreferenceOneItem();
        FlowPreference flowPreference = new FlowPreference.Builder()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setCheckoutPreferenceResponse(preference);
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);

        presenter.initialize();

        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, collectedPayer);
        presenter.onPaymentConfirmation();

        assertEquals(provider.payerPosted.getEmail(), preference.getPayer().getEmail());
        assertEquals(provider.payerPosted.getAccessToken(), preference.getPayer().getAccessToken());
        assertEquals(provider.payerPosted.getFirstName(), firstName);
        assertEquals(provider.payerPosted.getLastName(), lastName);
        assertEquals(provider.payerPosted.getIdentification().getType(), identification.getType());
        assertEquals(provider.payerPosted.getIdentification().getNumber(), identification.getNumber());
    }

    @Test
    public void ifOnlyPayerFromPreferenceThenUseItForPayment() {
        CheckoutPreference preference = stubPreferenceWithAccessToken();
        FlowPreference flowPreference = new FlowPreference.Builder()
                .build();

        when(mercadoPagoCheckout.getCheckoutPreference()).thenReturn(preference);
        when(mercadoPagoCheckout.getFlowPreference()).thenReturn(flowPreference);
        provider.setCheckoutPreferenceResponse(preference);
        provider.setPaymentMethodSearchResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        provider.setPaymentResponse(Payments.getCallForAuthPayment());

        CheckoutPresenter presenter = getBasePresenter(PAYMENT_RESULT_CODE, view, provider);
        presenter.initialize();

        presenter.onPaymentMethodSelectionResponse(PaymentMethods.getPaymentMethodOff(), null, null, null, null, null, null);
        presenter.onPaymentConfirmation();
        assertEquals(provider.payerPosted.getEmail(), preference.getPayer().getEmail());
        assertEquals(provider.payerPosted.getAccessToken(), preference.getPayer().getAccessToken());
    }

    @Test
    public void onIdentificationInvalidAndErrorShownThenGoBackToPaymentMethodSelection() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        ApiException apiException = Payments.getInvalidIdentificationPayment();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setPaymentResponse(mpException);

        presenter.initialize();

        presenter.onErrorCancel(mpException);
        assertTrue(view.showingPaymentMethodSelection);
    }

    @Test
    public void createPaymentWithInvalidIdentificationThenShowError() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();

        ApiException apiException = Payments.getInvalidIdentificationPayment();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setPaymentResponse(mpException);

        presenter.initialize();

        PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer issuer = Issuers.getIssuers().get(0);
        PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        Token token = Tokens.getTokenWithESC();

        //Response from payment method selection
        presenter.onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, null, null, null);

        //Response from Review And confirm
        presenter.onPaymentConfirmation();
        assertTrue(provider.paymentRequested);

        Cause cause = provider.failedResponse.getApiException().getCause().get(0);
        assertEquals(cause.getCode(), ApiException.ErrorCodes.INVALID_IDENTIFICATION_NUMBER);
        assertTrue(view.showingError);
    }

    @Test
    public void ifNotNewFlowThenDoNotTrackInit() {
        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(PaymentMethods.getPaymentMethodOff());
        when(mercadoPagoCheckout.getPaymentData()).thenReturn(paymentData);
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        presenter.initialize();
        assertFalse(view.initTracked);
    }

    @Test
    public void ifNewFlowThenDoTrackInit() {
        CheckoutPresenter presenter = getPaymentPresenterWithDefaultFlowPreferenceMla();
        presenter.initialize();
        assertTrue(view.initTracked);
    }

    @Test
    public void whenIsDiscountEnabledAndNotHasDiscountThenShouldGetDiscountTrue() {
        CheckoutPresenter presenter = getPresenterWithFlowDiscount(true);
        assertTrue(presenter.shouldRetrieveDiscount());
    }

    @Test
    public void whenIsDiscountEnabledAndHasDiscountThenShouldGetDiscountFalse() {
        when(mercadoPagoCheckout.getDiscount()).thenReturn(new Discount());
        CheckoutPresenter presenter = getPresenterWithFlowDiscount(true);
        assertFalse(presenter.shouldRetrieveDiscount());
    }

    @Test
    public void whenIsNotDiscountEnabledAndNotHasDiscountThenShouldGetDiscountFalse() {
        CheckoutPresenter presenter = getPresenterWithFlowDiscount(false);
        boolean shouldGetDiscount = presenter.shouldRetrieveDiscount();
        assertFalse(shouldGetDiscount);
    }

    @Test
    public void whenIsDiscountEnabledAndHasPaymentMethodPluginThenShouldGetDiscountFalse() {
        CheckoutStore store = CheckoutStore.getInstance();
        List<PaymentMethodPlugin> paymentMethodPlugins = new ArrayList<>();

        PaymentMethodPlugin paymentMethodPlugin = mock(PaymentMethodPlugin.class);
        paymentMethodPlugins.add(paymentMethodPlugin);

        store.setPaymentMethodPluginList(paymentMethodPlugins);
        store.getData().put(DataInitializationTask.KEY_INIT_SUCCESS, true);

        when(paymentMethodPlugin.isEnabled(store.getData())).thenReturn(true);
        CheckoutPresenter presenter = getPresenterWithFlowDiscount(true);

        boolean shouldGetDiscount = presenter.shouldRetrieveDiscount();

        assertFalse(shouldGetDiscount);
    }

    @Test
    public void whenIsDiscountEnabledAndHasPaymentPluginThenShouldGetDiscountFalse() {
        CheckoutStore store = CheckoutStore.getInstance();
        PaymentProcessor paymentProcessor = mock(PaymentProcessor.class);
        store.addPaymentPlugins(paymentProcessor, PAYMENT_PROCESSOR_KEY);
        CheckoutPresenter presenter = getPresenterWithFlowDiscount(true);
        boolean shouldGetDiscount = presenter.shouldRetrieveDiscount();
        assertFalse(shouldGetDiscount);
    }

    private CheckoutPresenter getPresenterWithFlowDiscount(boolean enabled) {
        FlowPreference flowPreference = mock(FlowPreference.class);
        when(flowPreference.isDiscountEnabled()).thenReturn(enabled);
        return getPaymentPresenter(flowPreference);
    }

    private static class MockedView implements CheckoutView {

        private MercadoPagoError errorShown;
        private boolean showingError = false;
        private boolean showingPaymentMethodSelection = false;
        private boolean showingReviewAndConfirm = false;
        private boolean initTracked = false;
        private PaymentData paymentDataFinalResponse;
        private boolean showingPaymentResult = false;
        private boolean checkoutCanceled = false;
        private Payment paymentFinalResponse;
        private boolean finishedCheckoutWithoutPayment = false;
        private boolean showingPaymentRecoveryFlow = false;
        private PaymentRecovery paymentRecoveryRequested;

        @Override
        public void fetchImageFromUrl(String url) {
            //Do nothing
        }

        @Override
        public void showBusinessResult(final BusinessPaymentModel model) {
            //Do nothing
        }

        @Override
        public void showError(MercadoPagoError error) {
            this.showingError = true;
            this.errorShown = error;
        }

        @Override
        public void showProgress() {
            //Do nothing
        }

        @Override
        public void showReviewAndConfirm() {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentMethodSelection() {
            showingPaymentMethodSelection = true;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void startPaymentMethodEdition() {
            showingPaymentMethodSelection = true;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentResult(PaymentResult paymentResult) {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = true;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void backToReviewAndConfirm() {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void backToPaymentMethodSelection() {
            showingPaymentMethodSelection = true;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void finishWithPaymentResult() {
            finishedCheckoutWithoutPayment = true;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode) {

        }

        @Override
        public void finishWithPaymentResult(Payment payment) {
            paymentFinalResponse = payment;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode, Payment payment) {

        }

        @Override
        public void finishWithPaymentDataResult(PaymentData paymentData, Boolean paymentMethodEdited) {
            paymentDataFinalResponse = paymentData;
        }

        @Override
        public void cancelCheckout() {
            checkoutCanceled = true;
        }

        @Override
        public void cancelCheckout(MercadoPagoError mercadoPagoError) {

        }

        @Override
        public void cancelCheckout(final Integer customResultCode, final Boolean paymentMethodEdited) {

        }

        @Override
        public void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery) {
            paymentRecoveryRequested = paymentRecovery;
            showingPaymentRecoveryFlow = true;
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
        }

        @Override
        public void initializeMPTracker() {

        }

        @Override
        public void trackScreen() {
            initTracked = true;
        }

        @Override
        public void finishFromReviewAndConfirm() {

        }

        @Override
        public void showHook(Hook hook, int requestCode) {

        }

        @Override
        public void showPaymentProcessor() {

        }

        @Override
        public boolean isActive() {
            return true;
        }
    }

    public class MockedProvider implements CheckoutProvider {

        private boolean campaignsRequested = false;

        private List<Campaign> campaigns;
        private boolean checkoutPreferenceRequested = false;
        private CheckoutPreference preference;
        private boolean paymentMethodSearchRequested = false;
        private PaymentMethodSearch paymentMethodSearchResponse;
        private Payment paymentResponse;
        private boolean paymentRequested;
        private Customer customerResponse;
        private boolean saveESCRequested = false;
        private boolean deleteESCRequested = false;

        private String transactionId;
        private String paymentCustomerId;
        private PaymentMethod paymentMethodPaid;
        private Payer payerPosted;

        private boolean shouldFail = false;
        private MercadoPagoError failedResponse;

        @Override
        public void fetchFonts() {

        }

        @Override
        public void getCheckoutPreference(String checkoutPreferenceId, TaggedCallback<CheckoutPreference> taggedCallback) {
            checkoutPreferenceRequested = true;
            taggedCallback.onSuccess(preference);
        }

        @Override
        public void getDiscountCampaigns(TaggedCallback<List<Campaign>> callback) {
            this.campaignsRequested = true;
            callback.onSuccess(campaigns);
        }

        @Override
        public void getDirectDiscount(BigDecimal amount, String payerEmail, TaggedCallback<Discount> taggedCallback) {
            taggedCallback.onSuccess(null);
        }

        @Override
        public void getPaymentMethodSearch(BigDecimal amount, List<String> excludedPaymentTypes, List<String> excludedPaymentMethods, Payer payer, Site site, TaggedCallback<PaymentMethodSearch> onPaymentMethodSearchRetrievedCallback, TaggedCallback<Customer> onCustomerRetrievedCallback) {
            this.paymentMethodSearchRequested = true;
            onPaymentMethodSearchRetrievedCallback.onSuccess(paymentMethodSearchResponse);
            if (customerResponse != null) {
                onCustomerRetrievedCallback.onSuccess(customerResponse);
            }
        }

        @Override
        public String getCheckoutExceptionMessage(CheckoutPreferenceException exception) {
            return null;
        }

        @Override
        public String getCheckoutExceptionMessage(IllegalStateException exception) {
            return null;
        }

        @Override
        public void createPayment(String transactionId, CheckoutPreference checkoutPreference, PaymentData paymentData, Boolean binaryMode, String customerId, TaggedCallback<Payment> taggedCallback) {
            this.paymentMethodPaid = paymentData.getPaymentMethod();
            this.transactionId = transactionId;
            this.paymentCustomerId = customerId;
            this.paymentRequested = true;
            this.payerPosted = paymentData.getPayer();
            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(paymentResponse);
            }
        }

        @Override
        public boolean saveESC(String cardId, String value) {
            this.saveESCRequested = true;
            return saveESCRequested;
        }

        @Override
        public void deleteESC(String cardId) {
            this.deleteESCRequested = true;
        }

        public void setCampaignsResponse(List<Campaign> campaigns) {
            this.campaigns = campaigns;
        }

        public void setCheckoutPreferenceResponse(CheckoutPreference preference) {
            this.preference = preference;
        }

        public void setPaymentMethodSearchResponse(PaymentMethodSearch paymentMethodSearchResponse) {
            this.paymentMethodSearchResponse = paymentMethodSearchResponse;
        }

        public void setPaymentResponse(Payment paymentResponse) {
            this.paymentResponse = paymentResponse;
        }

        public void setCustomerResponse(Customer customerResponse) {
            this.customerResponse = customerResponse;
        }

        public void setPaymentResponse(MercadoPagoError error) {
            this.shouldFail = true;
            this.failedResponse = error;
        }
    }
}
