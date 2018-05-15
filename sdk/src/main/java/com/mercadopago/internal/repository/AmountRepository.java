package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public interface AmountRepository {

    @NonNull
    BigDecimal getAmountToPay();

    @NonNull
    BigDecimal getItemsAmount();

    @NonNull
    BigDecimal getDiscountAmount();

    @NonNull
    BigDecimal getCommissionsAmount();
}
