package com.mercadopago.views;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.CustomSearchItem;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearchItem;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.MvpView;
import com.mercadopago.plugins.PaymentMethodPlugin;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentVaultView extends MvpView {

    void startSavedCardFlow(Card card, BigDecimal transactionAmount);

    void showSelectedItem(PaymentMethodSearchItem item);

    void showProgress();

    void hideProgress();

    void showCustomOptions(List<CustomSearchItem> customSearchItems,
                           OnSelectedCallback<CustomSearchItem> customSearchItemOnSelectedCallback);

    void showPluginOptions(List<PaymentMethodPlugin> items, String position);

    void showSearchItems(List<PaymentMethodSearchItem> searchItems,
                         OnSelectedCallback<PaymentMethodSearchItem> paymentMethodSearchItemSelectionCallback);

    void showError(MercadoPagoError mercadoPagoError, String requestOrigin);

    void setTitle(String title);

    void startCardFlow(String paymentType, BigDecimal transactionAmount,
                       Boolean automaticallySelection);

    void startPaymentMethodsSelection();

    void finishPaymentMethodSelection(PaymentMethod selectedPaymentMethod);

    void finishPaymentMethodSelection(PaymentMethod paymentMethod, Payer payer);

    void showAmount(@Nullable Discount discount, @Nullable Campaign campaign, final BigDecimal totalAmount, final Site site);

    void startDiscountFlow(BigDecimal transactionAmount);

    void collectPayerInformation();

    void cleanPaymentMethodOptions();

    void showHook(final Hook hook, final int code);

    void showPaymentMethodPluginConfiguration();

    void showDetailDialog(@NonNull final Discount discount, @NonNull final Campaign campaign);

    void showDetailDialog(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign);

    void showDiscountInputDialog();
}