package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.commission.ChargeRule;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.FlowPreference;
import java.util.List;

public interface PaymentSettingRepository {

    void reset();

    void configure(@NonNull List<ChargeRule> charges);

    void configure(@NonNull FlowPreference flowPreference);

    void configure(@NonNull String publicKey);

    void configure(@Nullable CheckoutPreference checkoutPreference);

    void configurePreferenceId(@Nullable String preferenceId);

    @NonNull
    List<ChargeRule> chargeRules();

    @Nullable
    CheckoutPreference getCheckoutPreference();

    @Nullable
    String getCheckoutPreferenceId();

    @NonNull
    String getPublicKey();

    @NonNull
    FlowPreference getFlow();

    @Nullable
    String getPrivateKey();

    void configurePrivateKey(@Nullable final String privateKey);
}
