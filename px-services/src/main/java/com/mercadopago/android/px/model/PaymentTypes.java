package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class PaymentTypes {

    public static String CREDIT_CARD = "credit_card";

    public static String DEBIT_CARD = "debit_card";

    public static String PREPAID_CARD = "prepaid_card";

    public static String TICKET = "ticket";

    public static String ATM = "atm";

    public static String DIGITAL_CURRENCY = "digital_currency";

    public static String BANK_TRANSFER = "bank_transfer";

    @Deprecated
    public static String ACCOUNT_MONEY = "account_money";

    public static String PLUGIN = "payment_method_plugin";

    public static final String PAYMENT_METHOD = "payment_method";

    private PaymentTypes() {
    }

    public static boolean isPaymentMethodType(@NonNull final String type) {
        return PAYMENT_METHOD.equals(type);
    }

    public static boolean isCardPaymentType(String paymentType) {
        return PaymentTypes.CREDIT_CARD.equals(paymentType) ||
            PaymentTypes.DEBIT_CARD.equals(paymentType) ||
            PaymentTypes.PREPAID_CARD.equals(paymentType);
    }

    @Deprecated
    public static boolean isAccountMoney(String type) {
        return PaymentTypes.ACCOUNT_MONEY.equals(type);
    }

    public static boolean isPlugin(String type) {
        return PaymentTypes.ACCOUNT_MONEY.equals(type)
            || PaymentTypes.PLUGIN.equals(type);
    }

    public static List<String> getAllPaymentTypes() {
        return new ArrayList<String>() {{
            add(CREDIT_CARD);
            add(DEBIT_CARD);
            add(PREPAID_CARD);
            add(TICKET);
            add(ATM);
            add(DIGITAL_CURRENCY);
            add(BANK_TRANSFER);
            add(ACCOUNT_MONEY);
        }};
    }

    public static boolean isCreditCardPaymentType(final String paymentTypeId) {
        return PaymentTypes.CREDIT_CARD.equals(paymentTypeId);
    }
}
