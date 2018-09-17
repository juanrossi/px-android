package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS;

public class GuessingCardStoragePresenter extends GuessingCardPresenter {

    private final String mAccessToken;
    private PaymentMethod currentPaymentMethod;

    public GuessingCardStoragePresenter(final String accessToken) {
        super();
        mAccessToken = accessToken;
    }

    @Override
    public void initialize() {
        getView().onValidStart();
        getView().hideBankDeals();
        initializeCardToken();
        getPaymentMethods();
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        if (currentPaymentMethod != null) {
            return currentPaymentMethod.getPaymentTypeId();
        }
        return null;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return currentPaymentMethod;
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        currentPaymentMethod = paymentMethod;
        if (paymentMethod == null) {
            clearCardSettings();
        }
    }

    @Override
    public void getIdentificationTypesAsync() {
        getResourcesProvider().getIdentificationTypesAsync(mAccessToken,
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    resolveIdentificationTypes(identificationTypes);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getIdentificationTypesAsync();
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void getPaymentMethods() {
        getView().showProgress();
        getResourcesProvider()
            .getCardPaymentMethods(mAccessToken, new TaggedCallback<List<PaymentMethod>>(GET_CARD_PAYMENT_METHODS) {
                @Override
                public void onSuccess(final List<PaymentMethod> paymentMethods) {
                    if (isViewAttached()) {
                        getView().hideProgress();
                        mPaymentMethodGuessingController = new
                            PaymentMethodGuessingController(paymentMethods, null, null);
                        startGuessingForm();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().hideProgress();
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getPaymentMethods();
                            }
                        });
                        getView().showError(error, ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS);
                    }
                }
            });
    }

    @Override
    public void onPaymentMethodSet(final PaymentMethod paymentMethod) {
        setPaymentMethod(paymentMethod);
        configureWithSettings(paymentMethod);
        loadIdentificationTypes(paymentMethod);
        getView().setPaymentMethod(paymentMethod);
        getView().resolvePaymentMethodSet(paymentMethod);
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        getView().finishCardStorageFlow("asd");
    }

    @Nullable
    @Override
    public List<BankDeal> getBankDealsList() {
        return null;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        //TODO: implement
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        //TODO: implement
    }
}
