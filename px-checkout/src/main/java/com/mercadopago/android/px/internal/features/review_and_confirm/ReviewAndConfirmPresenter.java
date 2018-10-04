package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.DefaultProvider;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.viewmodel.mappers.BusinessModelMapper;

/* default */ final class ReviewAndConfirmPresenter extends MvpPresenter<ReviewAndConfirm.View, DefaultProvider>
    implements ReviewAndConfirm.Action {

    @NonNull private final PaymentRepository paymentRepository;
    @NonNull private final BusinessModelMapper businessModelMapper;
    @NonNull private final DynamicDialogConfiguration dynamicDialogConfiguration;
    @NonNull private final CheckoutPreference checkoutPreference;
    private final ExplodeDecoratorMapper explodeDecoratorMapper;
    private FailureRecovery recovery;

    /* default */ ReviewAndConfirmPresenter(@NonNull final PaymentRepository paymentRepository,
        @NonNull final BusinessModelMapper businessModelMapper,
        @NonNull final DynamicDialogConfiguration dynamicDialogConfiguration,
        @NonNull final CheckoutPreference checkoutPreference) {
        this.paymentRepository = paymentRepository;
        this.businessModelMapper = businessModelMapper;
        this.dynamicDialogConfiguration = dynamicDialogConfiguration;
        this.checkoutPreference = checkoutPreference;
        explodeDecoratorMapper = new ExplodeDecoratorMapper();
    }

    @Override
    public void attachView(final ReviewAndConfirm.View view) {
        super.attachView(view);
        paymentRepository.attach(this);
        resolveDynamicDialog(DynamicDialogConfiguration.DialogLocation.ENTER_REVIEW_AND_CONFIRM);
    }

    private void resolveDynamicDialog(@NonNull final DynamicDialogConfiguration.DialogLocation location) {
        final DynamicDialogCreator.CheckoutData checkoutData =
            new DynamicDialogCreator.CheckoutData(checkoutPreference, paymentRepository.getPaymentData());
        if (dynamicDialogConfiguration.hasCreatorFor(location) &&
            dynamicDialogConfiguration.getCreatorFor(location).shouldShowDialog(checkoutData)) {
            getView().showDynamicDialog(
                dynamicDialogConfiguration.getCreatorFor(location)
                    .create(checkoutData));
        }
    }

    @Override
    public void detachView() {
        paymentRepository.detach();
        super.detachView();
    }

    @Override
    public void onPaymentConfirm() {
        getView().trackPaymentConfirmation();
        pay();
    }

    private void pay() {
        if (paymentRepository.isExplodingAnimationCompatible()) {
            getView().startLoadingButton(paymentRepository.getPaymentTimeout());
            getView().hideConfirmButton();
        }
        // TODO improve: This was added because onetap can detach this listener on its OnDestroy
        paymentRepository.attach(this);
        paymentRepository.startPayment();
    }

    @Override
    public void onCardFlowResponse() {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        pay();
    }

    @Override
    public void onCardFlowCancel() {
        // TODO do nothing
        // TODO check if it's needed
        // TODO Reset UI - Exploading button
    }

    @Override
    public void onError(@NonNull final MercadoPagoError mercadoPagoError) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().cancelCheckoutAndInformError(mercadoPagoError);
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().showCardCVVRequired(card);
    }

    @Override
    public void onVisualPayment() {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().showPaymentProcessor();
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().startPaymentRecoveryFlow(recovery);
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(payment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showResult(paymentRepository.createPaymentResult(payment));
                }
            });
    }

    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(genericPayment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showResult(paymentRepository.createPaymentResult(genericPayment));
                }
            });
    }

    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        getView().showLoadingFor(explodeDecoratorMapper.map(businessPayment),
            new ExplodingFragment.ExplodingAnimationListener() {
                @Override
                public void onAnimationFinished() {
                    getView().showResult(businessModelMapper.map(businessPayment));
                }
            });
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();

        recovery = new FailureRecovery() {
            @Override
            public void recover() {
                pay();
            }
        };

        if (error.isPaymentProcessing()) {
            final PaymentResult paymentResult =
                new PaymentResult.Builder()
                    .setPaymentData(paymentRepository.getPaymentData())
                    .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
                    .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
                    .build();
            getView().showResult(paymentResult);
        } else if (error.isInternalServerError() || error.isNoConnectivityError()) {
            getView().showErrorSnackBar(error);
        } else {
            getView().showErrorScreen(error);
        }
    }

    @Override
    public void recoverFromFailure() {
        if (recovery != null) {
            recovery.recover();
        }
    }

    @Override
    public void executePostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        postPaymentAction.execute(new PostPaymentAction.ActionController() {
            @Override
            public void recoverFromReviewAndConfirm(@NonNull final PostPaymentAction postPaymentAction) {
                getView().startPaymentRecoveryFlow(paymentRepository.createPaymentRecovery());
            }

            @Override
            public void recoverFromOneTap() {
                //do nothing
            }

            @Override
            public void changePaymentMethod() {
                //do nothing
            }
        });
    }
}
