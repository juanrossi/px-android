package com.mercadopago.android.px;

import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodSelectionHandlerTest {

    @Mock private GroupsRepository groupsRepository;
    @Mock private PaymentMethodSelectionHandler.Callback callback;
    @Mock private CheckoutPreference checkoutPreference;

    private PaymentMethodSelectionHandler handler;

    @Before
    public void setUp() {
        handler = new PaymentMethodSelectionHandler(groupsRepository, checkoutPreference);
    }

    @Test
    public void whenPaymentMethodIsCardAndCardIdIsNullThenAutomaticSelectionDriveToPaymentVault() {
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.DEBIT_CARD);
        final PaymentMethodSelectionHandler paymentMethodSelectionHandler =
            new PaymentMethodSelectionHandler(paymentMethod, null);
        paymentMethodSelectionHandler.run(callback);
        verify(callback).driveToPaymentVault();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void whenPaymentMethodIsNotCardAndAnyCardIdThenAutomaticSelectionDriveToPaymentVault() {
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.ATM);
        final PaymentMethodSelectionHandler
            paymentMethodSelectionHandler = new PaymentMethodSelectionHandler(paymentMethod, null);
        paymentMethodSelectionHandler.run(callback);
        verify(callback).driveToPaymentVault();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void whenPaymentMethodIsCardAndCardIdIsNotNullThenAutomaticSelectionDriveToCardVault() {
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.DEBIT_CARD);
        final PaymentMethodSelectionHandler
            paymentMethodSelectionHandler = new PaymentMethodSelectionHandler(paymentMethod, "1234");
        paymentMethodSelectionHandler.run(callback);
        verify(callback).driveToCardVault();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void whenPaymentMethodIsCardAndCardIdIsNotNullButIsNotValidThenAutomaticSelectionDriveToPaymentVault() {
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.DEBIT_CARD);
        final PaymentMethodSelectionHandler
            paymentMethodSelectionHandler = new PaymentMethodSelectionHandler(paymentMethod, "4321");
        paymentMethodSelectionHandler.run(callback);
        verify(callback).driveToPaymentVault();
        verifyNoMoreInteractions(callback);
    }
}
