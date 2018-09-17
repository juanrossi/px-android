package com.mercadopago.android.px.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Installment;
import com.mercadopago.android.px.model.Instructions;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.SavedCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public final class StaticMock {

    // * Card token
    public final static String DUMMY_CARD_NUMBER = "4444444444440008";
    public final static String DUMMY_CARDHOLDER_NAME = "JOHN";
    public final static int DUMMY_EXPIRATION_MONTH = 11;
    public final static int DUMMY_EXPIRATION_YEAR_SHORT = 25;
    public final static int DUMMY_EXPIRATION_YEAR_LONG = 2025;
    public final static String DUMMY_IDENTIFICATION_NUMBER = "12345678";
    public final static String DUMMY_IDENTIFICATION_TYPE = "DNI";
    public final static String DUMMY_SECURITY_CODE = "123";

    // * Identification type
    public final static String DUMMI_IDENTIFICATION_TYPE_ID = "DNI";
    public final static String DUMMI_IDENTIFICATION_TYPE_NAME = "DNI";
    public final static String DUMMI_IDENTIFICATION_TYPE_TYPE = "number";
    public final static Integer DUMMI_IDENTIFICATION_TYPE_MIN_LENGTH = 7;
    public final static Integer DUMMI_IDENTIFICATION_TYPE_MAX_LENGTH = 8;

    // * Saved cards token
    public final static String DUMMY_CARD_ID = "11";

    private StaticMock() {
    }

    public static CardToken getCardToken() {

        return new CardToken(DUMMY_CARD_NUMBER, DUMMY_EXPIRATION_MONTH,
            DUMMY_EXPIRATION_YEAR_SHORT, DUMMY_SECURITY_CODE, DUMMY_CARDHOLDER_NAME,
            DUMMY_IDENTIFICATION_TYPE, DUMMY_IDENTIFICATION_NUMBER);
    }


    public static SavedCardToken getSavedCardToken() {

        return new SavedCardToken(DUMMY_CARD_ID, DUMMY_SECURITY_CODE);
    }

    public static PaymentMethod getPaymentMethod(Context context) {

        return getPaymentMethod(context, "");
    }

    public static PaymentMethod getPaymentMethod(Context context, String flavor) {

        return JsonUtil.getInstance()
            .fromJson(getFile(context, "mocks/payment_method_on" + flavor + ".json"), PaymentMethod.class);
    }

    public static IdentificationType getIdentificationType() {

        return new IdentificationType(DUMMI_IDENTIFICATION_TYPE_ID, DUMMI_IDENTIFICATION_TYPE_NAME,
            DUMMI_IDENTIFICATION_TYPE_TYPE, DUMMI_IDENTIFICATION_TYPE_MIN_LENGTH,
            DUMMI_IDENTIFICATION_TYPE_MAX_LENGTH);
    }

    public static PayerCost getPayerCostWithInterests() {
        return JsonUtil.getInstance()
            .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/payerCostWithInterest.json"),
                PayerCost.class);
    }

    public static Issuer getIssuer() {
        return JsonUtil.getInstance()
            .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/issuer.json"), Issuer.class);
    }

    public static String getInstallmentsJson() {

        try {
            return getFile(InstrumentationRegistry.getContext(), "mocks/installments.json");
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getIssuersJson() {

        try {
            return getFile(InstrumentationRegistry.getContext(), "mocks/issuers.json");
        } catch (Exception ex) {
            return null;
        }
    }


    public static String getPayerCostsJson() {

        try {
            return getFile(InstrumentationRegistry.getContext(), "mocks/payerCosts.json");
        } catch (Exception ex) {
            return null;
        }
    }

    public static Token getToken() {

        try {
            return JsonUtil.getInstance()
                .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/token.json"), Token.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Customer getCustomer(int cardsAmount) {
        try {
            if (cardsAmount == 1) {
                return JsonUtil.getInstance()
                    .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/customer_one_card.json"),
                        Customer.class);
            } else if (cardsAmount == 2) {
                return JsonUtil.getInstance()
                    .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/customer_two_cards.json"),
                        Customer.class);
            } else if (cardsAmount == 3) {
                return JsonUtil.getInstance()
                    .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/customer_three_cards.json"),
                        Customer.class);
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static List<Card> getCards() {

        try {
            Customer customer = JsonUtil.getInstance()
                .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/customer_three_cards.json"),
                    Customer.class);
            return customer.getCards();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Card getCard() {

        try {
            List<Card> cards = getCards();
            return cards.get(0);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Payment getPayment(Context context) {

        try {
            return JsonUtil.getInstance().fromJson(getFile(context, "mocks/payment.json"), Payment.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Payment getPayment() {

        try {
            return JsonUtil.getInstance()
                .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/payment.json"), Payment.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getPaymentMethodSearchWithoutCustomOptionsAsJson() {
        try {
            return getFile(InstrumentationRegistry.getContext(), "mocks/payment_method_search_all.json");
        } catch (Exception ex) {
            return null;
        }
    }



    public static PaymentMethodSearch getPaymentMethodSearchWithUniqueItemCreditCard() {
        try {
            return JsonUtil.getInstance().fromJson(getFile(InstrumentationRegistry.getContext(),
                "mocks/payment_method_search_unique_item_credit_card.json"), PaymentMethodSearch.class);
        } catch (Exception ex) {
            return null;
        }
    }



    public static PaymentMethod getPaymentMethodOn() {
        try {
            return JsonUtil.getInstance()
                .fromJson(getFile(InstrumentationRegistry.getContext(), "mocks/payment_method_on.json"),
                    PaymentMethod.class);
        } catch (Exception ex) {

            return null;
        }
    }


    public static Instructions getInstructions() {
        try {
            String json = getFile(InstrumentationRegistry.getContext(), "mocks/instructions_many.json");
            return JsonUtil.getInstance().fromJson(json, Instructions.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String getFile(Context context, String fileName) {

        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer);
        } catch (Exception e) {

            return "";
        }
    }

}
