package com.mercadopago.internal.datasource;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.commission.ChargeRule;
import com.mercadopago.model.commission.PaymentMethodRule;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.util.JsonUtil;
import java.lang.reflect.Type;
import java.util.List;

public class PaymentSettingService implements PaymentSettingRepository {

    private static final String PREF_CAMPAIGN = "pref_campaign";
    private static final String PREF_DISCOUNT = "pref_discount";
    private static final String PREF_CHARGES = "pref_charges";
    private static final String PREF_CHECKOUT_PREF = "pref_checkout_config";

    @NonNull private final SharedPreferences sharedPreferences;
    @NonNull private final JsonUtil jsonUtil;

    //mem cache
    private CheckoutPreference pref;

    public PaymentSettingService(@NonNull final SharedPreferences sharedPreferences, @NonNull final JsonUtil jsonUtil) {
        this.sharedPreferences = sharedPreferences;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void reset() {
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear().apply();
    }

    @Override
    public void configure(@NonNull final List<ChargeRule> charges) {
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREF_CHARGES, jsonUtil.toJson(charges));
        edit.apply();
    }

    @Override
    public void configure(@Nullable final CheckoutPreference checkoutPreference) {
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREF_CHECKOUT_PREF, jsonUtil.toJson(checkoutPreference));
        edit.apply();
        pref = checkoutPreference;
    }

    @Override
    public void configure(@Nullable final Discount discount) {
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREF_DISCOUNT, jsonUtil.toJson(discount));
        edit.apply();
    }

    @Override
    public void configure(@Nullable final Campaign campaign) {
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(PREF_CAMPAIGN, jsonUtil.toJson(campaign));
        edit.apply();
    }

    @Nullable
    @Override
    public Discount getDiscount() {
        return jsonUtil.fromJson(sharedPreferences.getString(PREF_DISCOUNT, ""), Discount.class);
    }

    @Nullable
    @Override
    public Campaign getCampaign() {
        return jsonUtil.fromJson(sharedPreferences.getString(PREF_CAMPAIGN, ""), Campaign.class);
    }

    @NonNull
    @Override
    public List<ChargeRule> chargeRules() {
        final Type listType = new TypeToken<List<PaymentMethodRule>>() {
        }.getType();
        return jsonUtil.fromJson(sharedPreferences.getString(PREF_CHARGES, ""), listType);
    }

    @NonNull
    @Override
    public CheckoutPreference getCheckoutPreference() {
        if (pref == null) {
            pref = jsonUtil.fromJson(sharedPreferences.getString(PREF_CHECKOUT_PREF, ""), CheckoutPreference.class);
        }
        return pref;
    }
}
