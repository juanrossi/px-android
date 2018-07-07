package com.mercadopago.utils;

import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.mocks.PaymentMethods;

public final class StubPaymentResult {

    private StubPaymentResult() {
    }

    public static PaymentResult stubApprovedOffPaymentResult() {
        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(PaymentMethods.getPaymentMethodOff());
        return new PaymentResult.Builder()
            .setPaymentData(paymentData)
            .setPaymentStatus(Payment.StatusCodes.STATUS_APPROVED)
            .build();
    }
}
