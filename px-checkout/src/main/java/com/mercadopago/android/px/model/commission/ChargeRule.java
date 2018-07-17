package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.io.Serializable;
import java.math.BigDecimal;

public abstract class ChargeRule implements Serializable {

    @NonNull
    private final BigDecimal charge;

    protected ChargeRule(@NonNull final BigDecimal charge) {
        this.charge = charge;
    }

    @NonNull
    public BigDecimal charge() {
        return charge;
    }

    public abstract boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository);
}
