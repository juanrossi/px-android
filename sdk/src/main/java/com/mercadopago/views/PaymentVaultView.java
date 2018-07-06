package com.mercadopago.views;

import android.support.annotation.NonNull;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.internal.repository.DiscountRepository;
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
import com.mercadopago.preferences.PaymentPreference;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentVaultView extends MvpView {

    void startSavedCardFlow(Card card);

    void showPaymentMethodPluginActivity();

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

    void startCardFlow(Boolean automaticallySelection);

    void startPaymentMethodsSelection(final PaymentPreference paymentPreference);

    void finishPaymentMethodSelection(PaymentMethod selectedPaymentMethod);

    void finishPaymentMethodSelection(PaymentMethod paymentMethod, Payer payer);

    void showAmount(@NonNull final DiscountRepository discountRepository,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site);

    void collectPayerInformation();

    void cleanPaymentMethodOptions();

    void showHook(final Hook hook, final int code);

    void showDetailDialog(@NonNull final Discount discount, @NonNull final Campaign campaign);

    void showDiscountInputDialog();
}