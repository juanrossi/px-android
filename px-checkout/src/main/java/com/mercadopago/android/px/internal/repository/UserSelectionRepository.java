package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;

public interface UserSelectionRepository {

    void select(@Nullable final PaymentMethod paymentMethod);

    void select(@NonNull final PayerCost payerCost);

    @Nullable
    PaymentMethod getPaymentMethod();

    void removePaymentMethodSelection();

    boolean hasSelectedPaymentMethod();

    boolean hasPayerCostSelected();

    @Nullable
    PayerCost getPayerCost();

    void reset();
}
