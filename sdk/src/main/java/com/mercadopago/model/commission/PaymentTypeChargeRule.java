package com.mercadopago.model.commission;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public final class PaymentTypeChargeRule extends ChargeRule {

    @NonNull
    private final String paymentType;
    @NonNull
    private final Charge charge;

    public PaymentTypeChargeRule(@NonNull final String paymentType,
                                 @NonNull final Charge charge) {
        this.paymentType = paymentType;
        this.charge = charge;
    }

    public boolean shouldApply(@NonNull final String paymentType) {
        return paymentType.equals(this.paymentType);
    }

    @Override
    @NonNull
    public BigDecimal applyCharge(@NonNull final BigDecimal totalAmount) {
        return charge.calculate(totalAmount);
    }
}
