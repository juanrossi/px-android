package com.mercadopago.android.px.internal.viewmodel;

import android.os.Parcel;
import android.support.annotation.NonNull;

public class RecoverPaymentPostPaymentAction extends PostPaymentAction {

    public RecoverPaymentPostPaymentAction(@NonNull final OriginAction originAction) {
        super(RequiredAction.RECOVER_PAYMENT, originAction);
    }

    /* default */ RecoverPaymentPostPaymentAction(final Parcel in) {
        super(in);
    }

    @Override
    public void execute(@NonNull final ActionController actionController) {
        switch (originAction) {
        case ONE_TAP:
            actionController.recoverFromOneTap();
            break;
        case REVIEW_AND_CONFIRM:
            actionController.recoverFromReviewAndConfirm(this);
            break;
        case UNKNOWN:
        default:
            break;
        }
    }

    public static final Creator<RecoverPaymentPostPaymentAction> CREATOR =
        new Creator<RecoverPaymentPostPaymentAction>() {
            @Override
            public RecoverPaymentPostPaymentAction createFromParcel(final Parcel in) {
                return new RecoverPaymentPostPaymentAction(in);
            }

            @Override
            public RecoverPaymentPostPaymentAction[] newArray(final int size) {
                return new RecoverPaymentPostPaymentAction[size];
            }
        };
}
