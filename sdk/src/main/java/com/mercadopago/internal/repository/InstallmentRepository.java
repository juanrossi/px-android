package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.lite.adapters.MPCall;
import com.mercadopago.model.Installment;
import java.math.BigDecimal;
import java.util.List;

public interface InstallmentRepository {

    @NonNull
    BigDecimal getInstallmentAmount();

    @NonNull
    BigDecimal getInstallmentTotalAmount();

    @NonNull
    MPCall<List<Installment>> getInstallments();
}
