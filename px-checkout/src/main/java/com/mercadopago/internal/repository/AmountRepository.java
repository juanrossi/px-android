package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import java.math.BigDecimal;

public interface AmountRepository {

    /**
     * Final amount value to pay
     * Discounts, charges and payer costs are involved on this value.
     *
     * @return amount to be processed by groups and installments
     */
    @NonNull
    BigDecimal getAmountToPay();

    /**
     * Partial amount to pay - No charges, discounts or payer costs applied
     *
     * @return amount summatory of items values * quantity
     */
    @NonNull
    BigDecimal getItemsAmount();

    /**
     * Partial amount to pay, it's needed for graphic proposes.
     *
     * @return amount items plus charges.
     */
    @NonNull
    BigDecimal getItemsPlusCharges();

    /**
     * Partial amount that represents charges.
     * It can be credit card extra charges, payment method charges or both.
     *
     * @return amount charges.
     */
    @NonNull
    BigDecimal getAppliedCharges();

}
