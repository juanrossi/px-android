package com.mercadopago.android.px.internal.datasource;

import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.InstallmentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmountServiceTest {

    @Mock private ChargeRepository chargeRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private CheckoutPreference checkoutPreference;
    @Mock private InstallmentRepository installmentRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private Discount discount;

    private AmountService amountService;

    @Before
    public void setUp() {
        amountService =
            new AmountService(paymentSettingRepository, chargeRepository, installmentRepository, discountRepository);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getTotalAmount()).thenReturn(BigDecimal.TEN);
    }

    @Test
    public void whenHasDiscountAndNoChargesAmountThenGetAmountToPayIsAmountLessDiscount() {
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.ZERO);
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ZERO);
        when(discount.getCouponAmount()).thenReturn(BigDecimal.ONE);
        when(discountRepository.getDiscount()).thenReturn(discount);
        assertEquals(BigDecimal.TEN.subtract(BigDecimal.ONE), amountService.getAmountToPay());
    }

    @Test
    public void whenHasNoDiscountAndNoChargesAmountThenGetAmountToPayIsJustAmount() {
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.ZERO);
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.TEN, amountService.getAmountToPay());
    }

    @Test
    public void whenHasNoDiscountAndHasChargesAmountThenGetAmountToPayIsAmountPlusChargesAmount() {
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.ZERO);
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ONE);
        assertEquals(BigDecimal.TEN.add(BigDecimal.ONE), amountService.getAmountToPay());
    }

    @Test
    public void whenHasDiscountAndHasChargesAmountThenGetAmountToPayIsAmountLessDiscountAndPlusChargesAmount() {
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.ZERO);
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ONE);
        when(discount.getCouponAmount()).thenReturn(BigDecimal.ONE);
        when(discountRepository.getDiscount()).thenReturn(discount);
        assertEquals(BigDecimal.TEN, amountService.getAmountToPay());
    }

    @Test
    public void whenGetItemsAmountThenReturnTotalAmount() {
        assertEquals(BigDecimal.TEN, amountService.getItemsAmount());
    }

    @Test
    public void whenHasChargesAmountThenGetItemsAmountPlusCharges() {
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ONE);
        assertEquals(BigDecimal.TEN.add(BigDecimal.ONE), amountService.getItemsPlusCharges());
    }

    @Test
    public void whenHasNoChargesAmountThenGetItemsAmount() {
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.TEN, amountService.getItemsPlusCharges());
    }

    @Test
    public void whenGetAppliedChargesAndNoCardChargesReturnOnlyChargesByPaymentMethod() {
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.TEN);
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.TEN, amountService.getAppliedCharges());
    }

    @Test
    public void whenGetAppliedChargesAndCardChargesReturnSumOfThem() {
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ONE);
        when(checkoutPreference.getTotalAmount()).thenReturn(BigDecimal.ONE);
        when(discountRepository.getDiscount()).thenReturn(null);
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.TEN);
        assertEquals(new BigDecimal("9"), amountService.getAppliedCharges());
    }

    @Test
    public void whenGetAmountToPayChargesAndCardChargesReturnCardTotal() {
        when(chargeRepository.getChargeAmount()).thenReturn(BigDecimal.ONE);
        when(checkoutPreference.getTotalAmount()).thenReturn(BigDecimal.ONE);
        when(discountRepository.getDiscount()).thenReturn(null);
        when(installmentRepository.getInstallmentTotalAmount()).thenReturn(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, amountService.getAmountToPay());
    }
}