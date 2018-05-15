package com.mercadopago.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mercadopago.internal.repository.AmountRepository;

import java.math.BigDecimal;

public class AmountMemRepository implements AmountRepository {

    @NonNull
    private final BigDecimal itemsAmount;
    @NonNull
    private final BigDecimal discountAmount;
    @NonNull
    private final BigDecimal chargeAmount;

    public AmountMemRepository(@NonNull final BigDecimal itemsAmount,
                               @Nullable BigDecimal discountAmount,
                               @Nullable BigDecimal chargeAmount) {
        this.itemsAmount = itemsAmount;
        this.discountAmount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        this.chargeAmount = chargeAmount == null ? BigDecimal.ZERO : chargeAmount;
    }

    @NonNull
    @Override
    public BigDecimal getAmountToPay() {
        return itemsAmount
                .subtract(getDiscountAmount())
                .add(getCommissionsAmount());
    }

    @NonNull
    @Override
    public BigDecimal getItemsAmount() {
        return itemsAmount;
    }

    @NonNull
    @Override
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @NonNull
    @Override
    public BigDecimal getCommissionsAmount() {
        return chargeAmount;
    }
}
