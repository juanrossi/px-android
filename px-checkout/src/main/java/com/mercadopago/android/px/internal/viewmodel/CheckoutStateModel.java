package com.mercadopago.android.px.internal.viewmodel;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PaymentRecovery;

public final class CheckoutStateModel {

    private static final String EXTRA_PM_EDITED = "EXTRA_PM_EDITED";
    private static final String EXTRA_EDITED_FROM_RYC = "EXTRA_EDITED_FROM_RYC";
    private static final String EXTRA_UNIQUE_PM = "EXTRA_UNIQUE_PM";
    private static final String EXTRA_IS_ONE_TAP = "EXTRA_IS_ONE_TAP";
    private static final String EXTRA_RECOVERY = "EXTRA_RECOVERY";
    private static final String EXTRA_PAYMENT = "EXTRA_PAYMENT";
    private static final String EXTRA_PAYMENT_SERIALIZABLE = "EXTRA_PAYMENT_SERIALIZABLE";

    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public boolean isUniquePaymentMethod;
    public boolean isOneTap;
    public IPayment createdPayment;

    public CheckoutStateModel() {
    }

    public void toBundle(@NonNull final Bundle bundle) {
        bundle.putBoolean(EXTRA_PM_EDITED, paymentMethodEdited);
        bundle.putBoolean(EXTRA_EDITED_FROM_RYC, editPaymentMethodFromReviewAndConfirm);
        bundle.putBoolean(EXTRA_UNIQUE_PM, isUniquePaymentMethod);
        bundle.putBoolean(EXTRA_IS_ONE_TAP, isOneTap);
        bundle.putSerializable(EXTRA_RECOVERY, paymentRecovery);
        if (createdPayment instanceof Parcelable) {
            bundle.putParcelable(EXTRA_PAYMENT, (Parcelable) createdPayment);
        } else {
            bundle.putSerializable(EXTRA_PAYMENT_SERIALIZABLE, createdPayment);
        }
    }

    public static CheckoutStateModel fromBundle(@NonNull final Bundle bundle) {
        final CheckoutStateModel stateModel = new CheckoutStateModel();
        stateModel.paymentMethodEdited = bundle.getBoolean(EXTRA_PM_EDITED);
        stateModel.editPaymentMethodFromReviewAndConfirm = bundle.getBoolean(EXTRA_EDITED_FROM_RYC);
        stateModel.isUniquePaymentMethod = bundle.getBoolean(EXTRA_UNIQUE_PM);
        stateModel.isOneTap = bundle.getBoolean(EXTRA_IS_ONE_TAP);
        if (bundle.containsKey(EXTRA_PAYMENT)) {
            stateModel.createdPayment = bundle.getParcelable(EXTRA_PAYMENT);
        } else if (bundle.containsKey(EXTRA_PAYMENT_SERIALIZABLE)) {
            stateModel.createdPayment = (IPayment) bundle.getSerializable(EXTRA_PAYMENT_SERIALIZABLE);
        }
        return stateModel;
    }
}
