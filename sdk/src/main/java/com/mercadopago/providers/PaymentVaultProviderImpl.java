package com.mercadopago.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mercadopago.BuildConfig;
import com.mercadopago.R;
import com.mercadopago.core.CustomServer;
import com.mercadopago.core.MercadoPagoServicesAdapter;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.lite.callbacks.Callback;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.model.Card;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentMethodSearchItem;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.tracker.Tracker;
import com.mercadopago.tracking.tracker.MPTracker;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.MercadoPagoESC;
import com.mercadopago.util.MercadoPagoESCImpl;
import com.mercadopago.util.TextUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentVaultProviderImpl implements PaymentVaultProvider {

    private final Context context;
    private final MercadoPagoServicesAdapter mercadoPago;
    private final String merchantBaseUrl;
    private final String merchantGetCustomerUri;
    private final Map<String, String> merchantGetCustomerAdditionalInfo;
    private final MercadoPagoESC mercadoPagoESC;
    private final String merchantPublicKey;

    public PaymentVaultProviderImpl(Context context, String publicKey, String privateKey, String merchantBaseUrl,
                                    String merchantGetCustomerUri, Map<String, String> merchantGetCustomerAdditionalInfo, boolean escEnabled) {
        this.context = context;
        this.merchantBaseUrl = merchantBaseUrl;
        this.merchantGetCustomerUri = merchantGetCustomerUri;
        this.merchantGetCustomerAdditionalInfo = merchantGetCustomerAdditionalInfo;
        this.mercadoPagoESC = new MercadoPagoESCImpl(context, escEnabled);
        this.merchantPublicKey = publicKey;

        mercadoPago = new MercadoPagoServicesAdapter(context, publicKey, privateKey);
    }

    @Override
    public void getDirectDiscount(String amount, String payerEmail, final TaggedCallback<Discount> taggedCallback) {
        mercadoPago.getDirectDiscount(amount, payerEmail, taggedCallback);
    }

    @Override
    public void getPaymentMethodSearch(final BigDecimal amount, final PaymentPreference paymentPreference,
        final Payer payer, final Site site, final List<String> cardsWithEsc, final List<String> supportedPlugins,
        final TaggedCallback<PaymentMethodSearch> taggedCallback) {

        final List<String> excludedPaymentTypes =
            paymentPreference == null ? null : paymentPreference.getExcludedPaymentTypes();
        final List<String> excludedPaymentMethodIds =
            paymentPreference == null ? null : paymentPreference.getExcludedPaymentMethodIds();

        mercadoPago.getPaymentMethodSearch(amount, excludedPaymentTypes, excludedPaymentMethodIds, cardsWithEsc,
            supportedPlugins, payer, site,
            new Callback<PaymentMethodSearch>() {
                @Override
                public void success(@NonNull final PaymentMethodSearch paymentMethodSearch) {
                    if (!paymentMethodSearch.hasSavedCards() && isMerchantServerCustomerAvailable()) {
                        addCustomerCardsFromMerchantServer(paymentMethodSearch, paymentPreference,
                            taggedCallback);
                    } else {
                        taggedCallback.onSuccess(paymentMethodSearch);
                    }
                }

                @Override
                public void failure(ApiException apiException) {
                    taggedCallback
                        .onFailure(new MercadoPagoError(apiException, ApiUtil.RequestOrigin.GET_PAYMENT_METHODS));
                }
            });
    }

    private void addCustomerCardsFromMerchantServer(final PaymentMethodSearch paymentMethodSearch,
                                                    final PaymentPreference paymentPreference,
                                                    final TaggedCallback<PaymentMethodSearch> taggedCallback) {
        CustomServer.getCustomer(context, merchantBaseUrl, merchantGetCustomerUri, merchantGetCustomerAdditionalInfo,
                new Callback<Customer>() {
                    @Override
                    public void success(Customer customer) {
                        List<Card> savedCards = paymentPreference == null ? customer.getCards()
                                : paymentPreference.getValidCards(customer.getCards());
                        paymentMethodSearch.setCards(savedCards, context.getString(R.string.mpsdk_last_digits_label));
                        taggedCallback.onSuccess(paymentMethodSearch);
                    }

                    @Override
                    public void failure(ApiException apiException) {
                        //Avoid failure caused by merchant's server
                        taggedCallback.onSuccess(paymentMethodSearch);
                    }
                });
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.mpsdk_title_activity_payment_vault);
    }

    @Override
    public String getInvalidSiteConfigurationErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_currency);
    }

    @Override
    public String getInvalidAmountErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_amount);
    }

    @Override
    public String getAllPaymentTypesExcludedErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_excluded_all_payment_type);
    }

    @Override
    public String getInvalidDefaultInstallmentsErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_default_installments);
    }

    @Override
    public String getInvalidMaxInstallmentsErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_max_installments);
    }

    @Override
    public String getStandardErrorMessage() {
        return context.getString(R.string.mpsdk_standard_error_message);
    }

    @Override
    public String getEmptyPaymentMethodsErrorMessage() {
        return context.getString(R.string.mpsdk_no_payment_methods_found);
    }

    private boolean isMerchantServerCustomerAvailable() {
        return !TextUtil.isEmpty(merchantBaseUrl) && !TextUtil.isEmpty(merchantGetCustomerUri);
    }

    public void initializeMPTracker(String siteId) {
        MPTracker.getInstance().initTracker(merchantPublicKey, siteId, BuildConfig.VERSION_NAME, context);
    }

    public void trackInitialScreen(PaymentMethodSearch paymentMethodSearch, String siteId) {
        initializeMPTracker(siteId);
        Tracker.trackPaymentVaultScreen(context, merchantPublicKey, paymentMethodSearch, mercadoPagoESC.getESCCardIds());
    }

    public void trackChildrenScreen(@NonNull final PaymentMethodSearchItem paymentMethodSearchItem, final @NonNull String siteId) {
        initializeMPTracker(siteId);
        Tracker.trackPaymentVaultChildrenScreen(context, merchantPublicKey, paymentMethodSearchItem);
    }

    @Override
    public List<String> getCardsWithEsc() {
        return new ArrayList<>(mercadoPagoESC.getESCCardIds());
    }
}
