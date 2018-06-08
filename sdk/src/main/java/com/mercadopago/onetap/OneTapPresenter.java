package com.mercadopago.onetap;

import android.support.annotation.NonNull;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.model.OneTapMetadata;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.ResourcesProvider;
import com.mercadopago.tracker.Tracker;
import com.mercadopago.viewmodel.OneTapModel;
import com.mercadopago.viewmodel.mappers.CardMapper;
import com.mercadopago.viewmodel.mappers.CardPaymentMapper;
import com.mercadopago.viewmodel.mappers.PaymentMethodMapper;

class OneTapPresenter extends MvpPresenter<OneTap.View, ResourcesProvider> implements OneTap.Actions {

    @NonNull
    private final OneTapModel model;
    @NonNull
    private final CardMapper cardMapper;
    @NonNull
    private final PaymentMethodMapper paymentMethodMapper;

    /**
     * Creates a OneTap presenter.
     *
     * @param model one tap viewmodel
     */
    OneTapPresenter(@NonNull final OneTapModel model) {
        this.model = model;
        cardMapper = new CardMapper();
        paymentMethodMapper = new PaymentMethodMapper();
    }

    @Override
    public void confirmPayment() {
        OneTapMetadata oneTapMetadata = model.getPaymentMethods().getOneTapMetadata();
        String paymentTypeId = oneTapMetadata.getPaymentTypeId();
        String paymentMethodId = oneTapMetadata.getPaymentMethodId();
        // TODO refactor
        CheckoutStore.getInstance().setSelectedPaymentMethodId(paymentMethodId);

        if (PaymentTypes.isCardPaymentMethod(paymentTypeId)) {
            getView().showCardFlow(model, cardMapper.map(model));
        } else if (PaymentTypes.isPlugin(paymentTypeId)) {
            getView().showPaymentFlowPlugin(paymentTypeId, paymentMethodId, model.getTransactionAmount());
        } else {
            getView().showPaymentFlow(paymentMethodMapper.map(model.getPaymentMethods()));
        }
    }

    @Override
    public void onReceived(@NonNull final Token token) {
        getView().showPaymentFlow(new CardPaymentMapper(token).map(model));
    }

    @Override
    public void changePaymentMethod() {
        getView().changePaymentMethod();
    }

    @Override
    public void onAmountShowMore() {
        getView().showDetailModal(model);
    }
}
