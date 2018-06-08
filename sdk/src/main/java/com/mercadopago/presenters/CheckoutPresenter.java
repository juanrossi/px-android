package com.mercadopago.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.hooks.HookHelper;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.lite.exceptions.CheckoutPreferenceException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.Cause;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.plugins.DataInitializationTask;
import com.mercadopago.plugins.model.BusinessPayment;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import com.mercadopago.providers.CheckoutProvider;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.TextUtil;
import com.mercadopago.util.TextUtils;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.CheckoutStateModel;
import com.mercadopago.viewmodel.OneTapModel;
import com.mercadopago.views.CheckoutView;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CheckoutPresenter extends MvpPresenter<CheckoutView, CheckoutProvider> {

    private static final String INTERNAL_SERVER_ERROR_FIRST_DIGIT = "5";

    private CheckoutStateModel state;

    private transient FailureRecovery failureRecovery;

    private DataInitializationTask dataInitializationTask; //instance saved as attribute to cancel and avoid crash

    public CheckoutPresenter(final CheckoutStateModel persistentData) {
        this.state = persistentData;
    }

    public Serializable getState() {
        return state;
    }

    public void initialize() {
        getView().showProgress();
        configurePreference();
    }

    private void configurePreference() {
        if (TextUtil.isEmpty(state.checkoutPreferenceId)) { // custom checkout preference
            startCheckoutForPreference();
        } else {
            retrieveCheckoutPreference(state.checkoutPreferenceId);
        }
    }

    private void startCheckoutForPreference() {
        try {
            state.checkoutPreference.validate();
            getView().initializeMPTracker();
            if (isNewFlow()) {
                getView().trackScreen();
            }
            startCheckout();
        } catch (CheckoutPreferenceException e) {
            String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, false));
        }
    }

    private boolean isNewFlow() {
        return state.paymentDataInput == null && state.paymentResultInput == null;
    }

    private void startCheckout() {
        getResourcesProvider().fetchFonts();
        fetchImages();
        resolvePreSelectedData();
        initializePluginsData();
    }

    private void fetchImages() {
        if (state.paymentResultScreenPreference != null && getView() != null) {
            if (!TextUtils.isEmpty(state.paymentResultScreenPreference.getApprovedUrlIcon())) {
                getView().fetchImageFromUrl(state.paymentResultScreenPreference.getApprovedUrlIcon());
            }
            if (!TextUtils.isEmpty(state.paymentResultScreenPreference.getRejectedUrlIcon())) {
                getView().fetchImageFromUrl(state.paymentResultScreenPreference.getRejectedUrlIcon());
            }
            if (!TextUtils.isEmpty(state.paymentResultScreenPreference.getPendingUrlIcon())) {
                getView().fetchImageFromUrl(state.paymentResultScreenPreference.getPendingUrlIcon());
            }
        }
    }

    private void initializePluginsData() {
        final CheckoutStore store = CheckoutStore.getInstance();
        dataInitializationTask = store.getDataInitializationTask();
        if (dataInitializationTask != null) {
            dataInitializationTask.execute(getDataInitializationCallback());
        } else {
            store.getData().put(DataInitializationTask.KEY_INIT_SUCCESS, true);
            finishInitializingPluginsData();
        }
    }

    @NonNull
    private DataInitializationTask.DataInitializationCallbacks getDataInitializationCallback() {
        return new DataInitializationTask.DataInitializationCallbacks() {
            @Override
            public void onDataInitialized(@NonNull final Map<String, Object> data) {
                data.put(DataInitializationTask.KEY_INIT_SUCCESS, true);
                finishInitializingPluginsData();
            }

            @Override
            public void onFailure(@NonNull Exception e, @NonNull Map<String, Object> data) {
                data.put(DataInitializationTask.KEY_INIT_SUCCESS, false);
                finishInitializingPluginsData();
            }
        };
    }

    private void finishInitializingPluginsData() {
        if (isViewAttached() && getView().isActive()) {
            if (shouldRetrieveDiscount()) {
                getDiscountCampaigns();
            } else {
                retrievePaymentMethodSearch();
            }
        }
    }

    @VisibleForTesting
    boolean shouldRetrieveDiscount() {
        CheckoutStore store = CheckoutStore.getInstance();
        return !store.hasEnabledPaymentMethodPlugin() && !store.hasPaymentProcessor();
    }

    private void resolvePreSelectedData() {
        if (state.paymentDataInput != null) {
            state.selectedIssuer = state.paymentDataInput.getIssuer();
            state.selectedPayerCost = state.paymentDataInput.getPayerCost();
            state.createdToken = state.paymentDataInput.getToken();
            state.selectedPaymentMethod = state.paymentDataInput.getPaymentMethod();
            if (state.discount == null) {
                state.discount = state.paymentDataInput.getDiscount();
            }
        } else if (state.paymentResultInput != null && state.paymentResultInput.getPaymentData() != null) {
            state.selectedPaymentMethod = state.paymentResultInput.getPaymentData().getPaymentMethod();
            state.selectedPayerCost = state.paymentResultInput.getPaymentData().getPayerCost();
            state.selectedIssuer = state.paymentResultInput.getPaymentData().getIssuer();
            state.createdToken = state.paymentResultInput.getPaymentData().getToken();
            if (state.discount == null) {
                state.discount = state.paymentResultInput.getPaymentData().getDiscount();
            }
        }
    }

    private void getDiscountCampaigns() {
        getResourcesProvider().getDiscountCampaigns(onCampaignsRetrieved());
    }

    private TaggedCallback<List<Campaign>> onCampaignsRetrieved() {
        return new TaggedCallback<List<Campaign>>(ApiUtil.RequestOrigin.GET_CAMPAIGNS) {
            @Override
            public void onSuccess(List<Campaign> campaigns) {
                if (isViewAttached()) {
                    analyzeCampaigns(campaigns);
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                state.campaign = null;
                state.discount = null;

                if (isViewAttached()) {
                    retrievePaymentMethodSearch();
                }
            }
        };
    }

    private void analyzeCampaigns(final List<Campaign> campaigns) {
        if (campaigns == null || campaigns.size() == 0) {
            retrievePaymentMethodSearch();
        } else {
            for (Campaign campaign : campaigns) {
                if (campaign.isDirectDiscountCampaign()) {
                    state.campaign = campaign;
                    getDirectDiscount();
                }
            }

            //TODO set couponDiscount;
        }
    }

    private void getDirectDiscount() {
        String payerEmail =
            state.checkoutPreference.getPayer() == null ? "" : state.checkoutPreference.getPayer().getEmail();
        getResourcesProvider().getDirectDiscount(state.checkoutPreference.getTotalAmount(), payerEmail,
            new TaggedCallback<Discount>(ApiUtil.RequestOrigin.GET_DIRECT_DISCOUNT) {
                @Override
                public void onSuccess(final Discount discount) {
                    if (isViewAttached()) {
                        CheckoutPresenter.this.state.discount = discount;
                        retrievePaymentMethodSearch();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    CheckoutPresenter.this.state.discount = null;
                    if (isViewAttached()) {
                        retrievePaymentMethodSearch();
                    }
                }
            });
    }

    private void retrievePaymentMethodSearch() {
        final Payer payer = new Payer();
        payer.setAccessToken(state.checkoutPreference.getPayer().getAccessToken());

        getResourcesProvider().getPaymentMethodSearch(
            getTransactionAmount(),
            state.checkoutPreference.getExcludedPaymentTypes(),
            state.checkoutPreference.getExcludedPaymentMethods(),
            getResourcesProvider().getCardsWithEsc(),
            CheckoutStore.getInstance().getEnabledPaymentMethodPluginsIds(),
            payer,
            state.checkoutPreference.getSite(),
            onPaymentMethodSearchRetrieved(),
            onCustomerRetrieved()
        );

    }

    public BigDecimal getTransactionAmount() {
        BigDecimal amount;

        if (state.discount != null) {
            amount = state.discount.getAmountWithDiscount(state.checkoutPreference.getTotalAmount());
        } else {
            amount = state.checkoutPreference.getTotalAmount();
        }

        return amount;
    }

    private TaggedCallback<PaymentMethodSearch> onPaymentMethodSearchRetrieved() {
        return new TaggedCallback<PaymentMethodSearch>(ApiUtil.RequestOrigin.PAYMENT_METHOD_SEARCH) {
            @Override
            public void onSuccess(final PaymentMethodSearch paymentMethodSearch) {
                if (isViewAttached()) {
                    CheckoutPresenter.this.state.paymentMethodSearch = paymentMethodSearch;
                    startFlow();
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                if (isViewAttached()) {
                    getView().showError(error);
                }
            }
        };
    }

    private TaggedCallback<Customer> onCustomerRetrieved() {
        return new TaggedCallback<Customer>(ApiUtil.RequestOrigin.PAYMENT_METHOD_SEARCH) {
            @Override
            public void onSuccess(final Customer customer) {
                if (customer != null) {
                    state.customerId = customer.getId();
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                //Do nothing
            }
        };
    }

    private void startFlow() {
        if (state.paymentDataInput != null) {
            showReviewAndConfirm();
        } else if (state.paymentResultInput != null && state.paymentResultInput.getPaymentData() != null) {
            checkStartPaymentResultActivity(state.paymentResultInput);
        } else if (hasOneTapInfo()) {
            getView().hideProgress();
            getView().showOneTap(OneTapModel.from(state, CheckoutStore.getInstance().getReviewAndConfirmPreferences()));
        } else {
            getView().showPaymentMethodSelection();
        }
    }

    private void showReviewAndConfirm() {
        getView().showReviewAndConfirm();
        state.editPaymentMethodFromReviewAndConfirm = false;
    }

    public void checkStartPaymentResultActivity(final PaymentResult paymentResult) {
        if (hasToDeleteESC(paymentResult)) {
            deleteESC(paymentResult.getPaymentData());
        }
        if (hasToContinuePaymentWithoutESC(paymentResult)) {
            continuePaymentWithoutESC();
        } else {

            if (hasToStoreESC(paymentResult)) {
                getResourcesProvider().saveESC(paymentResult.getPaymentData().getToken().getCardId(),
                    paymentResult.getPaymentData().getToken().getEsc());
            }

            if (hasToSkipPaymentResultScreen(paymentResult)) {
                finishCheckout();
            } else {
                getView().showPaymentResult(paymentResult);
            }
        }
    }

    private boolean hasToStoreESC(final PaymentResult paymentResult) {
        return hasValidParametersForESC(paymentResult) &&
            paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_APPROVED) &&
            paymentResult.getPaymentData().getToken().getEsc() != null &&
            !paymentResult.getPaymentData().getToken().getEsc().isEmpty();
    }

    private boolean hasValidParametersForESC(final PaymentResult paymentResult) {
        return paymentResult != null && paymentResult.getPaymentData() != null &&
            paymentResult.getPaymentData().getToken() != null &&
            paymentResult.getPaymentData().getToken().getCardId() != null &&
            !paymentResult.getPaymentData().getToken().getCardId().isEmpty() &&
            paymentResult.getPaymentStatus() != null &&
            paymentResult.getPaymentStatusDetail() != null;
    }

    private boolean hasToDeleteESC(final PaymentResult paymentResult) {
        return hasValidParametersForESC(paymentResult) &&
            !paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_APPROVED);
    }

    private boolean hasToContinuePaymentWithoutESC(final PaymentResult paymentResult) {
        return hasValidParametersForESC(paymentResult) &&
            paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_REJECTED) &&
            paymentResult.getPaymentStatusDetail().equals(Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);
    }

    private boolean hasToSkipPaymentResultScreen(final PaymentResult paymentResult) {
        String status = paymentResult == null ? "" : paymentResult.getPaymentStatus();
        return shouldSkipResult(status);
    }

    private boolean shouldSkipResult(final String paymentStatus) {
        return !state.flowPreference.isPaymentResultScreenEnabled()
            || (
            state.flowPreference.getCongratsDisplayTime() != null &&
                state.flowPreference.getCongratsDisplayTime() == 0 &&
                Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus))
            || Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus) &&
            !state.flowPreference.isPaymentApprovedScreenEnabled()
            || Payment.StatusCodes.STATUS_REJECTED.equals(paymentStatus) &&
            !state.flowPreference.isPaymentRejectedScreenEnabled()
            || Payment.StatusCodes.STATUS_PENDING.equals(paymentStatus) &&
            !state.flowPreference.isPaymentPendingScreenEnabled();
    }

    private boolean isReviewAndConfirmEnabled() {
        return state.flowPreference.isReviewAndConfirmScreenEnabled();
    }

    public boolean isInstallmentsReviewScreenEnabled() {
        return state.flowPreference.isInstallmentsReviewScreenEnabled();
    }

    public boolean isESCEnabled() {
        return state.flowPreference.isESCEnabled();
    }

    public Card getSelectedCard() {
        return state.selectedCard;
    }

    public ServicePreference getServicePreference() {
        return state.servicePreference;
    }

    private void retrieveCheckoutPreference(final String checkoutPreferenceId) {
        getResourcesProvider().getCheckoutPreference(checkoutPreferenceId,
            new TaggedCallback<CheckoutPreference>(ApiUtil.RequestOrigin.GET_PREFERENCE) {

                @Override
                public void onSuccess(final CheckoutPreference checkoutPreference) {
                    CheckoutPresenter.this.state.checkoutPreference = checkoutPreference;
                    if (isViewAttached()) {
                        startCheckoutForPreference();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                retrieveCheckoutPreference(checkoutPreferenceId);
                            }
                        });
                    }
                }
            });
    }

    public void onErrorCancel(final MercadoPagoError mercadoPagoError) {
        if (isIdentificationInvalidInPayment(mercadoPagoError)) {
            getView().showPaymentMethodSelection();
        } else {
            cancelCheckout();
        }
    }

    private boolean isIdentificationInvalidInPayment(final MercadoPagoError mercadoPagoError) {
        boolean identificationInvalid = false;
        if (mercadoPagoError != null && mercadoPagoError.isApiException()) {
            List<Cause> causeList = mercadoPagoError.getApiException().getCause();
            if (causeList != null && !causeList.isEmpty()) {
                Cause cause = causeList.get(0);
                if (cause.getCode().equals(ApiException.ErrorCodes.INVALID_IDENTIFICATION_NUMBER)) {
                    identificationInvalid = true;
                }
            }
        }
        return identificationInvalid;
    }

    public void onPaymentMethodSelectionResponse(final PaymentMethod paymentMethod,
        final Issuer issuer,
        final PayerCost payerCost,
        final Token token,
        final Discount discount,
        final Card card,
        final Payer payer) {
        state.selectedPaymentMethod = paymentMethod;
        state.selectedIssuer = issuer;
        state.selectedPayerCost = payerCost;
        state.createdToken = token;
        this.state.discount = discount;
        state.selectedCard = card;
        state.collectedPayer = payer;

        onPaymentMethodSelected();
    }

    private void onPaymentMethodSelected() {
        if (!showHook2(createPaymentData())) {
            hook2Continue();
        }
    }

    public void resolvePaymentDataResponse() {
        if (MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE == state.requestedResult) {
            PaymentData paymentData = createPaymentData();
            getView().finishWithPaymentDataResult(paymentData, state.paymentMethodEdited);
        } else {
            createPayment();
        }
    }

    private void createPayment() {

        final PaymentData paymentData = createPaymentData();

        if (hasCustomPaymentProcessor()) {

            CheckoutStore.getInstance().setPaymentData(paymentData);
            getView().showPaymentProcessor();
        } else {
            final String transactionId = getTransactionID();
            getResourcesProvider().createPayment(transactionId,
                state.checkoutPreference,
                paymentData,
                state.binaryMode,
                state.customerId,
                new TaggedCallback<Payment>(ApiUtil.RequestOrigin.CREATE_PAYMENT) {
                    @Override
                    public void onSuccess(final Payment payment) {
                        state.createdPayment = payment;
                        PaymentResult paymentResult = createPaymentResult(payment, paymentData);
                        checkStartPaymentResultActivity(paymentResult);
                        cleanTransactionId();
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {
                        if (isErrorInvalidPaymentWithEsc(error, paymentData)) {
                            deleteESC(paymentData);
                            continuePaymentWithoutESC();
                        } else {
                            recoverCreatePayment(error);
                        }
                    }
                });
        }
    }

    private boolean isErrorInvalidPaymentWithEsc(MercadoPagoError error, PaymentData paymentData) {
        if (error.isApiException() && error.getApiException().getStatus() == ApiUtil.StatusCodes.BAD_REQUEST) {
            List<Cause> causes = error.getApiException().getCause();
            if (causes != null && !causes.isEmpty()) {
                Cause cause = causes.get(0);
                return ApiException.ErrorCodes.INVALID_PAYMENT_WITH_ESC.equals(cause.getCode()) &&
                    paymentData.getToken().getCardId() != null;
            }
        }
        return false;
    }

    private boolean hasCustomPaymentProcessor() {
        return CheckoutStore.getInstance().doesPaymentProcessorSupportPaymentMethodSelected() != null;
    }

    private void continuePaymentWithoutESC() {
        state.paymentRecovery = new PaymentRecovery(state.createdToken, state.selectedPaymentMethod,
            state.selectedPayerCost, state.selectedIssuer, Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);

        getView().startPaymentRecoveryFlow(state.paymentRecovery);
    }

    private void deleteESC(final PaymentData paymentData) {
        getResourcesProvider().deleteESC(paymentData.getToken().getCardId());
    }

    private void recoverCreatePayment(final MercadoPagoError error) {
        setFailureRecovery(new FailureRecovery() {
            @Override
            public void recover() {
                createPayment();
            }
        });
        resolvePaymentFailure(error);
    }

    private void resolvePaymentFailure(final MercadoPagoError mercadoPagoError) {

        if (isPaymentProcessing(mercadoPagoError)) {
            resolveProcessingPaymentStatus();
        } else if (isInternalServerError(mercadoPagoError)) {
            resolveInternalServerError(mercadoPagoError);
        } else if (isBadRequestError(mercadoPagoError)) {
            resolveBadRequestError(mercadoPagoError);
        } else {
            getView().showError(mercadoPagoError);
        }
    }

    private boolean isBadRequestError(final MercadoPagoError mercadoPagoError) {
        return mercadoPagoError != null && mercadoPagoError.getApiException() != null
            && (mercadoPagoError.getApiException().getStatus() == ApiUtil.StatusCodes.BAD_REQUEST);
    }

    private boolean isInternalServerError(final MercadoPagoError mercadoPagoError) {
        return mercadoPagoError != null && mercadoPagoError.getApiException() != null
            && String.valueOf(mercadoPagoError.getApiException().getStatus())
            .startsWith(INTERNAL_SERVER_ERROR_FIRST_DIGIT);
    }

    private boolean isPaymentProcessing(final MercadoPagoError mercadoPagoError) {
        return mercadoPagoError != null && mercadoPagoError.getApiException() != null
            && mercadoPagoError.getApiException().getStatus() == ApiUtil.StatusCodes.PROCESSING;
    }

    private void resolveInternalServerError(final MercadoPagoError mercadoPagoError) {
        getView().showError(mercadoPagoError);
        setFailureRecovery(new FailureRecovery() {
            @Override
            public void recover() {
                createPayment();
            }
        });
    }

    private void resolveProcessingPaymentStatus() {
        state.createdPayment = new Payment();
        state.createdPayment.setStatus(Payment.StatusCodes.STATUS_IN_PROCESS);
        state.createdPayment.setStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY);
        PaymentResult paymentResult = createPaymentResult(state.createdPayment, createPaymentData());
        getView().showPaymentResult(paymentResult);
        cleanTransactionId();
    }

    private void resolveBadRequestError(final MercadoPagoError mercadoPagoError) {
        getView().showError(mercadoPagoError);
    }

    public void onPaymentMethodSelectionError(final MercadoPagoError mercadoPagoError) {
        if (!state.editPaymentMethodFromReviewAndConfirm) {
            getView().cancelCheckout(mercadoPagoError);
        } else {
            state.editPaymentMethodFromReviewAndConfirm = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onPaymentMethodSelectionCancel() {
        if (!state.editPaymentMethodFromReviewAndConfirm) {
            cancelCheckout();
        } else {
            state.editPaymentMethodFromReviewAndConfirm = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onPaymentConfirmation() {
        if (!showHook3(createPaymentData())) {
            resolvePaymentDataResponse();
        }
    }

    public void onReviewAndConfirmCancel() {
        if (state.flowPreference.shouldExitOnPaymentMethodChange() && !isUniquePaymentMethod()) {
            getView().exitCheckout(MercadoPagoCheckout.PAYMENT_METHOD_CHANGED_REQUESTED);
        } else if (isUniquePaymentMethod()) {
            getView().cancelCheckout();
        } else {
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
        }
    }

    public void onReviewAndConfirmError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onPaymentResultCancel(final String nextAction) {
        if (!TextUtils.isEmpty(nextAction)) {
            if (nextAction.equals(PaymentResult.SELECT_OTHER_PAYMENT_METHOD)) {
                state.paymentMethodEdited = true;
                getView().showPaymentMethodSelection();
            } else if (nextAction.equals(PaymentResult.RECOVER_PAYMENT)) {
                recoverPayment();
            }
        }
    }

    public void onPaymentResultResponse() {
        finishCheckout();
    }

    public void onCardFlowResponse(final PaymentMethod paymentMethod,
        final Issuer issuer,
        final PayerCost payerCost,
        final Token token,
        final Discount discount) {

        state.selectedPaymentMethod = paymentMethod;
        state.selectedIssuer = issuer;
        state.selectedPayerCost = payerCost;
        state.createdToken = token;
        state.discount = discount;

        if (isRecoverableTokenProcess()) {
            resolvePaymentDataResponse();
        } else {
            onPaymentMethodSelected();
        }
    }

    public void onCardFlowError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onCardFlowCancel() {
        state.paymentMethodEdited = true;
        getView().showPaymentMethodSelection();
    }

    public void onCustomReviewAndConfirmResponse(final Integer customResultCode) {
        getView().cancelCheckout(customResultCode, state.paymentMethodEdited);
    }

    public void onCustomPaymentResultResponse(final Integer customResultCode) {
        if (state.createdPayment == null) {
            getView().finishWithPaymentResult(customResultCode);
        } else {
            getView().finishWithPaymentResult(customResultCode, state.createdPayment);
        }
    }

    public boolean isUniquePaymentMethod() {
        final CheckoutStore store = CheckoutStore.getInstance();
        int pluginCount = store.getPaymentMethodPluginCount();
        int groupCount = 0;
        int customCount = 0;

        if (state.paymentMethodSearch != null && state.paymentMethodSearch.hasSearchItems()) {
            groupCount = state.paymentMethodSearch.getGroups().size();
            if (pluginCount == 0 && groupCount == 1 && state.paymentMethodSearch.getGroups().get(0).isGroup()) {
                return false;
            }
        }

        if (state.paymentMethodSearch != null && state.paymentMethodSearch.hasCustomSearchItems()) {
            customCount = state.paymentMethodSearch.getCustomSearchItems().size();
        }

        return groupCount + customCount + pluginCount == 1;
    }

    private PaymentResult createPaymentResult(final Payment payment, final PaymentData paymentData) {
        return new PaymentResult.Builder()
            .setPaymentData(paymentData)
            .setPaymentId(payment.getId())
            .setPaymentStatus(payment.getStatus())
            .setPaymentStatusDetail(payment.getStatusDetail())
            .setPayerEmail(state.checkoutPreference.getPayer().getEmail())
            .setStatementDescription(payment.getStatementDescriptor())
            .build();
    }

    private PaymentData createPaymentData() {

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(state.selectedPaymentMethod);
        paymentData.setPayerCost(state.selectedPayerCost);
        paymentData.setIssuer(state.selectedIssuer);
        paymentData.setDiscount(state.discount);
        paymentData.setToken(state.createdToken);
        paymentData.setTransactionAmount(state.checkoutPreference.getTotalAmount());
        Payer payer = createPayerFrom(state.checkoutPreference.getPayer(), state.collectedPayer);
        paymentData.setPayer(payer);

        return paymentData;
    }

    private Payer createPayerFrom(final Payer checkoutPreferencePayer,
        final Payer collectedPayer) {
        Payer payerForPayment;
        if (checkoutPreferencePayer != null && collectedPayer != null) {
            payerForPayment = copy(checkoutPreferencePayer);
            payerForPayment.setFirstName(collectedPayer.getFirstName());
            payerForPayment.setLastName(collectedPayer.getLastName());
            payerForPayment.setIdentification(collectedPayer.getIdentification());
        } else {
            payerForPayment = checkoutPreferencePayer;
        }
        return payerForPayment;
    }

    private Payer copy(final Payer original) {
        return JsonUtil.getInstance().fromJson(JsonUtil.getInstance().toJson(original), Payer.class);
    }

    private void recoverPayment() {
        try {
            PaymentResult paymentResult =
                state.paymentResultInput == null ? CheckoutStore.getInstance().getPaymentResult()
                    : state.paymentResultInput;
            String paymentStatus =
                state.createdPayment == null ? paymentResult.getPaymentStatus() : state.createdPayment.getStatus();
            String paymentStatusDetail = state.createdPayment == null ? paymentResult.getPaymentStatusDetail()
                : state.createdPayment.getStatusDetail();
            state.paymentRecovery =
                new PaymentRecovery(state.createdToken, state.selectedPaymentMethod, state.selectedPayerCost,
                    state.selectedIssuer, paymentStatus, paymentStatusDetail);
            getView().startPaymentRecoveryFlow(state.paymentRecovery);
        } catch (IllegalStateException e) {
            String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, e.getMessage(), false));
        }
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        } else {
            IllegalStateException e = new IllegalStateException("Failure recovery not defined");
            getView().showError(new MercadoPagoError(getResourcesProvider().getCheckoutExceptionMessage(e), false));
        }
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    private boolean isRecoverableTokenProcess() {
        return state.paymentRecovery != null && state.paymentRecovery.isTokenRecoverable();
    }

    private String getTransactionID() {
        if (!existsTransactionId() || state.paymentMethodEdited) {
            state.currentPaymentIdempotencyKey = createNewTransactionId();
        }
        return state.currentPaymentIdempotencyKey;
    }

    private String createNewTransactionId() {
        return state.idempotencyKeySeed + Calendar.getInstance().getTimeInMillis();
    }

    private boolean existsTransactionId() {
        return state.currentPaymentIdempotencyKey != null;
    }

    private void cleanTransactionId() {
        state.currentPaymentIdempotencyKey = null;
    }

    public PaymentMethod getSelectedPaymentMethod() {
        return state.selectedPaymentMethod;
    }

    public Issuer getIssuer() {
        return state.selectedIssuer;
    }

    public PayerCost getSelectedPayerCost() {
        return state.selectedPayerCost;
    }

    public Token getCreatedToken() {
        return state.createdToken;
    }

    public Payment getCreatedPayment() {
        return state.createdPayment;
    }

    public PaymentResultScreenPreference getPaymentResultScreenPreference() {
        return state.paymentResultScreenPreference;
    }

    public Integer getCongratsDisplay() {
        return state.flowPreference.getCongratsDisplayTime();
    }

    public CheckoutPreference getCheckoutPreference() {
        return state.checkoutPreference;
    }

    public PaymentMethodSearch getPaymentMethodSearch() {
        return state.paymentMethodSearch;
    }

    public void onPaymentMethodSelected(final PaymentMethodSearch paymentMethodSearch) {
        this.state.paymentMethodSearch = paymentMethodSearch;
    }

    public Discount getDiscount() {
        return state.discount;
    }

    public Campaign getCampaign() {
        return state.campaign;
    }

    public Boolean getShowBankDeals() {
        return state.flowPreference.isBankDealsEnabled() && state.servicePreference.shouldShowBankDeals();
    }

    public boolean shouldShowAllSavedCards() {
        return state.flowPreference.isShowAllSavedCardsEnabled();
    }

    public Integer getMaxSavedCardsToShow() {
        return state.flowPreference.getMaxSavedCardsToShow();
    }

    //### Hooks #####################

    private boolean showHook2(final PaymentData paymentData) {
        return showHook2(paymentData, MercadoPagoComponents.Activities.HOOK_2);
    }

    private boolean showHook2(final PaymentData paymentData, final int requestCode) {
        final Map<String, Object> data = CheckoutStore.getInstance().getData();
        final Hook hook = HookHelper.activateAfterPaymentMethodConfig(
            CheckoutStore.getInstance().getCheckoutHooks(), paymentData, data);
        if (hook != null && getView() != null) {
            getView().showHook(hook, requestCode);
            return true;
        }
        return false;
    }

    private boolean showHook3(final PaymentData paymentData) {
        return showHook3(paymentData, MercadoPagoComponents.Activities.HOOK_3);
    }

    private boolean showHook3(final PaymentData paymentData, final int requestCode) {
        final Map<String, Object> data = CheckoutStore.getInstance().getData();
        final Hook hook = HookHelper.activateBeforePayment(
            CheckoutStore.getInstance().getCheckoutHooks(), paymentData, data);
        if (hook != null && getView() != null) {
            getView().showHook(hook, requestCode);
            return true;
        }
        return false;
    }

    public void hook2Continue() {
        state.editPaymentMethodFromReviewAndConfirm = false;
        if (isReviewAndConfirmEnabled()) {
            showReviewAndConfirm();
        } else {
            resolvePaymentDataResponse();
        }
    }

    public void cancelInitialization() {
        if (dataInitializationTask != null) {
            dataInitializationTask.cancel();
        }
    }

    public void onBusinessResult(final BusinessPayment businessPayment) {
        //TODO look for a better option than singleton, it make it not testeable.
        PaymentData paymentData = CheckoutStore.getInstance().getPaymentData();

        final String lastFourDigits =
            paymentData.getToken() != null ? paymentData.getToken().getLastFourDigits() : null;
        BusinessPaymentModel model =
            new BusinessPaymentModel(businessPayment, state.discount, paymentData.getPaymentMethod(),
                paymentData.getPayerCost(),
                state.checkoutPreference.getSite().getCurrencyId(),
                paymentData.getTransactionAmount(), lastFourDigits);
        getView().showBusinessResult(model);
    }

    private void finishCheckout() {
        if (state.createdPayment == null) {
            getView().finishWithPaymentResult();
        } else {
            getView().finishWithPaymentResult(state.createdPayment);
        }
    }

    /**
     * Send intention to close checkout
     * if the checkout has oneTap data then it should not close.
     */
    public void cancelCheckout() {
        if (hasOneTapInfo()) {
            getView().hideProgress();
        } else {
            getView().cancelCheckout();
        }
    }

    private boolean hasOneTapInfo() {
        return (state.paymentMethodSearch != null
            && state.paymentMethodSearch.hasOneTapMetadata());
    }

    /**
     * Close checkout with resCode
     */
    public void exitWithCode(final int resCode) {
        getView().exitCheckout(resCode);
    }

    public void onChangePaymentMethodFromReviewAndConfirm() {
        //TODO remove when navigation is corrected and works with stack.
        onChangePaymentMethod(true);
    }

    public void onChangePaymentMethod() {
        onChangePaymentMethod(false);
    }

    private void onChangePaymentMethod(final boolean fromReviewAndConfirm) {
        if (state.flowPreference.shouldExitOnPaymentMethodChange()) {
            getView().exitCheckout(MercadoPagoCheckout.PAYMENT_METHOD_CHANGED_REQUESTED);
        } else {
            //TODO remove when navigation is corrected and works with stack.
            state.editPaymentMethodFromReviewAndConfirm = fromReviewAndConfirm;
            state.paymentMethodEdited = true;
            getView().showProgress();
            getView().showPaymentMethodSelection();
        }
    }

    public void startCardPayment(@NonNull final CardPaymentModel cardPaymentModel) {
        state.selectedPayerCost = cardPaymentModel.payerCost;
        state.createdToken = cardPaymentModel.token;
        state.selectedCard = cardPaymentModel.card;
        state.selectedIssuer = cardPaymentModel.issuer;
        state.selectedPaymentMethod = cardPaymentModel.card.getPaymentMethod();
        getView().showProgress();
        createPayment();
    }

    public void startPayment(@NonNull final PaymentMethod paymentMethod) {
        // TODO refactor // see hooks options
        state.selectedPaymentMethod = paymentMethod;
        getView().showProgress();
        createPayment();
    }
}