package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.model.commission.PaymentMethodRule;
import java.math.BigDecimal;

public interface ChargeRepository {

    @NonNull
    BigDecimal getChargeAmount();

    boolean shouldApply(PaymentMethodRule paymentMethodRule);
}
