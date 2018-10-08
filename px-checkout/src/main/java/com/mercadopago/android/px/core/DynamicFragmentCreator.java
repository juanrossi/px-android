package com.mercadopago.android.px.core;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public interface DynamicFragmentCreator extends Parcelable {

    /* default */ final class CheckoutData {

        @NonNull
        public final CheckoutPreference checkoutPreference;
        @NonNull
        public final PaymentData paymentData;

        public CheckoutData(@NonNull final CheckoutPreference checkoutPreference,
            @NonNull final PaymentData paymentData) {
            this.checkoutPreference = checkoutPreference;
            this.paymentData = paymentData;
        }
    }

    /**
     * if true is returned then create will be called and fragment the fragment will be
     * placed.
     *
     * @param checkoutData
     * @return
     */
    boolean shouldShowFragment(@NonNull final Context context, @NonNull final CheckoutData checkoutData);

    /**
     * @param checkoutData available data
     * @return yourCustomDynamicFragment
     */
    @NonNull
    Fragment create(@NonNull final Context context, @NonNull final CheckoutData checkoutData);
}
