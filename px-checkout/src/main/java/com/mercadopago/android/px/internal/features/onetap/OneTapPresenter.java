package com.mercadopago.android.px.internal.features.onetap;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

/* default */ class OneTapPresenter extends MvpPresenter<OneTap.View, ResourcesProvider>
    implements OneTap.Actions {

    @NonNull private final OneTapModel model;
    @NonNull private final PaymentRepository paymentRepository;
    private final ExplodeDecoratorMapper explodeDecoratorMapper;

    /* default */ OneTapPresenter(@NonNull final OneTapModel model,
        @NonNull final PaymentRepository paymentRepository) {
        this.model = model;
        this.paymentRepository = paymentRepository;
        explodeDecoratorMapper = new ExplodeDecoratorMapper();
    }

    @Override
    public void confirmPayment() {
        getView().trackConfirm(model);
        getView().hideToolbar();

        if (paymentRepository.isExplodingAnimationCompatible()) {
            getView().startLoadingButton(paymentRepository.getPaymentTimeout());
            getView().hideConfirmButton();
        }

        // TODO improve: This was added because onetap can detach this listener on its OnDestroy
        paymentRepository.attach(this);
        paymentRepository.startOneTapPayment(model);
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
        getView().trackCancel();
    }

    @Override
    public void onTokenResolved() {
        getView().cancelLoading();
        confirmPayment();
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(payment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showPaymentResult(payment);
                }
            });
    }

    /**
     * When there is no visual interaction needed this callback is called.
     *
     * @param genericPayment plugin payment.
     */
    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(genericPayment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showPaymentResult(genericPayment);
                }
            });
    }

    /**
     * When there is no visual interaction needed this callback is called.
     *
     * @param businessPayment plugin payment.
     */
    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(businessPayment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showPaymentResult(businessPayment);
                }
            });
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        getView().cancelLoading();

        if (error.isInternalServerError() || error.isNoConnectivityError()) {
            getView().showErrorSnackBar(error);
        } else {
            getView().showErrorScreen(error);
        }
    }

    @Override
    public void onVisualPayment() {
        getView().showPaymentProcessor();
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        getView().cancelLoading();
        getView().showCardFlow(model, card);
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().onRecoverPaymentEscInvalid(recovery);
    }

    @Override
    public void onViewResumed(final OneTapModel model) {
        getView().updateViews(model);
        paymentRepository.attach(this);

        //If a payment was attempted, the exploding fragment is still visible when we go back to one tap fragment.
        //Example: call for authorize, after asking for cvv and pressing back, we go back to one tap and need to
        //remove the exploding fragment we had before.
        if (paymentRepository.hasPayment()) {
            final IPayment payment = paymentRepository.getPayment();
            final boolean isRecoverable = Payment.StatusDetail
                .isRecoverablePaymentStatus(payment.getPaymentStatus(), payment.getPaymentStatusDetail());
            if (isRecoverable) {
                getView().cancelLoading();
            }
        }
    }

    @Override
    public void attachView(final OneTap.View view) {
        super.attachView(view);
        paymentRepository.attach(this);
    }

    @Override
    public void onViewPaused() {
        paymentRepository.detach(this);
    }

    @Override
    public void detachView() {
        onViewPaused();
        super.detachView();
    }

}