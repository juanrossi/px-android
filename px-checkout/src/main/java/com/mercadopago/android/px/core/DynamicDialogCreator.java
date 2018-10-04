package com.mercadopago.android.px.core;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public interface DynamicDialogCreator extends Parcelable {

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
     * if true is returned then create will be called and dialog will be
     * asked to be shown.
     *
     * @param checkoutData
     * @return
     */
    boolean shouldShowDialog(@NonNull final CheckoutData checkoutData);

    /**
     * @param checkoutData available data
     * @return yourDialog
     */
    @NonNull
    DialogFragment create(@NonNull final CheckoutData checkoutData);
}
