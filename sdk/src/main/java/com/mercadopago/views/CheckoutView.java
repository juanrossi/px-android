package com.mercadopago.views;

import android.support.annotation.NonNull;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.mvp.MvpView;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.viewmodel.OneTapModel;

public interface CheckoutView extends MvpView {

    void showError(MercadoPagoError error);

    void showProgress();

    void showReviewAndConfirm();

    void showPaymentMethodSelection();

    void showPaymentResult(PaymentResult paymentResult);

    void backToReviewAndConfirm();

    void finishWithPaymentResult();

    void finishWithPaymentResult(Integer customResultCode);

    void finishWithPaymentResult(Payment payment);

    void finishWithPaymentResult(Integer customResultCode, Payment payment);

    void finishWithPaymentDataResult(PaymentData paymentData, Boolean paymentMethodEdited);

    void cancelCheckout();

    void cancelCheckout(MercadoPagoError mercadoPagoError);

    void cancelCheckout(Integer customResultCode, Boolean paymentMethodEdited);

    void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery);

    void initializeMPTracker();

    void trackScreen();

    void showHook(final Hook hook, final int requestCode);

    void showPaymentProcessor();

    boolean isActive();

    void fetchImageFromUrl(String url);

    void showBusinessResult(BusinessPaymentModel model);

    void showOneTap(@NonNull final OneTapModel oneTapModel);

    void hideProgress();

    void exitCheckout(int resCode);

    void transitionOut();
}
