package com.mercadopago.android.px.utils;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.datasource.PluginInitializationAsync;
import java.util.ArrayList;

public class PluginInitializationSuccess extends PluginInitializationAsync {

    public PluginInitializationSuccess() {
        super(new ArrayList<PaymentMethodPlugin>(), null);
    }

    @Override
    public void init(@NonNull final DataInitializationCallbacks callbacks) {
        callbacks.onDataInitialized();
    }
}
