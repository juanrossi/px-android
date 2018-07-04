package com.mercadopago.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.internal.repository.InstallmentRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;
import java.math.BigDecimal;

public class InstallmentService implements InstallmentRepository {

    @NonNull private final UserSelectionRepository userSelectionService;

    public InstallmentService(@NonNull final UserSelectionRepository userSelectionService) {
        this.userSelectionService = userSelectionService;
    }

    @NonNull
    @Override
    public BigDecimal getInstallmentAmount() {
        return userSelectionService.hasPayerCostSelected() ? userSelectionService.getPayerCost().getInstallmentAmount()
            : BigDecimal.ZERO;
    }

    @NonNull
    @Override
    public BigDecimal getInstallmentTotalAmount() {
        return userSelectionService.hasPayerCostSelected() ? userSelectionService.getPayerCost().getTotalAmount()
            : BigDecimal.ZERO;
    }
}
