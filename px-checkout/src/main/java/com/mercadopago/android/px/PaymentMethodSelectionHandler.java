package com.mercadopago.android.px;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.util.TextUtil;

class PaymentMethodSelectionHandler {

    @Nullable private final GroupsRepository groupsRepository;
    @Nullable private final CheckoutPreference preference;

    public PaymentMethodSelectionHandler(@Nullable final GroupsRepository groupsRepository,
        @Nullable final CheckoutPreference preference) {
        this.groupsRepository = groupsRepository;
        this.preference = preference;
    }

    private boolean isCard() {
        return paymentMethod == null ? false : PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId());
    }

    public void run(final Callback callback) {
        if (isCard() && !isValid(cardId)) {
            callback.driveToPaymentVault();
        } else if (isCard() && isValid(cardId)) {
            callback.driveToCardVault();
        } else {
            callback.driveToPaymentVault();
        }
    }

    private boolean isValid(final String cardId) {
        return !TextUtil.isEmpty(cardId) && "1234".equalsIgnoreCase(cardId);
    }

    public interface Callback {

        void driveToPaymentVault();

        void driveToCardVault();
    }
}
