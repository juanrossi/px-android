package com.mercadopago.internal.repository;

import android.support.annotation.NonNull;
import java.math.BigDecimal;

public interface InstallmentRepository {

    @NonNull
    BigDecimal getInstallmentAmount();

    @NonNull
    BigDecimal getInstallmentTotalAmount();
}
