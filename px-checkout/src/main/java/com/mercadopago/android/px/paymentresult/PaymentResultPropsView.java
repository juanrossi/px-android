package com.mercadopago.android.px.paymentresult;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.mvp.MvpView;

public interface PaymentResultPropsView extends MvpView {

    void setPropPaymentResult(@NonNull final String currencyId,
        @NonNull final PaymentResult paymentResult,
        final boolean showLoading);

    void setPropInstruction(@NonNull final Instruction instruction,
        @NonNull final String processingModeString,
        final boolean showLoading);

    void notifyPropsChanged();
}
