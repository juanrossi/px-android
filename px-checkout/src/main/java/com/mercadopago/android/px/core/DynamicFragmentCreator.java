package com.mercadopago.android.px.core;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public interface DynamicFragmentCreator extends Parcelable {

    final class CheckoutData {

        @Nullable
        public final CheckoutPreference checkoutPreference;

        @Nullable
        public final PaymentMethod selectedPaymentMethod;

        public CheckoutData(@Nullable final CheckoutPreference checkoutPreference,
            @Nullable final PaymentMethod selectedPaymentMethod) {
            this.checkoutPreference = checkoutPreference;
            this.selectedPaymentMethod = selectedPaymentMethod;
        }
    }

    /**
     * if true is returned then create will be called and fragment the fragment will be
     * placed.
     *
     * @param checkoutData
     * @return
     */
    boolean shouldShowFragment(@NonNull final CheckoutData checkoutData);

    /**
     * @param checkoutData available data
     * @return yourCustomDynamicFragment
     */
    @NonNull
    Fragment create(@NonNull final CheckoutData checkoutData);
}
