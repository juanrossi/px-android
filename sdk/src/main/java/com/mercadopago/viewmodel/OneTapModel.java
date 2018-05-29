package com.mercadopago.viewmodel;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.model.Discount;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import java.io.Serializable;

public class OneTapModel implements Serializable {

    private final CheckoutPreference checkoutPreference;
    private final Discount discount;
    private final PaymentMethodSearch paymentMethods;
    private final boolean isEscEnabled;
    private final boolean hasExtraAmount;
    @DrawableRes
    @Nullable
    private final Integer collectorIcon;

    private OneTapModel(@NonNull final CheckoutPreference checkoutPreference,
        @Nullable final Discount discount,
        @NonNull final PaymentMethodSearch paymentMethods,
        boolean isEscEnabled,
        final boolean hasExtraAmount,
        @Nullable final Integer collectorIcon) {
        this.checkoutPreference = checkoutPreference;
        this.discount = discount;
        this.paymentMethods = paymentMethods;
        this.isEscEnabled = isEscEnabled;
        this.hasExtraAmount = hasExtraAmount;
        this.collectorIcon = collectorIcon;
    }

    public static OneTapModel from(CheckoutStateModel checkoutStateModel,
        final ReviewAndConfirmPreferences reviewAndConfirmPreferences) {
        return new OneTapModel(checkoutStateModel.checkoutPreference,
            checkoutStateModel.discount,
            checkoutStateModel.paymentMethodSearch,
            checkoutStateModel.flowPreference.isESCEnabled(),
            reviewAndConfirmPreferences.hasExtrasAmount(),
            reviewAndConfirmPreferences.getCollectorIcon());
    }

    public CheckoutPreference getCheckoutPreference() {
        return checkoutPreference;
    }

    public Discount getDiscount() {
        return discount;
    }

    public PaymentMethodSearch getPaymentMethods() {
        return paymentMethods;
    }

    public boolean isEscEnabled() {
        return isEscEnabled;
    }

    public boolean hasExtraAmount() {
        return hasExtraAmount;
    }

    @Nullable
    public Integer getCollectorIcon() {
        return collectorIcon;
    }
}
