package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentMethodPlugin;

public class PluginInitializationAsync extends PluginInitSync {

    /* default */ Thread taskThread;

    public PluginInitializationAsync(@NonNull final Iterable<PaymentMethodPlugin> plugins,
        @NonNull final PaymentMethodPlugin.CheckoutData checkoutData) {
        super(plugins, checkoutData);
    }

    /* async init */
    @Override
    public void init(@NonNull final DataInitializationCallbacks callbacks) {
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
                PluginInitializationAsync.super.init(new DataInitializationCallbacks() {
                    @Override
                    public void onDataInitialized() {
                        try {
                            if (!taskThread.isInterrupted()) {
                                callbacks.onDataInitialized();
                            }
                        } catch (final Exception e) {
                            callbacks.onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        if (!taskThread.isInterrupted()) {
                            callbacks.onFailure(e);
                        }
                    }
                });
            }
        });
        taskThread.start();
    }

    @Override
    public void cancel() {
        if (taskThread != null && taskThread.isAlive() && !taskThread.isInterrupted()) {
            taskThread.interrupt();
        }
    }
}