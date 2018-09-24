package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS;

public class GuessingCardStoragePresenter extends GuessingCardPresenter {

    private final String mAccessToken;
    /* default */ MercadoPagoESC mercadoPagoESC;
    private PaymentMethod currentPaymentMethod;

    public GuessingCardStoragePresenter(final String accessToken, final MercadoPagoESC mercadoPagoESC) {
        super();
        mAccessToken = accessToken;
        this.mercadoPagoESC = mercadoPagoESC;
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
    public void createToken() {
        getResourcesProvider()
            .createTokenAsync(mCardToken, mAccessToken, new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
                @Override
                public void onSuccess(final Token token) {
                    resolveTokenRequest(token);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    resolveTokenCreationError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
                }
            });
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        getResourcesProvider().associateCardToUser(mAccessToken, token.getId(), getPaymentMethod().getId(),
            new TaggedCallback<Card>(ApiUtil.RequestOrigin.ASSOCIATE_CARD) {
                @Override
                public void onSuccess(final Card card) {
                    if (isViewAttached()) {
                        mercadoPagoESC.saveESC(card.getId(), token.getEsc());
                        getView().finishCardStorageFlow(card.getId());
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    getView().setErrorView(error.getMessage());
                }
            });
    }

    @Nullable
    @Override
    public List<BankDeal> getBankDealsList() {
        return null;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        if (getPaymentMethod() != null) {
            super.onSaveInstanceState(outState, cardSideState, lowResActive);
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }
}
