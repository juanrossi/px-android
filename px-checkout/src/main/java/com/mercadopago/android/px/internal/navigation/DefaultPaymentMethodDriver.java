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
    private boolean newCard;

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
        if (preference != null) {
            if (isCard() && isValid(preference.getDefaultCardId())) {
                final Card card = paymentMethods.getCardById(preference.getDefaultCardId());
                card.setPaymentMethod(paymentMethods.getPaymentMethodById(preference.getDefaultPaymentMethodId()));
                paymentMethodDriverCallback.driveToCardVault(card);
            } else if (isNewCard()) {
                paymentMethodDriverCallback.driveToNewCardFlow();
            } else {
                paymentMethodDriverCallback.doNothing();
            }
        } else {
            paymentMethodDriverCallback.doNothing();
        }
    }

    private boolean isValid(@Nullable final String cardId) {
        return !TextUtil.isEmpty(cardId) && paymentMethods.getCardById(cardId) != null;
    }

    public boolean isNewCard() {
        final PaymentMethod paymentMethod =
            paymentMethods.getPaymentMethodByPaymentTypeId(preference.getDefaultPaymentMethodId());
        return preference.getDefaultCardId() == null && paymentMethod != null && PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId());
    }

    public interface PaymentMethodDriverCallback {

        void driveToCardVault(@NonNull final Card card);

        void doNothing();

        void driveToNewCardFlow();
    }
}
