package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.repository.PluginInitTask;

/* default */ class PluginInitSync implements PluginInitTask {

    @NonNull private final Iterable<PaymentMethodPlugin> plugins;
    @NonNull private final PaymentMethodPlugin.CheckoutData checkoutData;

    /* default */ PluginInitSync(@NonNull final Iterable<PaymentMethodPlugin> plugins,
        @NonNull final PaymentMethodPlugin.CheckoutData checkoutData) {
        this.plugins = plugins;
        this.checkoutData = checkoutData;
    }

    @Override
    public void init(@NonNull final DataInitializationCallbacks callback) {
        try {
            for (final PaymentMethodPlugin plugin : plugins) {
                plugin.init(checkoutData);
            }
            callback.onDataInitialized();
        } catch (final Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public void cancel() {
        // Do nothing.
    }
}
