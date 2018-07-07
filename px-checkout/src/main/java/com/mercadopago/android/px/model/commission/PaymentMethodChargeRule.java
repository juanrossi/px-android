package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.internal.repository.ChargeRepository;
import java.math.BigDecimal;

public final class PaymentMethodChargeRule extends PaymentMethodRule {

    /**
     * @param paymentMethodId the payment type associated with the charge to shouldBeTriggered.
     * @param charge the charge amount to apply for this rule
     */
    public PaymentMethodChargeRule(@NonNull final String paymentMethodId,
        @NonNull final BigDecimal charge) {
        super(paymentMethodId, charge);
    }

    @Override
    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }
}
