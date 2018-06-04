package com.mercadopago.views;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.Discount;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.MvpView;
import java.math.BigDecimal;
import java.util.List;

public interface InstallmentsActivityView extends MvpView {
    void showInstallments(List<PayerCost> payerCostList, OnSelectedCallback<Integer> onSelectedCallback);

    void startDiscountFlow(BigDecimal transactionAmount);

    void finishWithResult(PayerCost payerCost);

    void showLoadingView();

    void hideLoadingView();

    void showError(MercadoPagoError error, String requestOrigin);

    void showHeader();

    void initInstallmentsReviewView(PayerCost payerCost);

    void hideInstallmentsRecyclerView();

    void showInstallmentsRecyclerView();

    void hideInstallmentsReviewView();

    void showInstallmentsReviewView();

    void warnAboutBankInterests();

    void showAmount(@Nullable Discount discount, @Nullable Campaign campaign, final BigDecimal totalAmount, final Site site);

    void showDetailDialog(@NonNull final Discount discount, @NonNull final Campaign campaign);

    void showDetailDialog(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign);

    void showDiscountInputDialog();
}
