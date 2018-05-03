package com.mercadopago.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.mercadopago.CheckoutActivity;
import com.mercadopago.callbacks.CallbackHolder;
import com.mercadopago.hooks.CheckoutHooks;
import com.mercadopago.lite.controllers.CustomServicesHandler;
import com.mercadopago.model.Discount;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.plugins.DataInitializationTask;
import com.mercadopago.plugins.PaymentMethodPlugin;
import com.mercadopago.plugins.PaymentProcessor;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import com.mercadopago.tracker.FlowHandler;
import com.mercadopago.uicontrollers.FontCache;
import com.mercadopago.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class MercadoPagoCheckout {

    //TODO will make them public or internal in order to use always the same constants
    // region constants bundle
    private static final String MERCHANT_PUBLIC_KEY = "merchantPublicKey";
    private static final String PAYMENT_DATA = "paymentData";
    private static final String CHECKOUT_PREFERENCE = "checkoutPreference";
    private static final String SERVICE_PREFERENCE = "servicePreference";
    private static final String FLOW_PREFERENCE = "flowPreference";
    private static final String PAYMENT_RESULT_SCREEN_PREFERENCE = "paymentResultScreenPreference";
    private static final String PAYMENT_RESULT = "paymentResult";
    private static final String DISCOUNT = "discount";
    private static final String BINARY_MODE = "binaryMode";
    private static final String RESULT_CODE = "resultCode";
    private static final String EXTRA_PREFERENCE_ID = "extra_preference_id";
    //endregion constants bundle

    public static final int CHECKOUT_REQUEST_CODE = 5;
    public static final int PAYMENT_DATA_RESULT_CODE = 6;
    public static final int PAYMENT_RESULT_CODE = 7;
    public static final int TIMER_FINISHED_RESULT_CODE = 8;
    public static final int PAYMENT_METHOD_CHANGED_REQUESTED = 9;

    public static final String PAYMENT_PROCESSOR_KEY = "payment_processor";


    private final String publicKey;
    private final CheckoutPreference checkoutPreference;
    private final ServicePreference servicePreference;
    private final FlowPreference flowPreference;
    private final PaymentResultScreenPreference paymentResultScreenPreference;
    private final PaymentData paymentData;
    private final PaymentResult paymentResult;
    private final Boolean binaryMode;
    private final Discount discount;
    private final String preferenceId;

    private MercadoPagoCheckout(Builder builder) {
        publicKey = builder.publicKey;
        checkoutPreference = builder.checkoutPreference;
        servicePreference = builder.servicePreference;
        flowPreference = builder.flowPreference;
        paymentResultScreenPreference = builder.paymentResultScreenPreference;
        binaryMode = builder.binaryMode;
        discount = builder.discount;
        paymentResult = builder.paymentResult;
        paymentData = builder.paymentData;
        preferenceId = builder.preferenceId;
        configureCustomServicesHandler(servicePreference);
        configureCheckoutStore(builder);
        configureFlowHandler();
    }

    /**
     * Deprecated - new implementation involves payment processor.
     * <p>
     * Starts checkout experience.
     * When the flows ends it returns a {@link PaymentData} object to finish the payment.
     * will be returned on {@link Activity#onActivityResult(int, int, Intent)} if success or
     * {@link com.mercadopago.exceptions.MercadoPagoError}
     * if something went wrong or canceled.
     *
     * @param context context needed to start checkout.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void startForPaymentData(@NonNull final Context context) {
        //TODO payment result code should not be hardcoded.
        startForResult(context, MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE);
    }

    /**
     * Starts checkout experience.
     * When the flows ends it returns a {@link PaymentResult} object that
     * will be returned on {@link Activity#onActivityResult(int, int, Intent)} if success or
     * {@link com.mercadopago.exceptions.MercadoPagoError}
     * <p>
     * will return on {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @param context context needed to start checkout.
     */
    @SuppressWarnings("unused")
    public void startForPayment(@NonNull final Context context) {
        //TODO payment result code should not be hardcoded.
        startForResult(context, MercadoPagoCheckout.PAYMENT_RESULT_CODE);
    }

    private void configureFlowHandler() {
        //Create flow identifier only for new checkouts
        if (paymentResult == null && paymentData == null) {
            FlowHandler.getInstance().generateFlowId();
        }
    }

    private void configureCheckoutStore(final Builder builder) {
        final CheckoutStore store = CheckoutStore.getInstance();
        store.reset();
        store.setReviewAndConfirmPreferences(builder.reviewAndConfirmPreferences);
        store.setPaymentResultScreenPreference(paymentResultScreenPreference);
        store.setPaymentMethodPluginList(builder.paymentMethodPluginList);
        store.setPaymentPlugins(builder.paymentPlugins);
        store.setCheckoutHooks(builder.checkoutHooks);
        store.setDataInitializationTask(builder.dataInitializationTask);
        store.setCheckoutPreference(builder.checkoutPreference);
    }

    private void configureCustomServicesHandler(ServicePreference servicePreference) {
        CustomServicesHandler.getInstance().clear();
        CustomServicesHandler.getInstance().setServices(servicePreference);
    }

    private void validate(int resultCode) throws IllegalStateException {
        if (isCheckoutTimerAvailable() && isPaymentDataIntegration(resultCode)) {
            throw new IllegalStateException("CheckoutTimer is not available with PaymentData integration");
        }
    }

    private boolean isCheckoutTimerAvailable() {
        return flowPreference != null && flowPreference.isCheckoutTimerEnabled();
    }

    private boolean isPaymentDataIntegration(int resultCode) {
        return resultCode == MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE;
    }

    private void startForResult(@NonNull final Context context, int resultCode) {
        CallbackHolder.getInstance().clean();
        startCheckoutActivity(context, resultCode);
    }

    private void startCheckoutActivity(@NonNull final Context context, int resultCode) {
        validate(resultCode);
        startIntent(context, getIntent(context, resultCode));
    }

    private void startIntent(@NonNull final Context context, @NonNull final Intent checkoutIntent) {
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(checkoutIntent, MercadoPagoCheckout.CHECKOUT_REQUEST_CODE);
        } else {
            context.startActivity(checkoutIntent);
        }
    }

    @NonNull
    private Intent getIntent(@NonNull final Context context, final int resultCode) {
        Intent checkoutIntent = new Intent(context, CheckoutActivity.class);
        checkoutIntent.putExtra(MERCHANT_PUBLIC_KEY, publicKey);
        checkoutIntent.putExtra(PAYMENT_DATA, JsonUtil.getInstance().toJson(paymentData));
        checkoutIntent.putExtra(CHECKOUT_PREFERENCE, JsonUtil.getInstance().toJson(checkoutPreference));
        checkoutIntent.putExtra(EXTRA_PREFERENCE_ID, preferenceId);
        checkoutIntent.putExtra(SERVICE_PREFERENCE, JsonUtil.getInstance().toJson(servicePreference));
        checkoutIntent.putExtra(FLOW_PREFERENCE, JsonUtil.getInstance().toJson(flowPreference));
        checkoutIntent.putExtra(PAYMENT_RESULT_SCREEN_PREFERENCE, JsonUtil.getInstance().toJson(paymentResultScreenPreference));
        checkoutIntent.putExtra(PAYMENT_RESULT, JsonUtil.getInstance().toJson(paymentResult));
        checkoutIntent.putExtra(DISCOUNT, JsonUtil.getInstance().toJson(discount));
        checkoutIntent.putExtra(BINARY_MODE, binaryMode);
        checkoutIntent.putExtra(RESULT_CODE, resultCode);
        return checkoutIntent;
    }

    public static class Builder {

        private final String publicKey;
        private final String preferenceId;
        private final CheckoutPreference checkoutPreference;
        private final List<PaymentMethodPlugin> paymentMethodPluginList = new ArrayList<>();
        private final Map<String, PaymentProcessor> paymentPlugins = new HashMap<>();
        private Boolean binaryMode = false;
        private ServicePreference servicePreference;
        private FlowPreference flowPreference;
        private PaymentResultScreenPreference paymentResultScreenPreference;
        private PaymentData paymentData;
        private PaymentResult paymentResult;
        private Discount discount;
        private CheckoutHooks checkoutHooks;
        private DataInitializationTask dataInitializationTask;
        private String regularFontPath;
        private String lightFontPath;
        private String monoFontPath;
        private ReviewAndConfirmPreferences reviewAndConfirmPreferences;

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         *
         * @param publicKey merchant public key.
         * @param checkoutPreference the preference that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final CheckoutPreference checkoutPreference) {
            this.publicKey = publicKey;
            this.preferenceId = null;
            this.checkoutPreference = checkoutPreference;
        }

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         *
         * @param publicKey    merchant public key.
         * @param preferenceId the preference id that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final String preferenceId) {
            this.publicKey = publicKey;
            this.preferenceId = preferenceId;
            this.checkoutPreference = null;
        }

        public Builder setDiscount(Discount discount) {
            this.discount = discount;
            return this;
        }

        public Builder setServicePreference(ServicePreference servicePreference) {
            this.servicePreference = servicePreference;
            return this;
        }

        public Builder setFlowPreference(FlowPreference flowPreference) {
            this.flowPreference = flowPreference;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setPaymentResultScreenPreference(PaymentResultScreenPreference paymentResultScreenPreference) {
            this.paymentResultScreenPreference = paymentResultScreenPreference;
            return this;
        }

        public Builder setPaymentData(PaymentData paymentData) {
            this.paymentData = paymentData;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder enableBinaryMode() {
            binaryMode = true;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on
         * the Review and Confirm Screen see {@link ReviewAndConfirmPreferences.Builder}
         *
         * @param reviewAndConfirmPreferences the custom preference configuration
         * @return builder to keep operating
         */
        @SuppressWarnings("unused")
        public Builder setReviewAndConfirmPreferences(ReviewAndConfirmPreferences reviewAndConfirmPreferences) {
            this.reviewAndConfirmPreferences = reviewAndConfirmPreferences;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setPaymentResult(PaymentResult paymentResult) {
            this.paymentResult = paymentResult;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCheckoutHooks(@NonNull final CheckoutHooks checkoutHooks) {
            this.checkoutHooks = checkoutHooks;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addPaymentMethodPlugin(@NonNull final PaymentMethodPlugin paymentMethodPlugin,
                                              @NonNull final PaymentProcessor paymentProcessor) {
            paymentMethodPluginList.add(paymentMethodPlugin);
            paymentPlugins.put(paymentMethodPlugin.getId(), paymentProcessor);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setPaymentProcessor(@NonNull final PaymentProcessor paymentProcessor) {
            paymentPlugins.put(PAYMENT_PROCESSOR_KEY, paymentProcessor);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setDataInitializationTask(@NonNull final DataInitializationTask dataInitializationTask) {
            this.dataInitializationTask = dataInitializationTask;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCustomLightFont(String lightFontPath, Context context) {
            this.lightFontPath = lightFontPath;
            if (lightFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_LIGHT_FONT, this.lightFontPath);
            }
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCustomRegularFont(String regularFontPath, Context context) {
            this.regularFontPath = regularFontPath;
            if (regularFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_REGULAR_FONT, this.regularFontPath);
            }
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCustomMonoFont(String monoFontPath, Context context) {
            this.monoFontPath = monoFontPath;
            if (monoFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_MONO_FONT, this.monoFontPath);
            }
            return this;
        }

        private void setCustomFont(Context context, String fontType, String fontPath) {
            Typeface typeFace;
            if (!FontCache.hasTypeface(fontType)) {
                typeFace = Typeface.createFromAsset(context.getAssets(), fontPath);
                FontCache.setTypeface(fontType, typeFace);
            }
        }

        private boolean hasPaymentDataDiscount() {
            return paymentData != null && paymentData.getDiscount() != null;
        }

        private boolean hasPaymentResultDiscount() {
            return paymentResult != null && paymentResult.getPaymentData() != null &&
                    paymentResult.getPaymentData().getDiscount() != null;
        }

        private boolean hasTwoDiscountsSet() {
            return (hasPaymentDataDiscount() || hasPaymentResultDiscount()) && discount != null;
        }

        public MercadoPagoCheckout build() {
            if (hasTwoDiscountsSet()) {
                throw new IllegalStateException("payment data discount and discount set");
            }
            return new MercadoPagoCheckout(this);
        }
    }
}