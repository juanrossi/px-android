package com.mercadopago.onetap;

import android.support.annotation.NonNull;
import com.mercadopago.internal.repository.PluginRepository;
import com.mercadopago.model.OneTapMetadata;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.ResourcesProvider;
import com.mercadopago.viewmodel.OneTapModel;
import com.mercadopago.viewmodel.mappers.CardMapper;
import com.mercadopago.viewmodel.mappers.CardPaymentMapper;
import com.mercadopago.viewmodel.mappers.PaymentMethodMapper;

class OneTapPresenter extends MvpPresenter<OneTap.View, ResourcesProvider> implements OneTap.Actions {

    @NonNull private final OneTapModel model;
    @NonNull private final PluginRepository pluginRepository;

    @NonNull private final CardMapper cardMapper;
    @NonNull private final PaymentMethodMapper paymentMethodMapper;

    /**
     * Creates a OneTap presenter.
     *  @param model one tap viewmodel
     * @param pluginRepository
     */
    OneTapPresenter(@NonNull final OneTapModel model,
        @NonNull final PluginRepository pluginRepository) {
        this.model = model;
        this.pluginRepository = pluginRepository;
        cardMapper = new CardMapper();
        paymentMethodMapper = new PaymentMethodMapper();
    }

    @Override
    public void confirmPayment() {
        final OneTapMetadata oneTapMetadata = model.getPaymentMethods().getOneTapMetadata();
        final String paymentTypeId = oneTapMetadata.getPaymentTypeId();
        final String paymentMethodId = oneTapMetadata.getPaymentMethodId();
        getView().trackConfirm(model);
        if (PaymentTypes.isCardPaymentType(paymentTypeId)) {
            getView().showCardFlow(model, cardMapper.map(model));
        } else if (PaymentTypes.isPlugin(paymentTypeId)) {
            getView().showPaymentFlow(pluginRepository.getPluginAsPaymentMethod(paymentMethodId, paymentTypeId));
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
        getView().trackModal(model);
        getView().showDetailModal(model);
    }

    public void cancel() {
        getView().cancel();
        getView().trackCancel(model.getPublicKey());
    }
}
