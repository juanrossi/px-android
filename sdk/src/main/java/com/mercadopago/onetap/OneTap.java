package com.mercadopago.onetap;

import android.support.annotation.NonNull;
import com.mercadopago.model.Card;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpView;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.OneTapModel;
import java.math.BigDecimal;

public interface OneTap {

    interface View extends MvpView {

        void cancel();

        void changePaymentMethod();

        void showCardFlow(@NonNull final OneTapModel oneTapModel, @NonNull final Card card);

        void showPaymentFlow(@NonNull final PaymentMethod oneTapMetadata);

        void showPaymentFlow(@NonNull final CardPaymentModel cardPaymentModel);

        void showPaymentFlowPlugin(@NonNull final String paymentTypeId, @NonNull final String paymentMethodId,
            @NonNull final BigDecimal transactionAmount);

        void showDetailModal(@NonNull final OneTapModel model);
    }

    interface Actions {

        void confirmPayment();

        void onReceived(@NonNull final Token token);

        void changePaymentMethod();

        void onAmountShowMore();
    }
}
