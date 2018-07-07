package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.services.adapters.MPCall;
import com.mercadopago.android.px.model.PaymentMethodSearch;

public interface GroupsRepository {

    @NonNull
    MPCall<PaymentMethodSearch> getGroups();
}
