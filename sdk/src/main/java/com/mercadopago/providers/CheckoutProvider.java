package com.mercadopago.providers;

import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.lite.exceptions.CheckoutPreferenceException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Payer;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.ResourcesProvider;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.preferences.CheckoutPreference;
import java.math.BigDecimal;
import java.util.List;

public interface CheckoutProvider extends ResourcesProvider {
    void getCheckoutPreference(String checkoutPreferenceId, TaggedCallback<CheckoutPreference> taggedCallback);

    void getDiscountCampaigns(TaggedCallback<List<Campaign>> callback);

    void getDirectDiscount(BigDecimal amount, String payerEmail, TaggedCallback<Discount> taggedCallback);

    void getPaymentMethodSearch(BigDecimal amount, final List<String> excludedPaymentTypes,
        final List<String> excludedPaymentMethods, final List<String> cardsWithEsc,
        final List<String> supportedPlugins, final Payer payer, final Site site,
        final TaggedCallback<PaymentMethodSearch> onPaymentMethodSearchRetrievedCallback,
        final TaggedCallback<Customer> onCustomerRetrievedCallback);

    String getCheckoutExceptionMessage(CheckoutPreferenceException exception);

    String getCheckoutExceptionMessage(IllegalStateException exception);

    void createPayment(String transactionId,
                       CheckoutPreference checkoutPreference,
                       PaymentData paymentData,
                       Boolean binaryMode,
                       String customerId,
                       TaggedCallback<Payment> taggedCallback);

    List<String> getCardsWithEsc();

    void fetchFonts();

    /**
     * Resolve ESC for transaction - delete it if needed
     *
     * @param paymentData the payment information
     * @param paymentStatus the payment status
     * @param paymentStatusDetail the payment detail related with the status
     * @return isInvalidEsc
     */
    boolean manageEscForPayment(final PaymentData paymentData,
        final String paymentStatus,
        final String paymentStatusDetail);

    /**
     * Resolve ESC for transaction - delete it if needed.
     *
     * @param paymentData the payment information
     * @param error the payment error
     * @return isInvalidEsc
     */
    boolean manageEscForError(final MercadoPagoError error, final PaymentData paymentData);
}
