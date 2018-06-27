package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.commission.ChargeRule;
import com.mercadopago.preferences.CheckoutPreference;
import java.util.List;

public interface PaymentSettingRepository {

    void reset();

    void configure(@NonNull List<ChargeRule> charges);

    void configure(@Nullable CheckoutPreference checkoutPreference);

    void configure(@Nullable final Discount discount);

    void configure(@Nullable final Campaign campaign);

    @Nullable
    Discount getDiscount();

    @Nullable
    Campaign getCampaign();

    @NonNull
    List<ChargeRule> chargeRules();

    @NonNull
    CheckoutPreference getCheckoutPreference();
}
