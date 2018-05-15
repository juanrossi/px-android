package com.mercadopago.model.commission;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public abstract class ChargeRule {
    @NonNull
    public abstract BigDecimal applyCharge(@NonNull BigDecimal totalAmount);
}
