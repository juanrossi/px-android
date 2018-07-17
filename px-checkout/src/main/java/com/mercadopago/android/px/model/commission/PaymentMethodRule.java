package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.math.BigDecimal;

public class PaymentMethodRule extends ChargeRule {

    @NonNull
    private final String stringValue;

    /**
     * @param stringValue to compare
     * @param charge the charge amount to apply for this rule
     */
    PaymentMethodRule(@NonNull final String stringValue,
        @NonNull final BigDecimal charge) {
        super(charge);
        this.stringValue = stringValue;
    }

    @NonNull
    public String getValue() {
        return stringValue;
    }

    @Override
    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }
}
