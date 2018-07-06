package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.lite.adapters.MPCall;
import com.mercadopago.model.PaymentMethodSearch;

public interface GroupsRepository {

    @NonNull
    MPCall<PaymentMethodSearch> getGroups();
}
