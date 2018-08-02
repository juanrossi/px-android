package com.mercadopago.android.px.internal.navigation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.util.TextUtil;

public class DefaultPaymentMethodDriver {

    @NonNull private final PaymentMethodSearch paymentMethods;
    @Nullable private final PaymentPreference preference;

    public DefaultPaymentMethodDriver(@NonNull final PaymentMethodSearch paymentMethods,
        @Nullable final PaymentPreference preference) {
        this.paymentMethods = paymentMethods;
        this.preference = preference;
    }

    private boolean isCard() {
        final PaymentMethod paymentMethod = paymentMethods.getPaymentMethodById(preference.getDefaultPaymentMethodId());
        return paymentMethod != null && PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId());
    }

    public void drive(final PaymentMethodDriverCallback paymentMethodDriverCallback) {
        if (preference == null) {
            paymentMethodDriverCallback.doNothing();
        } else if (isCard() && !isValid(preference.getDefaultCardId())) {
            paymentMethodDriverCallback.driveToPaymentVault();
        } else if (isCard() && isValid(preference.getDefaultCardId())) {
            paymentMethodDriverCallback.driveToCardVault(paymentMethods.getCardById(preference.getDefaultCardId()));
        } else {
            paymentMethodDriverCallback.driveToPaymentVault();
        }
    }

    private boolean isValid(@Nullable final String cardId) {
        return !TextUtil.isEmpty(cardId) && paymentMethods.getCardById(cardId) != null;
    }

    public interface PaymentMethodDriverCallback {

        void driveToPaymentVault();

        void driveToCardVault(@NonNull final Card card);

        void doNothing();
    }
}
