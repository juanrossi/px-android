package com.mercadopago.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;

public class PaymentMethodMapper extends Mapper<PaymentMethodSearch, PaymentMethod> {

    @Override
    public PaymentMethod map(@NonNull final PaymentMethodSearch val) {
        final String paymentMethodId = val.getOneTapMetadata().getPaymentMethodId();
        return val.getPaymentMethodById(paymentMethodId);
    }
}
