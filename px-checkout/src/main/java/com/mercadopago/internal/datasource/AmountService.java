package com.mercadopago.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.ChargeRepository;
import com.mercadopago.internal.repository.DiscountRepository;
import com.mercadopago.internal.repository.InstallmentRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.model.Discount;
import java.math.BigDecimal;

public class AmountService implements AmountRepository {

    @NonNull private final PaymentSettingRepository paymentSetting;
    @NonNull private final ChargeRepository chargeRepository;
    @NonNull private final InstallmentRepository installmentRepository;
    @NonNull private final DiscountRepository discountRepository;

    public AmountService(@NonNull final PaymentSettingRepository paymentSetting,
        @NonNull final ChargeRepository chargeRepository,
        @NonNull final InstallmentRepository installmentRepository,
        @NonNull final DiscountRepository discountRepository) {
        this.paymentSetting = paymentSetting;
        this.chargeRepository = chargeRepository;
        this.installmentRepository = installmentRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    @NonNull
    public BigDecimal getAmountToPay() {
        final BigDecimal amount = amountWithoutPayerCosts();
        final BigDecimal installmentTotalAmount = installmentRepository.getInstallmentTotalAmount();
        final int cmp = amount.compareTo(installmentTotalAmount);
        // if amount  > installment amount ; return amount else installmentAmount
        return cmp > 0 ? amount : installmentTotalAmount;
    }

    private BigDecimal amountWithoutPayerCosts() {
        return paymentSetting.getCheckoutPreference().getTotalAmount()
            .add(chargeRepository.getChargeAmount())
            .subtract(getDiscountAmount());
    }

    @Override
    @NonNull
    public BigDecimal getItemsAmount() {
        return paymentSetting.getCheckoutPreference().getTotalAmount();
    }

    @NonNull
    @Override
    public BigDecimal getItemsPlusCharges() {
        final BigDecimal totalAmount = paymentSetting.getCheckoutPreference().getTotalAmount();
        return totalAmount.add(chargeRepository.getChargeAmount());
    }

    @NonNull
    @Override
    public BigDecimal getAppliedCharges() {
        final BigDecimal amountWithoutPayerCosts = amountWithoutPayerCosts();
        final BigDecimal installmentTotalAmount = installmentRepository.getInstallmentTotalAmount();
        final int compare = amountWithoutPayerCosts.compareTo(installmentTotalAmount);
        // if amount  > installment amount ; return only charges else charges plus diff
        return compare > 0 ? chargeRepository.getChargeAmount() : installmentTotalAmount
            .subtract(amountWithoutPayerCosts)
            .add(chargeRepository.getChargeAmount());
    }


    private BigDecimal getDiscountAmount() {
        final Discount discount = discountRepository.getDiscount();
        return discount == null ? BigDecimal.ZERO : discount.getCouponAmount();
    }
}
