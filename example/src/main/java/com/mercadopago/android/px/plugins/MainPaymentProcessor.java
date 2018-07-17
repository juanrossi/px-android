package com.mercadopago.android.px.plugins;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.plugins.components.MainPayment;
import com.mercadopago.android.px.plugins.model.PluginPayment;

public class MainPaymentProcessor extends PaymentProcessor {

    private final PluginPayment pluginPayment;

    public MainPaymentProcessor(final PluginPayment pluginPayment) {
        this.pluginPayment = pluginPayment;
    }

    @NonNull
    @Override
    public PluginComponent createPaymentComponent(@NonNull final PluginComponent.Props props,
        @NonNull final Context context) {

        PluginComponent.Props newProps = props.toBuilder()
            .setToolbarVisible(false)
            .build();

        return new MainPayment(newProps, pluginPayment);
    }
}