package com.mercadopago.internal.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.mercadopago.util.JsonUtil;

class ActivityModule implements PreferenceComponent {

    @NonNull
    private final Context context;

    /* default */ ActivityModule(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("com.mercadopago.checkout.store", Context.MODE_PRIVATE);
    }

    public JsonUtil getJsonUtil() {
        return JsonUtil.getInstance();
    }
}
