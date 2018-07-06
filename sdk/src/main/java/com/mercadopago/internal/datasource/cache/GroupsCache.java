package com.mercadopago.internal.datasource.cache;

import android.support.annotation.NonNull;
import com.mercadopago.lite.adapters.MPCall;
import com.mercadopago.model.PaymentMethodSearch;

public interface GroupsCache {

    @NonNull
    MPCall<PaymentMethodSearch> get();

    void put(@NonNull final PaymentMethodSearch groups);

    void evict();

    boolean isCached();
}
