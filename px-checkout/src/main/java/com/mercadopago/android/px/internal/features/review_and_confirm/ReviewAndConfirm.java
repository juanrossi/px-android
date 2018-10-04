package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public interface ReviewAndConfirm {

    interface View extends MvpView {

        void trackPaymentConfirmation();

        void showCardCVVRequired(@NonNull final Card card);

        void cancelCheckoutAndInformError(@NonNull final MercadoPagoError mercadoPagoError);

        void showPaymentProcessor();

        void showResult(BusinessPaymentModel businessPaymentModel);

        void showResult(@NonNull PaymentResult paymentResult);

        void startLoadingButton(final int paymentTimeout);

        void cancelLoadingButton();

        void showLoadingFor(@NonNull final ExplodeDecorator decorator,
            @NonNull final ExplodingFragment.ExplodingAnimationListener explodingAnimationListener);

        void hideConfirmButton();

        void startPaymentRecoveryFlow(PaymentRecovery recovery);

        void showErrorScreen(@NonNull final MercadoPagoError error);

        void showConfirmButton();

        void showErrorSnackBar(@NonNull final MercadoPagoError error);

        void showDynamicDialog(DialogFragment dialogFragment);
    }

    interface Action extends PaymentServiceHandler {

        void onPaymentConfirm();

        //TODO unify Checkout Activity
        void onCardFlowResponse();

        void onCardFlowCancel();

        void onError(@NonNull final MercadoPagoError mercadoPagoError);

        void recoverFromFailure();



        void executePostPaymentAction(@NonNull PostPaymentAction postPaymentAction);
    }
}
