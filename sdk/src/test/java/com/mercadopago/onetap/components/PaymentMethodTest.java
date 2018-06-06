package com.mercadopago.onetap.components;

import com.mercadopago.mocks.Cards;
import com.mercadopago.mocks.PaymentMethodSearchs;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.onetap.OneTap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodTest {

    @Mock
    PaymentMethod.Props mock;

    @Mock
    CardPaymentMetadata cardPaymentMetadata;

    @Mock
    PaymentMethodSearch paymentMethodSearch;

    @Mock
    OneTap.Actions actions;

    private PaymentMethod paymentMethod;

    @Before
    public void setUp() {
        paymentMethod = new PaymentMethod(mock, actions);
    }

    @Test
    public void whenPaymentTypeCardThenRenderMethodCard() {
        when(mock.getPaymentMethodType()).thenReturn(PaymentTypes.DEBIT_CARD);
        when(mock.getPaymentMethodSearch()).thenReturn(paymentMethodSearch);
        when(mock.getCard()).thenReturn(cardPaymentMetadata);
        assertTrue(paymentMethod.resolveComponent() instanceof MethodCard);
    }

    @Test
    public void whenPaymentTypePluginThenRenderMethodPlugin() {
        when(mock.getPaymentMethodType()).thenReturn(PaymentTypes.PLUGIN);
        assertTrue(paymentMethod.resolveComponent() instanceof MethodPlugin);
    }

    @Test(expected = IllegalStateException.class)
    public void whenPaymentTypeOtherThenThrows() throws IllegalArgumentException {
        when(mock.getPaymentMethodType()).thenReturn(PaymentTypes.ATM);
        paymentMethod.resolveComponent();
    }

}