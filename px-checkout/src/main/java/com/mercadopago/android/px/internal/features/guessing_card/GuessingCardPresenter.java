package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.providers.GuessingCardProvider;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.features.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.internal.tracker.FlowHandler;
import com.mercadopago.android.px.internal.tracker.MPTrackingContext;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.MPCardMaskUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Bin;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Cardholder;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.model.SecurityCode;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.utils.TrackingUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.mercadopago.android.px.model.Card.CARD_DEFAULT_SECURITY_CODE_LENGTH;

public abstract class GuessingCardPresenter extends MvpPresenter<GuessingCardActivityView, GuessingCardProvider> {

    protected static final String CARD_SIDE_STATE_BUNDLE = "mCardSideState";
    protected static final String PAYMENT_METHOD_BUNDLE = "paymentMethod";
    protected static final String ID_REQUIRED_BUNDLE = "mIdentificationNumberRequired";
    protected static final String SEC_CODE_REQUIRED_BUNDLE = "mIsSecurityCodeRequired";
    protected static final String SEC_CODE_LENGTH_BUNDLE = "mCardSecurityCodeLength";
    protected static final String CARD_NUMBER_LENGTH_BUNDLE = "mCardNumberLength";
    protected static final String SEC_CODE_LOCATION_BUNDLE = "mSecurityCodeLocation";
    protected static final String CARD_TOKEN_BUNDLE = "mCardToken";
    protected static final String CARD_INFO_BIN_BUNDLE = "mBin";
    protected static final String EXPIRY_MONTH_BUNDLE = "mExpiryMonth";
    protected static final String EXPIRY_YEAR_BUNDLE = "mExpiryYear";
    protected static final String CARD_NUMBER_BUNDLE = "mCardNumber";
    protected static final String CARD_NAME_BUNDLE = "mCardName";
    protected static final String IDENTIFICATION_BUNDLE = "mIdentification";
    protected static final String IDENTIFICATION_NUMBER_BUNDLE = "mIdentificationNumber";
    protected static final String IDENTIFICATION_TYPE_BUNDLE = "mIdentificationType";
    protected static final String PAYMENT_TYPES_LIST_BUNDLE = "mPaymentTypesList";
    protected static final String BANK_DEALS_LIST_BUNDLE = "mBankDealsList";
    protected static final String IDENTIFICATION_TYPES_LIST_BUNDLE = "mIdTypesList";
    protected static final String PAYMENT_RECOVERY_BUNDLE = "mPaymentRecovery";
    protected static final String LOW_RES_BUNDLE = "mLowRes";
    //Card Info
    protected String mBin;
    protected boolean mShowPaymentTypes;
    protected boolean mEraseSpace;
    //Activity parameters
    protected PaymentMethodGuessingController mPaymentMethodGuessingController;
    protected Identification mIdentification;
    protected Token mToken;
    protected CardToken mCardToken;
    // Extra info
    private List<PaymentType> mPaymentTypesList;
    private List<IdentificationType> mIdentificationTypes;
    private String mCardNumber;
    private int mCurrentNumberLength;
    private int mSecurityCodeLength;
    private String mSecurityCodeLocation;
    private String mCardholderName;
    private String mExpiryMonth;
    private String mExpiryYear;
    private IdentificationType mIdentificationType;
    private String mIdentificationNumber;
    private boolean mIsSecurityCodeRequired;
    private boolean mIdentificationNumberRequired;
    private FailureRecovery mFailureRecovery;
    private String mSecurityCode;

    public GuessingCardPresenter() {
        super();
        mToken = new Token();
        mIdentification = new Identification();
        mEraseSpace = true;
    }

    public static GuessingCardPresenter buildGuessingCardPaymentPresenter(final Session session,
        final PaymentRecovery paymentRecovery) {
        return new GuessingCardPaymentPresenter(session.getAmountRepository(),
            session.getConfigurationModule().getUserSelectionRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getGroupsRepository(),
            session.getConfigurationModule().getPaymentSettings().getAdvancedConfiguration(),
            paymentRecovery);
    }

    public static GuessingCardPresenter buildGuessingCardStoragePresenter(final String accessToken) {
        return new GuessingCardStoragePresenter(accessToken);
    }

    private MPTrackingContext getTrackingContext() {
        return getResourcesProvider().getTrackingContext();
    }

    protected void trackCardIdentification() {
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_IDENTIFICATION)
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_IDENTIFICATION_NUMBER)
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID,
                getPaymentTypeId() != null ? getPaymentTypeId() : "null")
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, getPaymentMethod() != null ?
                getPaymentMethod().getId() : "null")
            .build();
        getTrackingContext().trackEvent(event);
    }

    protected void trackCardNumber() {
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(
                String.format(Locale.US, "%s%s%s", TrackingUtil.SCREEN_ID_CARD_FORM, getPaymentTypeId(),
                    TrackingUtil.CARD_NUMBER))
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_NUMBER)
            .build();
        getTrackingContext().trackEvent(event);
    }

    protected void trackCardHolderName() {
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(
                String.format(Locale.US, "%s%s%s", TrackingUtil.SCREEN_ID_CARD_FORM, getPaymentTypeId(),
                    TrackingUtil.CARD_HOLDER_NAME))
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_NAME)
            .build();
        getTrackingContext().trackEvent(event);
    }

    protected void trackCardExpiryDate() {
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(String
                .format(Locale.US, "%s%s%s", TrackingUtil.SCREEN_ID_CARD_FORM, getPaymentTypeId(),
                    TrackingUtil.CARD_EXPIRATION_DATE))
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_EXPIRY)
            .build();
        getTrackingContext().trackEvent(event);
    }

    protected void trackCardSecurityCode() {
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(String
                .format(Locale.US, "%s%s%s", TrackingUtil.SCREEN_ID_CARD_FORM, getPaymentTypeId(),
                    TrackingUtil.CARD_SECURITY_CODE))
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_CVV)
            .build();
        getTrackingContext().trackEvent(event);
    }

    public void resolvePaymentMethodListSet(final List<PaymentMethod> paymentMethodList, final String bin) {
        saveBin(bin);
        if (paymentMethodList.isEmpty()) {
            getView().setCardNumberInputMaxLength(Bin.BIN_LENGTH);
            setInvalidCardMessage();
        } else if (paymentMethodList.size() == 1) {
            onPaymentMethodSet(paymentMethodList.get(0));
        } else if (shouldAskPaymentType(paymentMethodList)) {
            enablePaymentTypeSelection(paymentMethodList);
            onPaymentMethodSet(paymentMethodList.get(0));
        } else {
            onPaymentMethodSet(paymentMethodList.get(0));
        }
    }

    private void enablePaymentTypeSelection(final Iterable<PaymentMethod> paymentMethodList) {
        final List<PaymentType> paymentTypesList = new ArrayList<>();
        for (final PaymentMethod pm : paymentMethodList) {
            final PaymentType type = new PaymentType(pm.getPaymentTypeId());
            paymentTypesList.add(type);
        }
        mPaymentTypesList = paymentTypesList;

        mShowPaymentTypes = true;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public boolean shouldAskPaymentType(@NonNull final List<PaymentMethod> paymentMethodList) {
        final String paymentType = paymentMethodList.get(0).getPaymentTypeId();
        for (final PaymentMethod currentPaymentMethod : paymentMethodList) {
            if (!paymentType.equals(currentPaymentMethod.getPaymentTypeId())) {
                return true;
            }
        }
        return false;
    }

    private void setInvalidCardMessage() {
        if (onlyOnePaymentMethodSupported()) {
            getView().setInvalidCardOnePaymentMethodErrorView();
        } else {
            getView().setInvalidCardMultipleErrorView();
        }
    }

    private boolean onlyOnePaymentMethodSupported() {
        final List<PaymentMethod> supportedPaymentMethods = getAllSupportedPaymentMethods();
        return supportedPaymentMethods != null && supportedPaymentMethods.size() == 1;
    }

    public List<PaymentMethod> getAllSupportedPaymentMethods() {
        if (mPaymentMethodGuessingController != null) {
            return mPaymentMethodGuessingController.getAllSupportedPaymentMethods();
        }
        return Collections.emptyList();
    }

    protected void saveBin(@Nullable final String bin) {
        mBin = bin;
        if (mPaymentMethodGuessingController != null) {
            mPaymentMethodGuessingController.saveBin(bin);
        }
    }

    public void saveCardNumber(final String cardNumber) {
        mCardNumber = cardNumber;
    }

    public void setCurrentNumberLength(final int currentNumberLength) {
        mCurrentNumberLength = currentNumberLength;
    }

    public int getSecurityCodeLength() {
        return mSecurityCodeLength;
    }

    public String getSecurityCodeLocation() {
        return mSecurityCodeLocation;
    }

    public void setSecurityCodeLocation(final String securityCodeLocation) {
        mSecurityCodeLocation = securityCodeLocation;
    }

    public void saveCardholderName(final String cardholderName) {
        mCardholderName = cardholderName;
    }

    public void saveExpiryMonth(final String expiryMonth) {
        mExpiryMonth = expiryMonth;
    }

    public void saveExpiryYear(final String expiryYear) {
        mExpiryYear = expiryYear;
    }

    public void saveSecurityCode(final String securityCode) {
        mSecurityCode = securityCode;
    }

    public void saveIdentificationType(final IdentificationType identificationType) {
        mIdentificationType = identificationType;
        if (identificationType != null) {
            mIdentification.setType(identificationType.getId());
            getView().setIdentificationNumberRestrictions(identificationType.getType());
        }
    }

    public void saveIdentificationNumber(final String identificationNumber) {
        mIdentificationNumber = identificationNumber;
    }

    public int getIdentificationNumberMaxLength() {
        if (mIdentificationType != null) {
            return mIdentificationType.getMaxLength();
        } else {
            return Card.CARD_DEFAULT_IDENTIFICATION_NUMBER_LENGTH;
        }
    }

    public String getIdentificationNumber() {
        return mIdentificationNumber;
    }

    public void setIdentificationNumber(final String number) {
        mIdentificationNumber = number;
        mIdentification.setNumber(number);
    }

    public boolean validateIdentificationNumber() {
        mIdentification.setNumber(getIdentificationNumber());
        mCardToken.getCardholder().setIdentification(mIdentification);
        final boolean validated = mCardToken.validateIdentificationNumber(mIdentificationType);
        if (validated) {
            getView().clearErrorView();
            getView().clearErrorIdentificationNumber();
        } else {
            getView().setErrorView(getResourcesProvider().getInvalidIdentificationNumberErrorMessage());
            getView().setErrorIdentificationNumber();
        }
        return validated;
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    public void setCardNumber(@Nullable final String cardNumber) {
        mCardNumber = cardNumber;
    }

    public String getCardholderName() {
        return mCardholderName;
    }

    public void setCardholderName(@Nullable final String name) {
        mCardholderName = name;
    }

    public boolean validateCardName() {
        final Cardholder cardHolder = new Cardholder();
        cardHolder.setName(getCardholderName());
        cardHolder.setIdentification(mIdentification);
        mCardToken.setCardholder(cardHolder);
        if (mCardToken.validateCardholderName()) {
            getView().clearErrorView();
            return true;
        } else {
            getView().setErrorView(getResourcesProvider().getInvalidEmptyNameErrorMessage());
            getView().setErrorCardholderName();
            return false;
        }
    }

    public boolean validateExpiryDate() {
        final String monthString = getExpiryMonth();
        final String yearString = getExpiryYear();
        final Integer month = (monthString == null || monthString.isEmpty()) ? null : Integer.valueOf(monthString);
        final Integer year = (yearString == null || yearString.isEmpty()) ? null : Integer.valueOf(yearString);
        mCardToken.setExpirationMonth(month);
        mCardToken.setExpirationYear(year);
        if (mCardToken.validateExpiryDate()) {
            getView().clearErrorView();
            return true;
        } else {
            getView().setErrorView(getResourcesProvider().getInvalidExpiryDateErrorMessage());
            getView().setErrorExpiryDate();
            return false;
        }
    }

    public boolean checkIsEmptyOrValidExpiryDate() {
        return TextUtils.isEmpty(mExpiryMonth) || validateExpiryDate();
    }

    public String getExpiryMonth() {
        return mExpiryMonth;
    }

    public void setExpiryMonth(@Nullable final String expiryMonth) {
        mExpiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return mExpiryYear;
    }

    public void setExpiryYear(@Nullable final String expiryYear) {
        mExpiryYear = expiryYear;
    }

    public boolean isSecurityCodeRequired() {
        return mIsSecurityCodeRequired;
    }

    /* default */ void setSecurityCodeRequired(final boolean required) {
        mIsSecurityCodeRequired = required;
    }

    public boolean isIdentificationNumberRequired() {
        return mIdentificationNumberRequired;
    }

    protected void setIdentificationNumberRequired(final boolean identificationNumberRequired) {
        mIdentificationNumberRequired = identificationNumberRequired;
        getView().showIdentificationInput();
    }

    public boolean checkIsEmptyOrValidCardholderName() {
        return TextUtils.isEmpty(mCardholderName) || validateCardName();
    }

    public boolean checkIsEmptyOrValidSecurityCode() {
        return TextUtils.isEmpty(mSecurityCode) || validateSecurityCode();
    }

    public boolean checkIsEmptyOrValidIdentificationNumber() {
        return TextUtils.isEmpty(mIdentificationNumber) || validateIdentificationNumber();
    }

    public String getSecurityCode() {
        return mSecurityCode;
    }

    @Nullable
    public String getSecurityCodeFront() {
        String securityCode = null;
        if (mSecurityCodeLocation.equals(CardView.CARD_SIDE_FRONT)) {
            securityCode = getSecurityCode();
        }
        return securityCode;
    }

    /* default */ List<PaymentType> getPaymentTypes() {
        return mPaymentTypesList;
    }

    /* default */
    void resolveTokenCreationError(final MercadoPagoError error, final String requestOrigin) {
        if (isIdentificationNumberWrong(error)) {
            showIdentificationNumberError();
        } else {
            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    createToken();
                }
            });
            getView().showError(error, requestOrigin);
        }
    }

    private boolean isIdentificationNumberWrong(final MercadoPagoError error) {
        return error.isApiException() &&
            error.getApiException().containsCause(ApiException.ErrorCodes.INVALID_CARD_HOLDER_IDENTIFICATION_NUMBER);
    }

    private void showIdentificationNumberError() {
        getView().hideProgress();
        getView().setErrorView(getResourcesProvider().getInvalidFieldErrorMessage());
        getView().setErrorIdentificationNumber();
    }

    public void setSelectedPaymentType(final PaymentType paymentType) {
        if (mPaymentMethodGuessingController == null) {
            return;
        }
        for (final PaymentMethod paymentMethod : mPaymentMethodGuessingController.getGuessedPaymentMethods()) {
            if (paymentMethod.getPaymentTypeId().equals(paymentType.getId())) {
                setPaymentMethod(paymentMethod);
            }
        }
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public List<IdentificationType> getIdentificationTypes() {
        return mIdentificationTypes;
    }

    /* default */ void loadIdentificationTypes(final PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return;
        }
        mIdentificationNumberRequired = paymentMethod.isIdentificationNumberRequired();
        if (mIdentificationNumberRequired) {
            getIdentificationTypesAsync();
        } else {
            getView().hideIdentificationInput();
        }
    }

    protected void resolveIdentificationTypes(final List<IdentificationType> identificationTypes) {
        if (identificationTypes.isEmpty()) {
            getView().showError(
                new MercadoPagoError(getResourcesProvider().getMissingIdentificationTypesErrorMessage(), false),
                ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
        } else {
            mIdentificationType = identificationTypes.get(0);
            getView().initializeIdentificationTypes(identificationTypes);
            mIdentificationTypes = identificationTypes;
        }
    }

    protected void configureWithSettings(final PaymentMethod paymentMethod) {
        if (paymentMethod != null) {
            mIsSecurityCodeRequired = paymentMethod.isSecurityCodeRequired(mBin);
            if (!mIsSecurityCodeRequired) {
                getView().hideSecurityCodeInput();
            }
            final Setting setting =
                Setting.getSettingByPaymentMethodAndBin(paymentMethod, mBin);
            if (setting == null) {
                getView()
                    .showError(
                        new MercadoPagoError(getResourcesProvider().getSettingNotFoundForBinErrorMessage(), false),
                        "");
            } else {
                final int cardNumberLength = getCardNumberLength();
                int spaces = FrontCardView.CARD_DEFAULT_AMOUNT_SPACES;

                if (cardNumberLength == FrontCardView.CARD_NUMBER_DINERS_LENGTH ||
                    cardNumberLength == FrontCardView.CARD_NUMBER_AMEX_LENGTH ||
                    cardNumberLength == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_1_LENGTH) {
                    spaces = FrontCardView.CARD_AMEX_DINERS_AMOUNT_SPACES;
                } else if (cardNumberLength == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_LENGTH) {
                    spaces = FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_AMOUNT_SPACES;
                }
                getView().setCardNumberInputMaxLength(cardNumberLength + spaces);
                final SecurityCode securityCode = setting.getSecurityCode();
                if (securityCode == null) {
                    mSecurityCodeLength = CARD_DEFAULT_SECURITY_CODE_LENGTH;
                    mSecurityCodeLocation = CardView.CARD_SIDE_BACK;
                } else {
                    mSecurityCodeLength = securityCode.getLength();
                    mSecurityCodeLocation = securityCode.getCardLocation();
                }
                getView().setSecurityCodeInputMaxLength(mSecurityCodeLength);
                getView().setSecurityCodeViewLocation(mSecurityCodeLocation);
            }
        }
    }

    protected void startGuessingForm() {
        getView().initializeTitle();
        getView().setCardNumberListeners(mPaymentMethodGuessingController);
        getView().setCardholderNameListeners();
        getView().setExpiryDateListeners();
        getView().setSecurityCodeListeners();
        getView().setIdentificationTypeListeners();
        getView().setIdentificationNumberListeners();
        getView().setNextButtonListeners();
        getView().setBackButtonListeners();
        getView().setErrorContainerListener();
        getView().setContainerAnimationListeners();
        checkPaymentMethodsSupported(false);
    }

    private void checkPaymentMethodsSupported(final boolean withAnimation) {
        final List<PaymentMethod> supportedPaymentMethods = getAllSupportedPaymentMethods();
        if (supportedPaymentMethods != null && supportedPaymentMethods.size() == 1) {
            getView().setExclusionWithOneElementInfoView(getAllSupportedPaymentMethods().get(0), withAnimation);
        }
    }

    protected void initializeCardToken() {
        mCardToken = new CardToken("", null, null,
            "", "", "", "");
    }

    protected int getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(getPaymentMethod(), mBin);
    }

    protected Identification getIdentification() {
        return mIdentification;
    }

    public void setIdentification(final Identification identification) {
        mIdentification = identification;
    }

    public Token getToken() {
        return mToken;
    }

    public void setToken(final Token token) {
        mToken = token;
    }

    public CardToken getCardToken() {
        return mCardToken;
    }

    protected void setCardToken(final CardToken cardToken) {
        mCardToken = cardToken;
    }

    protected void setPaymentTypesList(@Nullable final List<PaymentType> paymentTypesList) {
        mPaymentTypesList = paymentTypesList;
    }

    public boolean isDefaultSpaceErasable() {
        if (MPCardMaskUtil.isDefaultSpaceErasable(mCurrentNumberLength)) {
            mEraseSpace = true;
        }

        if (getPaymentMethod() != null && mBin != null && mEraseSpace &&
            (getCardNumberLength() == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_1_LENGTH ||
                getCardNumberLength() == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_LENGTH)) {
            mEraseSpace = false;
            return true;
        }
        return false;
    }

    public FailureRecovery getFailureRecovery() {
        return mFailureRecovery;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public boolean validateSecurityCode() {
        mCardToken.setSecurityCode(getSecurityCode());
        try {
            mCardToken.validateSecurityCode(getPaymentMethod());
            getView().clearErrorView();
            return true;
        } catch (final CardTokenException e) {
            setCardSecurityCodeErrorView(e);
            return false;
        }
    }

    private void setCardSecurityCodeErrorView(final CardTokenException exception) {
        if (!isSecurityCodeRequired()) {
            return;
        }
        getView().setErrorView(exception);
        getView().setErrorSecurityCode();
    }

    public boolean validateCardNumber() {
        mCardToken.setCardNumber(getCardNumber());
        try {
            final PaymentMethod paymentMethod = getPaymentMethod();
            if (paymentMethod == null) {
                if (getCardNumber() == null || getCardNumber().length() < Bin.BIN_LENGTH) {
                    throw new CardTokenException(CardTokenException.INVALID_CARD_NUMBER_INCOMPLETE);
                } else if (getCardNumber().length() == Bin.BIN_LENGTH) {
                    throw new CardTokenException(CardTokenException.INVALID_PAYMENT_METHOD);
                } else {
                    throw new CardTokenException(CardTokenException.INVALID_PAYMENT_METHOD);
                }
            }
            mCardToken.validateCardNumber(paymentMethod);
            getView().clearErrorView();
            return true;
        } catch (final CardTokenException e) {
            getView().setErrorView(e);
            getView().setErrorCardNumber();
            return false;
        }
    }

    public PaymentMethodGuessingController getGuessingController() {
        return mPaymentMethodGuessingController;
    }

    @Nullable
    protected List<PaymentMethod> getGuessedPaymentMethods() {
        if (mPaymentMethodGuessingController == null) {
            return null;
        }
        return mPaymentMethodGuessingController.getGuessedPaymentMethods();
    }

    protected IdentificationType getIdentificationType() {
        return mIdentificationType;
    }

    public void setIdentificationType(final IdentificationType identificationType) {
        mIdentificationType = identificationType;
    }

    protected void clearCardSettings() {
        mSecurityCodeLength = CARD_DEFAULT_SECURITY_CODE_LENGTH;
        mSecurityCodeLocation = CardView.CARD_SIDE_BACK;
        mIsSecurityCodeRequired = true;
        mBin = "";
    }

    public void trackScreen() {
        final String paymentTypeId = getPaymentTypeId();
        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(String.format(Locale.US, "%s%s", TrackingUtil.SCREEN_ID_CARD_FORM, paymentTypeId))
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM + " " + paymentTypeId)
            .build();

        getTrackingContext().trackEvent(event);
    }

    public void resolvePaymentMethodCleared() {
        setPaymentMethod(null);
        getView().clearErrorView();
        getView().hideRedErrorContainerView(true);
        getView().restoreBlackInfoContainerView();
        getView().clearCardNumberInputLength();
        mEraseSpace = true;
        setPaymentMethod(null);
        getView().clearSecurityCodeEditText();
        initializeCardToken();
        setIdentificationNumberRequired(true);
        setSecurityCodeRequired(true);
        mShowPaymentTypes = false;
        mPaymentTypesList = null;
        getView().checkClearCardView();
        checkPaymentMethodsSupported(true);
    }

    public String getSavedBin() {
        return mBin;
    }

    public void checkFinishWithCardToken() {
        if (mShowPaymentTypes && getGuessedPaymentMethods() != null) {
            getView().askForPaymentType(getGuessedPaymentMethods(), getPaymentTypes(), new CardInfo(getCardToken()));
        } else {
            getView().showFinishCardFlow();
        }
    }

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

    public abstract void initialize();

    public abstract String getPaymentTypeId();

    public abstract PaymentMethod getPaymentMethod();

    public abstract void setPaymentMethod(@Nullable final PaymentMethod paymentMethod);

    public abstract void getIdentificationTypesAsync();

    public abstract void createToken();

    public abstract void getPaymentMethods();

    public abstract void resolveTokenRequest(final Token token);

    public abstract List<BankDeal> getBankDealsList();

    public void onSaveInstanceState(final Bundle outState, final String cardSideState,
        final boolean lowResActive) {
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
        outState.putString(IDENTIFICATION_TYPES_LIST_BUNDLE,
            JsonUtil.getInstance().toJson(getIdentificationTypes()));
        outState.putBoolean(LOW_RES_BUNDLE, lowResActive);
        getView().clearSecurityCodeEditText();
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        final String paymentMethodBundleJson = savedInstanceState.getString(PAYMENT_METHOD_BUNDLE);
        if (!TextUtil.isEmpty(paymentMethodBundleJson)) {
            final PaymentMethod pm = JsonUtil.getInstance()
                .fromJson(paymentMethodBundleJson, PaymentMethod.class);
            if (pm != null) {
                List<IdentificationType> identificationTypesList;
                try {
                    final Type listType = new TypeToken<List<IdentificationType>>() {
                    }.getType();
                    identificationTypesList = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(IDENTIFICATION_TYPES_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    identificationTypesList = null;
                }
                resolveIdentificationTypes(identificationTypesList);
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
                final boolean lowResActive = savedInstanceState.getBoolean(LOW_RES_BUNDLE);
                getView().recoverCardViews(lowResActive, getCardNumber(), getCardholderName(), getExpiryMonth(),
                    getExpiryYear(), idNumber, identificationType);
                onPaymentMethodSet(pm);
            }
        }
    }
}
