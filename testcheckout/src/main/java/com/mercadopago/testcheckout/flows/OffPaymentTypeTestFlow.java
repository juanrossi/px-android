package com.mercadopago.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.testcheckout.assertions.Validator;
import com.mercadopago.testcheckout.pages.CongratsPage;
import com.mercadopago.testcheckout.pages.PaymentMethodPage;

public class OffPaymentTypeTestFlow extends TestFlow {


    public OffPaymentTypeTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public OffPaymentTypeTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runOffPaymentTypeFlow(@NonNull final String paymentMethodName, final Validator validator) {

        super.startCheckout();

        return new PaymentMethodPage(validator)
                .selectCash()
                .selectMethod(paymentMethodName)
                .pressConfirmButton();

    }

    @NonNull
    public CongratsPage runOffPaymentTypeFlow(@NonNull final String paymentMethodName) {
        return runOffPaymentTypeFlow(paymentMethodName, null);
    }
}
