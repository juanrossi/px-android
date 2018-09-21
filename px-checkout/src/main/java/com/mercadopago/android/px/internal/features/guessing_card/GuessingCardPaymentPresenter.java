package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.DifferentialPricing;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Installment;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import java.lang.reflect.Type;
import java.util.List;

public class GuessingCardPaymentPresenter extends GuessingCardPresenter {

    @NonNull final PaymentSettingRepository mPaymentSettingRepository;
    @NonNull private final AmountRepository mAmountRepository;
    @NonNull private final UserSelectionRepository mUserSelectionRepository;
    @NonNull private final GroupsRepository mGroupsRepository;
    @NonNull private final AdvancedConfiguration mAdvancedConfiguration;
    protected PaymentRecovery mPaymentRecovery;
    //Extra info
    private List<BankDeal> mBankDealsList;
    //Discount
    private Issuer mIssuer;

    public GuessingCardPaymentPresenter(@NonNull final AmountRepository amountRepository,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final GroupsRepository groupsRepository,
        @NonNull final AdvancedConfiguration advancedConfiguration,
        @NonNull final PaymentRecovery paymentRecovery
    ) {
        super();
        mAmountRepository = amountRepository;
        mUserSelectionRepository = userSelectionRepository;
        mPaymentSettingRepository = paymentSettingRepository;
        mGroupsRepository = groupsRepository;
        mAdvancedConfiguration = advancedConfiguration;
        mPaymentRecovery = paymentRecovery;
    }

    @Override
    public void initialize() {
        getView().onValidStart();
        trackScreen();
        initializeCardToken();
        resolveBankDeals();
        getPaymentMethods();
        if (recoverWithCardHolder()) {
            fillRecoveryFields();
        }
    }

    private void fillRecoveryFields() {
        getView().setCardholderName(mPaymentRecovery.getToken().getCardHolder().getName());
        getView()
            .setIdentificationNumber(mPaymentRecovery.getToken().getCardHolder().getIdentification().getNumber());
    }

    @Nullable
    @Override
    public PaymentMethod getPaymentMethod() {
        return mUserSelectionRepository.getPaymentMethod();
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        mUserSelectionRepository.select(paymentMethod);
        if (paymentMethod == null) {
            clearCardSettings();
        }
    }

    @Override
    public void getIdentificationTypesAsync() {
        getResourcesProvider().getIdentificationTypesAsync(
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
        mGroupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                if (isViewAttached()) {
                    getView().hideProgress();
                    final PaymentPreference paymentPreference =
                        mPaymentSettingRepository.getCheckoutPreference().getPaymentPreference();
                    mPaymentMethodGuessingController = new PaymentMethodGuessingController(
                        paymentPreference.getSupportedPaymentMethods(paymentMethodSearch.getPaymentMethods()),
                        getPaymentTypeId(),
                        paymentPreference.getExcludedPaymentTypes());
                    startGuessingForm();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                if (isViewAttached()) {
                    getView().hideProgress();
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            getPaymentMethods();
                        }
                    });
                }
            }
        });
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        return mUserSelectionRepository.getPaymentType();
    }

    private void resolveBankDeals() {
        if (mAdvancedConfiguration.isBankDealsEnabled()) {
            getBankDealsAsync();
        } else {
            getView().hideBankDeals();
        }
    }

    @Override
    public List<BankDeal> getBankDealsList() {
        return mBankDealsList;
    }

    private void setBankDealsList(@Nullable final List<BankDeal> bankDealsList) {
        mBankDealsList = bankDealsList;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        if (getPaymentMethod() != null) {
            super.onSaveInstanceState(outState, cardSideState, lowResActive);
            outState.putString(BANK_DEALS_LIST_BUNDLE, JsonUtil.getInstance().toJson(getBankDealsList()));
            outState.putString(PAYMENT_TYPES_LIST_BUNDLE, JsonUtil.getInstance().toJson(getPaymentTypes()));
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
            final String paymentMethodBundleJson = savedInstanceState.getString(PAYMENT_METHOD_BUNDLE);
            if (!TextUtil.isEmpty(paymentMethodBundleJson)) {
                List<PaymentType> paymentTypesList;
                try {
                    final Type listType = new TypeToken<List<PaymentType>>() {
                    }.getType();
                    paymentTypesList = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(PAYMENT_TYPES_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    paymentTypesList = null;
                }
                setPaymentTypesList(paymentTypesList);
                List<BankDeal> bankDealsList;
                try {
                    final Type listType = new TypeToken<List<BankDeal>>() {
                    }.getType();
                    bankDealsList = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(BANK_DEALS_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    bankDealsList = null;
                }
                setBankDealsList(bankDealsList);
                setPaymentRecovery(JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(PAYMENT_RECOVERY_BUNDLE), PaymentRecovery.class));
                super.onRestoreInstanceState(savedInstanceState);
            }
        }
    }

    /* default */ void getBankDealsAsync() {
        getResourcesProvider()
            .getBankDealsAsync(new TaggedCallback<List<BankDeal>>(ApiUtil.RequestOrigin.GET_BANK_DEALS) {
                @Override
                public void onSuccess(final List<BankDeal> bankDeals) {
                    resolveBankDeals(bankDeals);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getBankDealsAsync();
                            }
                        });
                    }
                }
            });
    }

    /* default */ void resolveBankDeals(final List<BankDeal> bankDeals) {
        if (isViewAttached()) {
            if (bankDeals == null || bankDeals.isEmpty()) {
                getView().hideBankDeals();
            } else {
                mBankDealsList = bankDeals;
                getView().showBankDeals();
            }
        }
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        mToken = token;
        mPaymentSettingRepository.configure(mToken);
        MPTracker.getInstance().trackToken(mToken.getId());
        getIssuers();
    }

    /* default */ void getIssuers() {
        final PaymentMethod paymentMethod = getPaymentMethod();
        if (paymentMethod != null) {
            getResourcesProvider().getIssuersAsync(paymentMethod.getId(), mBin,
                new TaggedCallback<List<Issuer>>(ApiUtil.RequestOrigin.GET_ISSUERS) {
                    @Override
                    public void onSuccess(final List<Issuer> issuers) {
                        resolveIssuersList(issuers);
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getIssuers();
                            }
                        });
                        getView().showError(error, ApiUtil.RequestOrigin.GET_ISSUERS);
                    }
                });
        }
    }

    /* default */ void resolveIssuersList(final List<Issuer> issuers) {
        if (issuers.size() == 1) {
            mIssuer = issuers.get(0);
            mUserSelectionRepository.select(mIssuer);
            getInstallments();
        } else {
            getView().finishCardFlow(getPaymentMethod(), mToken, issuers);
        }
    }

    /* default */ void getInstallments() {
        final CheckoutPreference checkoutPreference = mPaymentSettingRepository.getCheckoutPreference();
        if (checkoutPreference != null) {
            final DifferentialPricing differentialPricing = checkoutPreference.getDifferentialPricing();
            final Integer differentialPricingId = differentialPricing == null ? null : differentialPricing.getId();
            final PaymentMethod paymentMethod = getPaymentMethod();
            if (paymentMethod != null) {
                getResourcesProvider().getInstallmentsAsync(mBin, mAmountRepository.getAmountToPay(), mIssuer.getId(),
                    paymentMethod.getId(), differentialPricingId,
                    new TaggedCallback<List<Installment>>(ApiUtil.RequestOrigin.GET_INSTALLMENTS) {
                        @Override
                        public void onSuccess(final List<Installment> installments) {
                            resolveInstallments(installments);
                        }

                        @Override
                        public void onFailure(final MercadoPagoError error) {
                            setFailureRecovery(new FailureRecovery() {
                                @Override
                                public void recover() {
                                    getInstallments();
                                }
                            });
                            getView().showError(error, ApiUtil.RequestOrigin.GET_INSTALLMENTS);
                        }
                    });
            }
        }
    }

    /* default */ void resolveInstallments(final List<Installment> installments) {
        String errorMessage = null;
        if (installments == null || installments.isEmpty()) {
            errorMessage = getResourcesProvider().getMissingInstallmentsForIssuerErrorMessage();
        } else if (installments.size() == 1) {
            resolvePayerCosts(installments.get(0).getPayerCosts());
        } else {
            errorMessage = getResourcesProvider().getMultipleInstallmentsForIssuerErrorMessage();
        }
        if (errorMessage != null && isViewAttached()) {
            getView().showError(new MercadoPagoError(errorMessage, false), ApiUtil.RequestOrigin.GET_INSTALLMENTS);
        }
    }

    private void resolvePayerCosts(final List<PayerCost> payerCosts) {
        final PayerCost defaultPayerCost =
            mPaymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultInstallments(payerCosts);
        if (defaultPayerCost != null) {
            mUserSelectionRepository.select(defaultPayerCost);
            getView().finishCardFlow(getPaymentMethod(), mToken, mIssuer,
                defaultPayerCost);
        } else if (payerCosts.isEmpty()) {
            getView().showError(new MercadoPagoError(getResourcesProvider().getMissingPayerCostsErrorMessage(), false),
                ApiUtil.RequestOrigin.GET_INSTALLMENTS);
        } else if (payerCosts.size() == 1) {
            final PayerCost payerCost = payerCosts.get(0);
            mUserSelectionRepository.select(payerCost);
            getView().finishCardFlow(getPaymentMethod(), mToken, mIssuer,
                payerCost);
        } else {
            getView().finishCardFlow(getPaymentMethod(), mToken, mIssuer, payerCosts);
        }
    }

    public PaymentRecovery getPaymentRecovery() {
        return mPaymentRecovery;
    }

    public void setPaymentRecovery(final PaymentRecovery paymentRecovery) {
        mPaymentRecovery = paymentRecovery;
        if (recoverWithCardHolder()) {
            saveCardholderName(paymentRecovery.getToken().getCardHolder().getName());
            saveIdentificationNumber(paymentRecovery.getToken().getCardHolder().getIdentification().getNumber());
        }
    }

    protected boolean recoverWithCardHolder() {
        return mPaymentRecovery != null && mPaymentRecovery.getToken() != null &&
            mPaymentRecovery.getToken().getCardHolder() != null;
    }

    @Override
    public void createToken() {
        getResourcesProvider()
            .createTokenAsync(mCardToken, new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
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
}
