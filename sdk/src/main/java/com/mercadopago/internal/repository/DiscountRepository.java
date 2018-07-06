package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.lite.adapters.MPCall;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.mvp.ResourcesProvider;
import java.math.BigDecimal;

public interface DiscountRepository extends ResourcesProvider {

    void configureDiscountManually(@Nullable final Discount discount, @Nullable final Campaign campaign);

    @NonNull
    MPCall<Boolean> configureDiscountAutomatically(final BigDecimal amountToPay);

    @NonNull
    MPCall<Discount> getCodeDiscount(@NonNull final BigDecimal amount, @NonNull final String inputCode);

    @Nullable
    Discount getDiscount();

    @Nullable
    String getDiscountCode();

    @Nullable
    Campaign getCampaign();

    boolean hasCodeCampaign();

    void saveDiscountCode(@NonNull final String code);

    void reset();
}
