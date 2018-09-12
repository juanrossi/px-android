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
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.DifferentialPricing;
import com.mercadopago.android.px.model.Identification;
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
import java.lang.reflect.Type;
import java.util.List;

public class GuessingCardPaymentPresenter extends GuessingCardPresenter {

    @NonNull private final AmountRepository mAmountRepository;
    @NonNull private final UserSelectionRepository mUserSelectionRepository;
    @NonNull private final PaymentSettingRepository mPaymentSettingRepository;
    @NonNull private final GroupsRepository mGroupsRepository;
    @NonNull private final AdvancedConfiguration mAdvancedConfiguration;

    //Card controller
    /* default */ PaymentPreference mPaymentPreference;

    //Extra info
    private List<BankDeal> mBankDealsList;

    //Discount
    private Issuer mIssuer;

    public GuessingCardPaymentPresenter(@NonNull final AmountRepository amountRepository,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final GroupsRepository groupsRepository,
        @NonNull final AdvancedConfiguration advancedConfiguration,
        @NonNull final PaymentPreference paymentPreference,
        @NonNull final PaymentRecovery paymentRecovery
    ) {
        super();
        mAmountRepository = amountRepository;
        mUserSelectionRepository = userSelectionRepository;
        mPaymentSettingRepository = paymentSettingRepository;
        mGroupsRepository = groupsRepository;
        mAdvancedConfiguration = advancedConfiguration;
        mPaymentPreference = paymentPreference;
        mPaymentRecovery = paymentRecovery;
        mToken = new Token();
        mIdentification = new Identification();
        mEraseSpace = true;
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

    public PaymentPreference getPaymentPreference() {
        return mPaymentPreference;
    }

    public void setPaymentPreference(final PaymentPreference paymentPreference) {
        mPaymentPreference = paymentPreference;
    }

    @Override
    public void getPaymentMethods() {
        mGroupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                mPaymentMethodGuessingController = new PaymentMethodGuessingController(
                    mPaymentPreference.getSupportedPaymentMethods(paymentMethodSearch.getPaymentMethods()),
                    mPaymentPreference.getDefaultPaymentTypeId(),
                    mPaymentPreference.getExcludedPaymentTypes());
                startGuessingForm();
            }

            @Override
            public void failure(final ApiException apiException) {
                createToken();
            }
        });
    }

    @Override
    public String getPaymentTypeId() {
        if (mPaymentMethodGuessingController == null) {
            if (mPaymentPreference == null) {
                return null;
            } else {
                return mPaymentPreference.getDefaultPaymentTypeId();
            }
        } else {
            return mPaymentMethodGuessingController.getPaymentTypeId();
        }
    }

    private void resolveBankDeals() {
        if (mAdvancedConfiguration.isBankDealsEnabled()) {
            getBankDealsAsync();
        } else {
            getView().hideBankDeals();
        }
    }

    @Override
    public void onPaymentMethodSet(final PaymentMethod paymentMethod) {
        setPaymentMethod(paymentMethod);
        configureWithSettings(paymentMethod);
        loadIdentificationTypes(paymentMethod);
        getView().setPaymentMethod(paymentMethod);
        getView().resolvePaymentMethodSet(paymentMethod);
        //We need to erase default space in position 4 in some special cases.
        if (isDefaultSpaceErasable()) {
            getView().eraseDefaultSpace();
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
            outState.putString(CARD_SIDE_STATE_BUNDLE, cardSideState);
            outState.putString(PAYMENT_METHOD_BUNDLE, JsonUtil.getInstance().toJson(getPaymentMethod()));
            outState.putBoolean(ID_REQUIRED_BUNDLE, isIdentificationNumberRequired());
            outState.putBoolean(SEC_CODE_REQUIRED_BUNDLE, isSecurityCodeRequired());
            outState.putInt(SEC_CODE_LENGTH_BUNDLE, getSecurityCodeLength());
            outState.putInt(CARD_NUMBER_LENGTH_BUNDLE, getCardNumberLength());
            outState.putString(SEC_CODE_LOCATION_BUNDLE, getSecurityCodeLocation());
            outState.putString(CARD_TOKEN_BUNDLE, JsonUtil.getInstance().toJson(getCardToken()));
            outState.putString(CARD_INFO_BIN_BUNDLE, getSavedBin());
            outState.putString(CARD_NUMBER_BUNDLE, getCardNumber());
            outState.putString(CARD_NAME_BUNDLE, getCardholderName());
            outState.putString(EXPIRY_MONTH_BUNDLE, getExpiryMonth());
            outState.putString(EXPIRY_YEAR_BUNDLE, getExpiryYear());
            outState.putString(IDENTIFICATION_BUNDLE, JsonUtil.getInstance().toJson(getIdentification()));
            outState.putString(IDENTIFICATION_NUMBER_BUNDLE, getIdentificationNumber());
            outState.putString(IDENTIFICATION_TYPE_BUNDLE,
                JsonUtil.getInstance().toJson(getIdentificationType()));
            outState.putString(PAYMENT_TYPES_LIST_BUNDLE, JsonUtil.getInstance().toJson(getPaymentTypes()));
            outState.putString(BANK_DEALS_LIST_BUNDLE, JsonUtil.getInstance().toJson(getBankDealsList()));
            outState.putString(IDENTIFICATION_TYPES_LIST_BUNDLE,
                JsonUtil.getInstance().toJson(getIdentificationTypes()));
            outState.putString(PAYMENT_RECOVERY_BUNDLE, JsonUtil.getInstance().toJson(getPaymentRecovery()));
            outState.putBoolean(LOW_RES_BUNDLE, lowResActive);
            getView().clearSecurityCodeEditText();
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
            final String paymentMethodBundleJson = savedInstanceState.getString(PAYMENT_METHOD_BUNDLE);
            if (!TextUtil.isEmpty(paymentMethodBundleJson)) {
                final PaymentMethod pm = JsonUtil.getInstance()
                    .fromJson(paymentMethodBundleJson, PaymentMethod.class);
                if (pm != null) {
                    List<PaymentType> paymentTypesList;
                    try {
                        final Type listType = new TypeToken<List<PaymentType>>() {
                        }.getType();
                        paymentTypesList = JsonUtil.getInstance().getGson().fromJson(
                            savedInstanceState.getString(PAYMENT_TYPES_LIST_BUNDLE), listType);
                    } catch (final Exception ex) {
                        paymentTypesList = null;
                    }
                    List<BankDeal> bankDealsList;
                    try {
                        final Type listType = new TypeToken<List<BankDeal>>() {
                        }.getType();
                        bankDealsList = JsonUtil.getInstance().getGson().fromJson(
                            savedInstanceState.getString(BANK_DEALS_LIST_BUNDLE), listType);
                    } catch (final Exception ex) {
                        bankDealsList = null;
                    }
                    List<IdentificationType> identificationTypesList;
                    try {
                        final Type listType = new TypeToken<List<IdentificationType>>() {
                        }.getType();
                        identificationTypesList = JsonUtil.getInstance().getGson().fromJson(
                            savedInstanceState.getString(IDENTIFICATION_TYPES_LIST_BUNDLE), listType);
                    } catch (final Exception ex) {
                        identificationTypesList = null;
                    }
                    setPaymentTypesList(paymentTypesList);
                    resolveIdentificationTypes(identificationTypesList);
                    setBankDealsList(bankDealsList);
                    getPaymentMethods();
                    saveBin(savedInstanceState.getString(CARD_INFO_BIN_BUNDLE));
                    setIdentificationNumberRequired(savedInstanceState.getBoolean(ID_REQUIRED_BUNDLE));
                    setSecurityCodeRequired(savedInstanceState.getBoolean(SEC_CODE_REQUIRED_BUNDLE));
                    setCardNumber(savedInstanceState.getString(CARD_NUMBER_BUNDLE));
                    setCardholderName(savedInstanceState.getString(CARD_NAME_BUNDLE));
                    setExpiryMonth(savedInstanceState.getString(EXPIRY_MONTH_BUNDLE));
                    setExpiryYear(savedInstanceState.getString(EXPIRY_YEAR_BUNDLE));
                    final String idNumber = savedInstanceState.getString(IDENTIFICATION_NUMBER_BUNDLE);
                    setIdentificationNumber(idNumber);
                    final Identification identification = JsonUtil.getInstance()
                        .fromJson(savedInstanceState.getString(IDENTIFICATION_BUNDLE), Identification.class);
                    identification.setNumber(idNumber);
                    setIdentification(identification);
                    setSecurityCodeLocation(savedInstanceState.getString(SEC_CODE_LOCATION_BUNDLE));
                    final CardToken cardToken = JsonUtil.getInstance()
                        .fromJson(savedInstanceState.getString(CARD_TOKEN_BUNDLE), CardToken.class);
                    cardToken.getCardholder().setIdentification(identification);
                    final IdentificationType identificationType = JsonUtil.getInstance()
                        .fromJson(savedInstanceState.getString(IDENTIFICATION_TYPE_BUNDLE),
                            IdentificationType.class);
                    setCardToken(cardToken);
                    setIdentificationType(identificationType);
                    setPaymentRecovery(JsonUtil.getInstance()
                        .fromJson(savedInstanceState.getString(PAYMENT_RECOVERY_BUNDLE), PaymentRecovery.class));
                    final boolean lowResActive = savedInstanceState.getBoolean(LOW_RES_BUNDLE);
                    getView().recoverCardViews(lowResActive, getCardNumber(), getCardholderName(), getExpiryMonth(),
                        getExpiryYear(), idNumber, identificationType);
                    onPaymentMethodSet(pm);
                }
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
        final PayerCost defaultPayerCost = mPaymentPreference.getDefaultInstallments(payerCosts);
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

    @Override
    public void checkFinishWithCardToken() {
        if (mShowPaymentTypes && getGuessedPaymentMethods() != null) {
            getView().askForPaymentType(getGuessedPaymentMethods(), getPaymentTypes(), new CardInfo(getCardToken()));
        } else {
            getView().showFinishCardFlow();
        }
    }
}
