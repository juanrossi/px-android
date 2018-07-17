package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.services.adapters.MPCall;

public interface GroupsRepository {

    @NonNull
    MPCall<PaymentMethodSearch> getGroups();
}
