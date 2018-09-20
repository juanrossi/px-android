package com.mercadopago.android.px.internal.viewmodel;

import android.os.Bundle;
import android.support.annotation.NonNull;

public final class CheckoutStateModel {

    private static final String EXTRA_PM_EDITED = "EXTRA_PM_EDITED";
    private static final String EXTRA_EDITED_FROM_RYC = "EXTRA_EDITED_FROM_RYC";
    private static final String EXTRA_UNIQUE_PM = "EXTRA_UNIQUE_PM";
    private static final String EXTRA_IS_ONE_TAP = "EXTRA_IS_ONE_TAP";

    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;

    public boolean isUniquePaymentMethod;
    public boolean isOneTap;

    public CheckoutStateModel() {
    }

    public void toBundle(@NonNull final Bundle bundle) {
        bundle.putBoolean(EXTRA_PM_EDITED, paymentMethodEdited);
        bundle.putBoolean(EXTRA_EDITED_FROM_RYC, editPaymentMethodFromReviewAndConfirm);
        bundle.putBoolean(EXTRA_UNIQUE_PM, isUniquePaymentMethod);
        bundle.putBoolean(EXTRA_IS_ONE_TAP, isOneTap);
    }

    public static CheckoutStateModel fromBundle(@NonNull final Bundle bundle) {
        final CheckoutStateModel stateModel = new CheckoutStateModel();
        stateModel.paymentMethodEdited = bundle.getBoolean(EXTRA_PM_EDITED);
        stateModel.editPaymentMethodFromReviewAndConfirm = bundle.getBoolean(EXTRA_EDITED_FROM_RYC);
        stateModel.isUniquePaymentMethod = bundle.getBoolean(EXTRA_UNIQUE_PM);
        stateModel.isOneTap = bundle.getBoolean(EXTRA_IS_ONE_TAP);
        return stateModel;
    }
}
