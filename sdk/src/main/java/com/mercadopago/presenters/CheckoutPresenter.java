package com.mercadopago.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.hooks.HookHelper;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.lite.exceptions.CheckoutPreferenceException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.Cause;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
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
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.providers.CheckoutProvider;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.TextUtils;
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.CheckoutStateModel;
import com.mercadopago.viewmodel.OneTapModel;
import com.mercadopago.views.CheckoutView;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CheckoutPresenter extends MvpPresenter<CheckoutView, CheckoutProvider> {

    private static final String INTERNAL_SERVER_ERROR_FIRST_DIGIT = "5";

    private CheckoutStateModel state;

    @NonNull private final PaymentSettingRepository paymentConfiguration;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final UserSelectionRepository userSelectionRepository;

    private transient FailureRecovery failureRecovery;

    private DataInitializationTask dataInitializationTask; //instance saved as attribute to cancel and avoid crash

    public CheckoutPresenter(final CheckoutStateModel persistentData,
        @NonNull final PaymentSettingRepository paymentConfiguration,
        @NonNull final AmountRepository amountRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.paymentConfiguration = paymentConfiguration;
        this.amountRepository = amountRepository;
        this.userSelectionRepository = userSelectionRepository;
        state = persistentData;
    }

    public Serializable getState() {
        return state;
    }

    public void initialize() {
        getView().showProgress();
        configurePreference();
    }

    private void configurePreference() {
        if (TextUtils.isEmpty(state.config.getPreferenceId())) { // custom checkout preference
            startCheckoutForPreference();
        } else {
            retrieveCheckoutPreference(state.config.getPreferenceId());
        }
    }

    private void startCheckoutForPreference() {
        try {
            getCheckoutPreference().validate();
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
        final PaymentResultScreenPreference resultPref =
            state.config.getPaymentResultScreenPreference();
        if (resultPref != null && getView() != null) {
            if (!TextUtils.isEmpty(resultPref.getApprovedUrlIcon())) {
                getView().fetchImageFromUrl(resultPref.getApprovedUrlIcon());
            }
            if (!TextUtils.isEmpty(resultPref.getRejectedUrlIcon())) {
                getView().fetchImageFromUrl(resultPref.getRejectedUrlIcon());
            }
            if (!TextUtils.isEmpty(resultPref.getPendingUrlIcon())) {
                getView().fetchImageFromUrl(resultPref.getPendingUrlIcon());
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
        final CheckoutStore store = CheckoutStore.getInstance();
        return !store.hasEnabledPaymentMethodPlugin() && !store.hasPaymentProcessor();
    }

    private void resolvePreSelectedData() {
        if (state.paymentDataInput != null) {
            state.selectedIssuer = state.paymentDataInput.getIssuer();
            state.createdToken = state.paymentDataInput.getToken();
            userSelectionRepository.select(state.paymentDataInput.getPaymentMethod());
            userSelectionRepository.select(state.paymentDataInput.getPayerCost());
            if (paymentConfiguration.getDiscount() == null) {
                paymentConfiguration.configure(state.paymentDataInput.getDiscount());
            }
        } else if (state.paymentResultInput != null && state.paymentResultInput.getPaymentData() != null) {
            userSelectionRepository.select(state.paymentResultInput.getPaymentData().getPaymentMethod());
            userSelectionRepository.select(state.paymentResultInput.getPaymentData().getPayerCost());
            state.selectedIssuer = state.paymentResultInput.getPaymentData().getIssuer();
            state.createdToken = state.paymentResultInput.getPaymentData().getToken();
            if (paymentConfiguration.getDiscount() == null) {
                paymentConfiguration.configure(state.paymentResultInput.getPaymentData().getDiscount());
            }
        }
    }

    private void getDiscountCampaigns() {
        getResourcesProvider().getDiscountCampaigns(onCampaignsRetrieved());
    }

    private TaggedCallback<List<Campaign>> onCampaignsRetrieved() {
        return new TaggedCallback<List<Campaign>>(ApiUtil.RequestOrigin.GET_CAMPAIGNS) {
            @Override
            public void onSuccess(final List<Campaign> campaigns) {
                if (isViewAttached()) {
                    analyzeCampaigns(campaigns);
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                paymentConfiguration.configure((Campaign) null);
                paymentConfiguration.configure((Discount) null);
                if (isViewAttached()) {
                    retrievePaymentMethodSearch();
                }
            }
        };
    }

    @VisibleForTesting
    void analyzeCampaigns(final Collection<Campaign> campaigns) {
        if (campaigns == null || campaigns.size() == 0) {
            retrievePaymentMethodSearch();
        } else {
            for (final Campaign campaign : campaigns) {
                if (campaign.isDirectDiscountCampaign()) {
                    paymentConfiguration.configure(campaign);
                    getDirectDiscount();
                }
            }

            //TODO set couponDiscount;
        }
    }

    private void getDirectDiscount() {
        final CheckoutPreference checkoutPreference = getCheckoutPreference();
        String payerEmail =
            checkoutPreference.getPayer() == null ? ""
                : checkoutPreference.getPayer().getEmail();
        getResourcesProvider().getDirectDiscount(checkoutPreference.getTotalAmount(), payerEmail,
            new TaggedCallback<Discount>(ApiUtil.RequestOrigin.GET_DIRECT_DISCOUNT) {
                @Override
                public void onSuccess(final Discount discount) {
                    if (isViewAttached()) {
                        paymentConfiguration.configure(discount);
                        retrievePaymentMethodSearch();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    paymentConfiguration.configure((Discount) null);
                    if (isViewAttached()) {
                        retrievePaymentMethodSearch();
                    }
                }
            });
    }

    private void retrievePaymentMethodSearch() {
        final CheckoutPreference checkoutPreference = getCheckoutPreference();

        final Payer payer = new Payer();
        payer.setAccessToken(checkoutPreference.getPayer().getAccessToken());

        getResourcesProvider().getPaymentMethodSearch(
            amountRepository.getAmountToPay(),
            checkoutPreference.getExcludedPaymentTypes(),
            checkoutPreference.getExcludedPaymentMethods(),
            getResourcesProvider().getCardsWithEsc(),
            CheckoutStore.getInstance().getEnabledPaymentMethodPluginsIds(),
            payer,
            checkoutPreference.getSite(),
            onPaymentMethodSearchRetrieved(),
            onCustomerRetrieved()
        );

    }

    private TaggedCallback<PaymentMethodSearch> onPaymentMethodSearchRetrieved() {
        return new TaggedCallback<PaymentMethodSearch>(ApiUtil.RequestOrigin.PAYMENT_METHOD_SEARCH) {
            @Override
            public void onSuccess(final PaymentMethodSearch paymentMethodSearch) {
                if (isViewAttached()) {
                    state.paymentMethodSearch = paymentMethodSearch;
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

    private boolean hasToSkipPaymentResultScreen(final PaymentResult paymentResult) {
        final String status = paymentResult == null ? "" : paymentResult.getPaymentStatus();
        return shouldSkipResult(status);
    }

    private boolean shouldSkipResult(final String paymentStatus) {
        final FlowPreference flowPref = state.config.getFlowPreference();

        return !flowPref.isPaymentResultScreenEnabled()
            || (
            flowPref.getCongratsDisplayTime() != null &&
                flowPref.getCongratsDisplayTime() == 0 &&
                Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus))
            || Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus) &&
            !flowPref.isPaymentApprovedScreenEnabled()
            || Payment.StatusCodes.STATUS_REJECTED.equals(paymentStatus) &&
            !flowPref.isPaymentRejectedScreenEnabled()
            || Payment.StatusCodes.STATUS_PENDING.equals(paymentStatus) &&
            !flowPref.isPaymentPendingScreenEnabled();
    }

    private boolean isReviewAndConfirmEnabled() {
        return state.config.getFlowPreference().isReviewAndConfirmScreenEnabled();
    }

    public boolean isInstallmentsReviewScreenEnabled() {
        return state.config.getFlowPreference().isInstallmentsReviewScreenEnabled();
    }

    public boolean isESCEnabled() {
        return state.config.getFlowPreference().isESCEnabled();
    }

    public Card getSelectedCard() {
        return state.selectedCard;
    }

    private void retrieveCheckoutPreference(final String checkoutPreferenceId) {
        getResourcesProvider().getCheckoutPreference(checkoutPreferenceId,
            new TaggedCallback<CheckoutPreference>(ApiUtil.RequestOrigin.GET_PREFERENCE) {

                @Override
                public void onSuccess(final CheckoutPreference checkoutPreference) {
                    //TODO 21/06/2017 - Hack for credits, should remove payer access token.
                    checkoutPreference.getPayer().setAccessToken(state.privateKey);
                    paymentConfiguration.configure(checkoutPreference);
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

    public void onErrorCancel(@Nullable final MercadoPagoError mercadoPagoError) {
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

    public void onPaymentMethodSelectionResponse(final Issuer issuer,
        final Token token,
        final Discount discount,
        final Card card,
        final Payer payer) {
        state.selectedIssuer = issuer;
        state.createdToken = token;
        paymentConfiguration.configure(discount);
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
        getView().showProgress();
        final PaymentData paymentData = createPaymentData();

        if (hasCustomPaymentProcessor()) {
            CheckoutStore.getInstance().setPaymentData(paymentData);
            getView().showPaymentProcessor();
        } else {
            final String transactionId = getTransactionID();
            getResourcesProvider().createPayment(transactionId,
                getCheckoutPreference(),
                paymentData,
                state.config.isBinaryMode(),
                state.customerId,
                new TaggedCallback<Payment>(ApiUtil.RequestOrigin.CREATE_PAYMENT) {
                    @Override
                    public void onSuccess(final Payment payment) {
                        if (isViewAttached()) {
                            getView().hideProgress();
                            state.createdPayment = payment;
                            PaymentResult paymentResult = createPaymentResult(payment, paymentData);
                            checkStartPaymentResultActivity(paymentResult);
                            cleanTransactionId();
                        }
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {
                        if (isViewAttached()) {
                            getView().hideProgress();
                            resolvePaymentError(error, paymentData);
                        }
                    }
                });
        }
    }

    @VisibleForTesting
    void resolvePaymentError(final MercadoPagoError error, final PaymentData paymentData) {
        final boolean invalidEsc = getResourcesProvider().manageEscForError(error, paymentData);
        if (invalidEsc) {
            continuePaymentWithoutESC();
        } else {
            recoverCreatePayment(error);
        }
    }

    private boolean hasCustomPaymentProcessor() {
        return CheckoutStore.getInstance()
            .doesPaymentProcessorSupportPaymentMethodSelected(userSelectionRepository.getPaymentMethod().getId()) !=
            null;
    }

    private void continuePaymentWithoutESC() {
        state.paymentRecovery = new PaymentRecovery(state.createdToken, userSelectionRepository.getPaymentMethod(),
            userSelectionRepository.getPayerCost(), state.selectedIssuer, Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);
        getView().startPaymentRecoveryFlow(state.paymentRecovery);
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
        if (state.config.getFlowPreference().shouldExitOnPaymentMethodChange() && !isUniquePaymentMethod()) {
            getView().exitCheckout(MercadoPagoCheckout.PAYMENT_METHOD_CHANGED_REQUESTED);
        } else if (isUniquePaymentMethod()) {
            getView().cancelCheckout();
        } else {
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
            //Back button in R&C
            getView().transitionOut();
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

    public void onCardFlowResponse(final Issuer issuer, final Token token) {
        state.selectedIssuer = issuer;
        state.createdToken = token;

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
            .setPayerEmail(getCheckoutPreference().getPayer().getEmail())
            .setStatementDescription(payment.getStatementDescriptor())
            .build();
    }

    private PaymentData createPaymentData() {

        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(userSelectionRepository.getPaymentMethod());
        paymentData.setPayerCost(userSelectionRepository.getPayerCost());
        paymentData.setIssuer(state.selectedIssuer);
        paymentData.setDiscount(paymentConfiguration.getDiscount());
        paymentData.setToken(state.createdToken);
        paymentData.setTransactionAmount(amountRepository.getAmountToPay());
        final Payer payer = createPayerFrom(getCheckoutPreference().getPayer(), state.collectedPayer);
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
                new PaymentRecovery(state.createdToken, userSelectionRepository.getPaymentMethod(),
                    userSelectionRepository.getPayerCost(),
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
        return state.config.getMerchantPublicKey() + Calendar.getInstance().getTimeInMillis();
    }

    private boolean existsTransactionId() {
        return state.currentPaymentIdempotencyKey != null;
    }

    private void cleanTransactionId() {
        state.currentPaymentIdempotencyKey = null;
    }

    public PaymentMethod getSelectedPaymentMethod() {
        return userSelectionRepository.getPaymentMethod();
    }

    public Issuer getIssuer() {
        return state.selectedIssuer;
    }

    public Token getCreatedToken() {
        return state.createdToken;
    }

    public Payment getCreatedPayment() {
        return state.createdPayment;
    }

    public PaymentResultScreenPreference getPaymentResultScreenPreference() {
        return state.config.getPaymentResultScreenPreference();
    }

    public Integer getCongratsDisplay() {
        return state.config.getFlowPreference().getCongratsDisplayTime();
    }

    public CheckoutPreference getCheckoutPreference() {
        return paymentConfiguration.getCheckoutPreference();
    }

    public PaymentMethodSearch getPaymentMethodSearch() {
        return state.paymentMethodSearch;
    }

    public void onPaymentMethodSelected(final PaymentMethodSearch paymentMethodSearch) {
        this.state.paymentMethodSearch = paymentMethodSearch;
    }

    public Discount getDiscount() {
        return paymentConfiguration.getDiscount();
    }

    public Campaign getCampaign() {
        return paymentConfiguration.getCampaign();
    }

    public Boolean getShowBankDeals() {
        return state.config.getFlowPreference().isBankDealsEnabled();
    }

    public boolean shouldShowAllSavedCards() {
        return state.config.getFlowPreference().isShowAllSavedCardsEnabled();
    }

    public Integer getMaxSavedCardsToShow() {
        return state.config.getFlowPreference().getMaxSavedCardsToShow();
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

    public void checkStartPaymentResultActivity(final PaymentResult paymentResult) {
        final PaymentData paymentData = paymentResult.getPaymentData();
        final String paymentStatus = paymentResult.getPaymentStatus();
        final String paymentStatusDetail = paymentResult.getPaymentStatusDetail();
        if (getResourcesProvider().manageEscForPayment(paymentData, paymentStatus, paymentStatusDetail)) {
            continuePaymentWithoutESC();
        } else {
            if (hasToSkipPaymentResultScreen(paymentResult)) {
                finishCheckout();
            } else {
                getView().showPaymentResult(paymentResult);
            }
        }
    }

    public void onBusinessResult(final BusinessPayment businessPayment) {
        //TODO look for a better option than singleton, it make it not testeable.
        final PaymentData paymentData = CheckoutStore.getInstance().getPaymentData();

        getResourcesProvider().manageEscForPayment(paymentData,
            businessPayment.getPaymentStatus(),
            businessPayment.getPaymentStatusDetail());

        final String lastFourDigits =
            paymentData.getToken() != null ? paymentData.getToken().getLastFourDigits() : null;

        final BusinessPaymentModel model =
            new BusinessPaymentModel(businessPayment, paymentConfiguration.getDiscount(), paymentData.getPaymentMethod(),
                paymentData.getPayerCost(),
                getCheckoutPreference().getSite().getCurrencyId(),
                amountRepository.getAmountToPay(), lastFourDigits);
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
        if (state.config.getFlowPreference().shouldExitOnPaymentMethodChange()) {
            getView().exitCheckout(MercadoPagoCheckout.PAYMENT_METHOD_CHANGED_REQUESTED);
        } else {
            //TODO remove when navigation is corrected and works with stack.
            state.editPaymentMethodFromReviewAndConfirm = fromReviewAndConfirm;
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
            if (fromReviewAndConfirm) {
                //Button "change payment method" in R&C
                getView().transitionOut();
            }
        }
    }

    public void startCardPayment(@NonNull final CardPaymentModel cardPaymentModel) {
        state.createdToken = cardPaymentModel.token;
        state.selectedCard = cardPaymentModel.card;
        state.selectedIssuer = cardPaymentModel.issuer;
        userSelectionRepository.select(cardPaymentModel.card.getPaymentMethod());
        userSelectionRepository.select(cardPaymentModel.payerCost);
        getView().showProgress();
        createPayment();
    }

    public void startPayment(@NonNull final PaymentMethod paymentMethod) {
        // TODO refactor // see hooks options
        userSelectionRepository.select(paymentMethod);
        getView().showProgress();
        createPayment();
    }

    public void confirmCardFlow() {
        getView().showProgress();
    }

    public void cancelCardFlow() {
        getView().hideProgress();
    }
}