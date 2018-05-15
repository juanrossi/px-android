package com.mercadopago.model.commission;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public abstract class Charge {
    @NonNull
    public abstract BigDecimal calculate(@NonNull BigDecimal amount);
}
