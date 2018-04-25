package com.mercadopago.preferences;

import com.mercadopago.lite.exceptions.CheckoutPreferenceException;
import com.mercadopago.model.Item;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Sites;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CheckoutPreferenceTest {

    ///////////////////PAYMENTS_TYPES tests///////////////////
    @Test
    public void testWhenValidatePreferenceWithAllPaymentsTypesExcludedReturnFalse() {
        CheckoutPreference preference = getPreferenceWithAllPaymentTypesExcluded();
        assertFalse(preference.validPaymentTypeExclusion());
    }

    @Test
    public void testWhenValidatePreferenceWithSomePaymentsTypesExcludedReturnTrue() {
        CheckoutPreference preference = getPreferenceWithSomePaymentTypesExcluded();
        assertTrue(preference.validPaymentTypeExclusion());
    }

    ///////////////////INSTALLMENTS tests///////////////////
    @Test
    public void testWhenValidatePreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumberReturnFalse() {
        CheckoutPreference preference = getPreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumber();
        assertFalse(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumberReturnFalse() {
        CheckoutPreference preference = getPreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumber();
        assertFalse(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithMaxInstallmentsNumberPositiveReturnTrue() {
        CheckoutPreference preference = getPreferenceWithPositiveInstallmentsNumber();
        assertTrue(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithNegativeMaxInstallmentsNumberReturnFalse() {
        CheckoutPreference preference = getPreferenceWithNegativeInstallmentsNumbers();
        assertFalse(preference.validInstallmentsPreference());
    }

    ///////////////////EXCEPTIONS tests///////////////////

    @Test
    public void testWhenValidatePreferenceValidNoThrowExceptionReturnTrue() {
        CheckoutPreference preference = getPreferenceWithOneItemValidActiveAndSomePaymentTypesExcluded();
        Boolean valid = true;

        try {
            preference.validate();
        } catch (CheckoutPreferenceException e) {
            valid = false;
        } finally {
            assertTrue(valid);
        }
    }

    @Test
    public void testWhenValidatePreferenceWithAllPaymentTypesExcludedThrowExceptionReturnTrue() {
        CheckoutPreference preference = getPreferenceWithOneItemValidActiveButAllPaymentTypesExcluded();

        try {
            preference.validate();
        } catch (CheckoutPreferenceException e) {
            assertTrue(e.getErrorCode() == CheckoutPreferenceException.EXCLUDED_ALL_PAYMENT_TYPES);
        }
    }

    @Test
    public void testWhenValidatePreferenceWithInstallmentsDefaultNumberAndInstallmentsNumberNegativeThrowExceptionReturnTrue() {
        CheckoutPreference preference = getPreferenceWithOneItemValidButInstallmenstsDefaultNumberAndInstallmentsNumberNegative();

        try {
            preference.validate();
        } catch (CheckoutPreferenceException e) {
            assertTrue(e.getErrorCode() == CheckoutPreferenceException.INVALID_INSTALLMENTS);
        }
    }

    @Test
    public void testWhenValidatePreferenceWithPreferenceExpiredThrowExceptionReturnTrue() {
        CheckoutPreference preference = getPreferenceWithOneItemValidButPreferenceExpired();

        try {
            preference.validate();
        } catch (CheckoutPreferenceException e) {
            assertTrue(e.getErrorCode() == CheckoutPreferenceException.EXPIRED_PREFERENCE);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testWhenValidatePreferenceWithNoItemsThrowException() {
        new CheckoutPreference.Builder()
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .build();
    }

    ///////////////////DATES tests///////////////////
    @Test
    public void testWhenPreferenceIsActiveReturnTrue() {
        CheckoutPreference preference = getActivePreference();
        assertTrue(preference.isActive());
    }

    @Test
    public void testWhenPreferenceIsNotActiveReturnFalse() {
        CheckoutPreference preference = getInactivePreference();
        assertFalse(preference.isActive());
    }

    @Test
    public void testWhenPreferenceIsNotExpiredReturnFalse() {
        CheckoutPreference preference = getNotExpiredPreference();
        assertFalse(preference.isExpired());
    }

    @Test
    public void testWhenPreferenceIsExpiredReturnTrue() {
        CheckoutPreference preference = getExpiredPreference();

        assertTrue(preference.isExpired());
    }

    @Test
    public void testWhenValidatePreferenceWithNullExpirationDateToReturnFalse() {
        CheckoutPreference preference = getPreferenceWithNullExpirationDateTo();
        assertFalse(preference.isExpired());
    }

    ///////////////////Getters preferences with different DATES///////////////////
    private CheckoutPreference getActivePreference() {
        Date pastDate = new Date();
        pastDate.setTime((new Date().getTime()) - 1000 * 60 * 60);

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(new Item("item", 1, BigDecimal.ONE))
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setActiveFrom(pastDate)
                .build();

        return preference;
    }

    private CheckoutPreference getInactivePreference() {
        GregorianCalendar calendar = new GregorianCalendar(2100, 3, 3); //Date should be after that today
        Date date = calendar.getTime();
        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(new Item("item", 1, BigDecimal.ONE))
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setActiveFrom(date)
                .build();
        return preference;
    }

    private CheckoutPreference getNotExpiredPreference() {
        GregorianCalendar calendar = new GregorianCalendar(2100, 7, 3); //Date should be after that today
        Date date = calendar.getTime();
        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(new Item("item", 1, BigDecimal.ONE))
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setActiveFrom(date)
                .build();
        return preference;
    }

    private CheckoutPreference getExpiredPreference() {
        GregorianCalendar calendar = new GregorianCalendar(2015, 3, 3); //Date should be before that today
        Date date = calendar.getTime();
        CheckoutPreference preference = new CheckoutPreference.Builder()
                .setPayerEmail("test@gmail.com")
                .addItem(new Item("item", 1, BigDecimal.ONE))
                .setSite(Sites.ARGENTINA)
                .setExpirationDate(date)
                .build();
        return preference;
    }

    private CheckoutPreference getPreferenceWithNullExpirationDateTo() {
        CheckoutPreference preference = new CheckoutPreference.Builder()
                .setPayerEmail("test@gmail.com")
                .addItem(new Item("item", 1, BigDecimal.ONE))
                .setSite(Sites.ARGENTINA)
                .build();
        return preference;
    }

    ///////////////////Getters preferences with different PAYMENTS_TYPES///////////////////
    private CheckoutPreference getPreferenceWithAllPaymentTypesExcluded() {

        ArrayList<String> paymentTypes = new ArrayList<>();
        Item item = new Item("123", BigDecimal.ONE);

        paymentTypes.addAll(PaymentTypes.getAllPaymentTypes());

        item.setUnitPrice(new BigDecimal(2));

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(item)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .addExcludedPaymentTypes(paymentTypes)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithSomePaymentTypesExcluded() {

        Item itemA = new Item("123", BigDecimal.ONE);
        itemA.setCurrencyId("USD");


        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .addExcludedPaymentType(PaymentTypes.CREDIT_CARD)
                .addExcludedPaymentType(PaymentTypes.DEBIT_CARD)
                .addExcludedPaymentType(PaymentTypes.PREPAID_CARD)
                .build();

        return preference;
    }

    ///////////////////Getters preferences with different INSTALLMENT///////////////////
    private CheckoutPreference getPreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumber() {
        Item itemA = new Item("123", BigDecimal.ONE);

        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setMaxInstallments(1)
                .setDefaultInstallments(-3)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumber() {

        Item itemA = new Item("123", BigDecimal.ONE);
        itemA.setCurrencyId("USD");

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("email@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setMaxInstallments(-1)
                .setDefaultInstallments(3)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithPositiveInstallmentsNumber() {

        Item itemA = new Item("123", BigDecimal.ONE);
        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setMaxInstallments(1)
                .setDefaultInstallments(3)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithNegativeInstallmentsNumbers() {
        Item itemA = new Item("123", BigDecimal.ONE);
        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setMaxInstallments(-1)
                .setDefaultInstallments(-1)
                .build();

        return preference;
    }

    ///////////////////Getters preferences with different EXCEPTIONS///////////////////
    private CheckoutPreference getPreferenceWithOneItemValidActiveAndSomePaymentTypesExcluded() {

        ArrayList<String> paymentTypes = new ArrayList<>();
        Item itemA = new Item("123", BigDecimal.ONE);

        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        Date pastDate = new Date();
        pastDate.setTime((new Date().getTime()) - 1000 * 60 * 60);

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .addExcludedPaymentType(PaymentTypes.CREDIT_CARD)
                .addExcludedPaymentType(PaymentTypes.DEBIT_CARD)
                .addExcludedPaymentType(PaymentTypes.PREPAID_CARD)
                .setActiveFrom(pastDate)
                .setMaxInstallments(1)
                .setDefaultInstallments(1)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithOneItemValidActiveButAllPaymentTypesExcluded() {

        ArrayList<String> paymentTypes = new ArrayList<>();
        Item itemA = new Item("123", BigDecimal.ONE);

        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        Date pastDate = new Date();
        pastDate.setTime((new Date().getTime()) - 1000 * 60 * 60);

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .addExcludedPaymentTypes(PaymentTypes.getAllPaymentTypes())
                .setActiveFrom(pastDate)
                .build();

        return preference;
    }


    private CheckoutPreference getPreferenceWithOneItemValidButInstallmenstsDefaultNumberAndInstallmentsNumberNegative() {

        Item itemA = new Item("123", BigDecimal.ONE);

        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        Date pastDate = new Date();
        pastDate.setTime((new Date().getTime()) - 1000 * 60 * 60);

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setMaxInstallments(-1)
                .setDefaultInstallments(-3)
                .setActiveFrom(pastDate)
                .build();

        return preference;
    }

    private CheckoutPreference getPreferenceWithOneItemValidButPreferenceExpired() {
        Item itemA = new Item("123", BigDecimal.ONE);

        itemA.setUnitPrice(new BigDecimal(2));
        itemA.setCurrencyId("USD");

        GregorianCalendar calendar = new GregorianCalendar(2015, 3, 3); //Date should be before that today
        Date date = calendar.getTime();

        CheckoutPreference preference = new CheckoutPreference.Builder()
                .addItem(itemA)
                .setPayerEmail("test@gmail.com")
                .setSite(Sites.ARGENTINA)
                .setExpirationDate(date)
                .build();

        return preference;
    }
}