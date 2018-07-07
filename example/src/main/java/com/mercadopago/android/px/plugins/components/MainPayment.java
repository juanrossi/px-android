package com.mercadopago.android.px.plugins.components;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.plugins.PaymentPluginProcessorResultAction;
import com.mercadopago.android.px.plugins.PluginComponent;
import com.mercadopago.android.px.plugins.model.PluginPayment;

public class MainPayment extends PluginComponent<Void> {

    static {
        RendererFactory.register(MainPayment.class, MainPaymentRenderer.class);
    }

    private final Handler handler = new Handler();
    private final PluginPayment pluginPayment;


    public MainPayment(@NonNull final Props props, PluginPayment pluginPayment) {
        super(props);
        this.pluginPayment = pluginPayment;
    }

    @Override
    public void onViewAttachedToWindow() {
        executePayment();
    }

    public void executePayment() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDispatcher().dispatch(new PaymentPluginProcessorResultAction(pluginPayment));
            }
        }, 2000);
    }
}