package com.mercadopago.viewmodel;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import java.io.Serializable;

public class OneTapModel implements Serializable {

    private final PaymentMethodSearch paymentMethods;
    private final boolean isEscEnabled;
    @DrawableRes
    @Nullable
    private final Integer collectorIcon;
    @NonNull private final String publicKey;

    private OneTapModel(@NonNull final PaymentMethodSearch paymentMethods,
        final boolean isEscEnabled,
        @NonNull final String publicKey,
        @Nullable final Integer collectorIcon) {

        this.paymentMethods = paymentMethods;
        this.isEscEnabled = isEscEnabled;
        this.collectorIcon = collectorIcon;
        this.publicKey = publicKey;
    }

    public static OneTapModel from(CheckoutStateModel checkoutStateModel,
        final ReviewAndConfirmPreferences reviewAndConfirmPreferences) {
        return new OneTapModel(checkoutStateModel.paymentMethodSearch,
            checkoutStateModel.config.getFlowPreference().isESCEnabled(),
            checkoutStateModel.config.getMerchantPublicKey(),
            reviewAndConfirmPreferences.getCollectorIcon());
    }

    public PaymentMethodSearch getPaymentMethods() {
        return paymentMethods;
    }

    public boolean isEscEnabled() {
        return isEscEnabled;
    }

    @Nullable
    public Integer getCollectorIcon() {
        return collectorIcon;
    }

    @NonNull
    public String getPublicKey() {
        return publicKey;
    }
}
