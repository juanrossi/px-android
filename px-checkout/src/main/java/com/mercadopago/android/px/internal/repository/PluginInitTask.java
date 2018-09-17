package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;

public interface PluginInitTask {

    /* async init */
    void init(@NonNull final DataInitializationCallbacks callback);

    void cancel();

    interface DataInitializationCallbacks {
        void onDataInitialized();

        void onFailure(@NonNull final Exception e);
    }
}
