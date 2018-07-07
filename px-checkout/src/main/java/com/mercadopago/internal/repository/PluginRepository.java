package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.plugins.PaymentMethodPlugin;
import com.mercadopago.plugins.model.PaymentMethodInfo;

public interface PluginRepository {

    @NonNull
    PaymentMethodPlugin getPlugin(@NonNull String pluginId);

    @NonNull
    PaymentMethod getPluginAsPaymentMethod(@NonNull final String pluginId, @NonNull final String paymentType);

    @NonNull
    PaymentMethodInfo getPaymentMethodInfo(PaymentMethodPlugin plugin);

    @NonNull
    PaymentMethodInfo getPaymentMethodInfo(@NonNull final String pluginId);
}
