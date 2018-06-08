package com.mercadopago.onetap;

import com.mercadopago.model.Card;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.OneTapMetadata;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Token;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.OneTapModel;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OneTapPresenterTest {

    private static final String CARD_ID = "visa";
    private static final String PLUGIN_ID = "account_money";
    private static final String OTHER_ID = "other";

    @Mock
    private OneTapModel model;

    @Mock
    private Card card;

    @Mock
    private PaymentMethodSearch paymentMethodSearch;

    @Mock
    private OneTapMetadata metadata;

    @Mock
    private OneTap.View view;

    @Mock
    private PaymentMethod paymentMethod;

    @Mock
    private CardPaymentMetadata cardMetadata;

    @Mock
    private BigDecimal transactionAmount;

    private OneTapPresenter oneTapPresenter;

    @Before
    public void setUp() {
        when(metadata.getCard()).thenReturn(cardMetadata);
        when(model.getPaymentMethods()).thenReturn(paymentMethodSearch);
        when(paymentMethodSearch.getOneTapMetadata()).thenReturn(metadata);
        when(model.getTransactionAmount()).thenReturn(transactionAmount);

        oneTapPresenter = new OneTapPresenter(model);
        oneTapPresenter.attachView(view);
    }

    @Test
    public void whenConfirmPaymentCardShowCardFlow() {
        cardConfig();
        oneTapPresenter.confirmPayment();
        verify(view).showCardFlow(model, card);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenConfirmPaymentPluginShowPaymentPluginFlow() {
        configPlugin();
        oneTapPresenter.confirmPayment();
        verify(view).showPaymentFlowPlugin(PaymentTypes.PLUGIN, PLUGIN_ID, transactionAmount);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenConfirmPaymentUnknownTypeShowPaymentFlow() {
        configOther();
        oneTapPresenter.confirmPayment();
        verify(view).showPaymentFlow(paymentMethod);
        verifyNoMoreInteractions(view);
    }

    private void configOther() {
        when(metadata.getPaymentTypeId()).thenReturn(PaymentTypes.ATM);
        when(metadata.getPaymentMethodId()).thenReturn(OTHER_ID);
        when(paymentMethodSearch.getPaymentMethodById(OTHER_ID)).thenReturn(paymentMethod);
    }

    private void configPlugin() {
        when(metadata.getPaymentTypeId()).thenReturn(PaymentTypes.PLUGIN);
        when(metadata.getPaymentMethodId()).thenReturn(PLUGIN_ID);
    }

    private void cardConfig() {
        when(metadata.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(metadata.getPaymentMethodId()).thenReturn(CARD_ID);

        when(paymentMethodSearch.getCardById(CARD_ID)).thenReturn(card);
        when(cardMetadata.getId()).thenReturn(CARD_ID);

        when(paymentMethodSearch.getIssuer(CARD_ID)).thenReturn(mock(Issuer.class));
    }

    @Test
    public void whenAnyTokenReceivedThenShowCardPaymentFlow() {
        cardConfig();
        oneTapPresenter.onReceived(mock(Token.class));
        verify(view).showPaymentFlow(any(CardPaymentModel.class));
    }

    @Test
    public void changePaymentMethod() {
        oneTapPresenter.changePaymentMethod();
        verify(view).changePaymentMethod();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void onAmountShowMore() {
        oneTapPresenter.onAmountShowMore();
        verify(view).showDetailModal(model);
        verifyNoMoreInteractions(view);
    }
}