package com.mercadopago.model.commission;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public final class AmountCharge extends Charge {

    private final BigDecimal chargeAmount;

    public AmountCharge(final BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    @Override
    public BigDecimal calculate(@NonNull final BigDecimal amount) {
        return amount.add(this.chargeAmount);
    }
}
