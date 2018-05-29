package com.mercadopago.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;

public class PaymentMethodMapper extends Mapper<PaymentMethodSearch, PaymentMethod> {

    @NonNull private final String paymentTypeId;

    public PaymentMethodMapper(@NonNull final String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    @Override
    public PaymentMethod map(@NonNull final PaymentMethodSearch val) {
        return val.getPaymentMethodById(paymentTypeId);
    }
}
