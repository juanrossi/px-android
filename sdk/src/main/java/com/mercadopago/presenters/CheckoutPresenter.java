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
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import com.mercadopago.providers.CheckoutProvider;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.TextUtil;
import com.mercadopago.util.TextUtils;
import com.mercadopago.views.CheckoutView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CheckoutPresenter extends MvpPresenter<CheckoutView, CheckoutProvider> {

    private static final String INTERNAL_SERVER_ERROR_FIRST_DIGIT = "5";

    private PersistentDataModel persistentData;

    private transient FailureRecovery failureRecovery;

    private DataInitializationTask dataInitializationTask; //instance saved as attribute to cancel and avoid crash

    public final static class PersistentDataModel implements Serializable {

        /**
         * If preference id is set then the checkout did not start
         * with a custom CheckoutPreference created by hand.
         */
        private String checkoutPreferenceId;

        /**
         * If preference is set then the checkout have just started
         * with a custom CheckoutPreference created by hand.
         */
        private CheckoutPreference checkoutPreference;

        private FlowPreference flowPreference;
        private ServicePreference servicePreference;
        private PaymentResultScreenPreference paymentResultScreenPreference;
        private Discount discount;
        private Campaign campaign;
        private PaymentData paymentDataInput;
        private PaymentResult paymentResultInput;
        private boolean binaryMode;
        private int requestedResult;
        private PaymentMethodSearch paymentMethodSearch;
        private Issuer selectedIssuer;
        private PayerCost selectedPayerCost;
        private Token createdToken;
        private Card selectedCard;
        private PaymentMethod selectedPaymentMethod;
        private Payment createdPayment;
        private Payer collectedPayer;
        private boolean paymentMethodEdited;
        private boolean paymentMethodEditionRequested;
        private PaymentRecovery paymentRecovery;
        private String customerId;
        private String idempotencyKeySeed;
        private String currentPaymentIdempotencyKey;

        private PersistentDataModel() {
        }

        public static PersistentDataModel createWith(final int requestedResult,
            MercadoPagoCheckout mercadoPagoCheckout) {
            final PersistentDataModel persistentDataModel = new PersistentDataModel();
            persistentDataModel.checkoutPreferenceId = mercadoPagoCheckout.getPreferenceId();
            persistentDataModel.checkoutPreference = mercadoPagoCheckout.getCheckoutPreference();
            persistentDataModel.discount = mercadoPagoCheckout.getDiscount();
            persistentDataModel.campaign = mercadoPagoCheckout.getCampaign();
            persistentDataModel.servicePreference = mercadoPagoCheckout.getServicePreference();
            persistentDataModel.flowPreference = mercadoPagoCheckout.getFlowPreference();
            persistentDataModel.paymentResultInput = mercadoPagoCheckout.getPaymentResult();
            persistentDataModel.paymentDataInput = mercadoPagoCheckout.getPaymentData();
            persistentDataModel.binaryMode = mercadoPagoCheckout.isBinaryMode();
            persistentDataModel.paymentResultScreenPreference = mercadoPagoCheckout.getPaymentResultScreenPreference();
            persistentDataModel.requestedResult = requestedResult;
            persistentDataModel.idempotencyKeySeed = mercadoPagoCheckout.getMerchantPublicKey();
            return persistentDataModel;
        }
    }

    public CheckoutPresenter(final PersistentDataModel persistentData) {
        this.persistentData = persistentData;
    }

    public Serializable getPersistentData() {
        return persistentData;
    }

    public void initialize() {
        getView().showProgress();
        configurePreference();
    }

    private void configurePreference() {
        if (TextUtil.isEmpty(persistentData.checkoutPreferenceId)) { // custom checkout preference
            startCheckoutForPreference();
        } else {
            retrieveCheckoutPreference(persistentData.checkoutPreferenceId);
        }
    }

    private void startCheckoutForPreference() {
        try {
            persistentData.checkoutPreference.validate();
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
        return persistentData.paymentDataInput == null && persistentData.paymentResultInput == null;
    }

    private void startCheckout() {
        getResourcesProvider().fetchFonts();
        fetchImages();
        resolvePreSelectedData();
        initializePluginsData();
    }

    private void fetchImages() {
        if (persistentData.paymentResultScreenPreference != null && getView() != null) {
            if (!TextUtils.isEmpty(persistentData.paymentResultScreenPreference.getApprovedUrlIcon())) {
                getView().fetchImageFromUrl(persistentData.paymentResultScreenPreference.getApprovedUrlIcon());
            }
            if (!TextUtils.isEmpty(persistentData.paymentResultScreenPreference.getRejectedUrlIcon())) {
                getView().fetchImageFromUrl(persistentData.paymentResultScreenPreference.getRejectedUrlIcon());
            }
            if (!TextUtils.isEmpty(persistentData.paymentResultScreenPreference.getPendingUrlIcon())) {
                getView().fetchImageFromUrl(persistentData.paymentResultScreenPreference.getPendingUrlIcon());
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
                resolveDiscount();
                retrievePaymentMethodSearch();
            }
        }
    }

    private void resolveDiscount() {
        if (isDiscountEnabled() && persistentData.discount == null) {
            persistentData.flowPreference.disableDiscount();
        }
    }

    @VisibleForTesting
    boolean shouldRetrieveDiscount() {
        CheckoutStore store = CheckoutStore.getInstance();
        return isDiscountEnabled() && persistentData.discount == null && !store.hasEnabledPaymentMethodPlugin() &&
            !store.hasPaymentProcessor();
    }

    public boolean isDiscountEnabled() {
        return persistentData.flowPreference.isDiscountEnabled();
    }

    private void resolvePreSelectedData() {
        if (persistentData.paymentDataInput != null) {
            persistentData.selectedIssuer = persistentData.paymentDataInput.getIssuer();
            persistentData.selectedPayerCost = persistentData.paymentDataInput.getPayerCost();
            persistentData.createdToken = persistentData.paymentDataInput.getToken();
            persistentData.selectedPaymentMethod = persistentData.paymentDataInput.getPaymentMethod();
            if (persistentData.discount == null) {
                persistentData.discount = persistentData.paymentDataInput.getDiscount();
            }
        } else if (persistentData.paymentResultInput != null &&
            persistentData.paymentResultInput.getPaymentData() != null) {
            persistentData.selectedPaymentMethod =
                persistentData.paymentResultInput.getPaymentData().getPaymentMethod();
            persistentData.selectedPayerCost = persistentData.paymentResultInput.getPaymentData().getPayerCost();
            persistentData.selectedIssuer = persistentData.paymentResultInput.getPaymentData().getIssuer();
            persistentData.createdToken = persistentData.paymentResultInput.getPaymentData().getToken();
            if (persistentData.discount == null) {
                persistentData.discount = persistentData.paymentResultInput.getPaymentData().getDiscount();
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
                if (isViewAttached()) {
                    persistentData.flowPreference.disableDiscount();
                    retrievePaymentMethodSearch();
                }
            }
        };
    }

    private void analyzeCampaigns(final List<Campaign> campaigns) {
        boolean directDiscountFound = false;
        boolean couponDiscountFound = false;

        if (campaigns == null) {
            persistentData.flowPreference.disableDiscount();
            retrievePaymentMethodSearch();
        } else {
            for (Campaign campaign : campaigns) {
                if (campaign.isDirectDiscountCampaign()) {
                    directDiscountFound = true;
                } else if (campaign.isSingleCodeDiscountCampaign()) {
                    couponDiscountFound = true;
                }
            }

            if (directDiscountFound) {
                getDirectDiscount(couponDiscountFound);
            } else {
                if (!couponDiscountFound) {
                    persistentData.flowPreference.disableDiscount();
                }
                retrievePaymentMethodSearch();
            }
        }
    }

    private void getDirectDiscount(final boolean couponDiscountFound) {
        String payerEmail = persistentData.checkoutPreference.getPayer() == null ? ""
            : persistentData.checkoutPreference.getPayer().getEmail();
        getResourcesProvider().getDirectDiscount(persistentData.checkoutPreference.getTotalAmount(), payerEmail,
            new TaggedCallback<Discount>(ApiUtil.RequestOrigin.GET_DIRECT_DISCOUNT) {
                @Override
                public void onSuccess(final Discount discount) {
                    if (isViewAttached()) {
                        CheckoutPresenter.this.persistentData.discount = discount;
                        retrievePaymentMethodSearch();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        if (couponDiscountFound) {
                            retrievePaymentMethodSearch();
                        } else {
                            persistentData.flowPreference.disableDiscount();
                            retrievePaymentMethodSearch();
                        }
                    }
                }
            });
    }

    @VisibleForTesting
    void retrievePaymentMethodSearch() {
        Payer payer = new Payer();
        payer.setAccessToken(persistentData.checkoutPreference.getPayer().getAccessToken());
        getResourcesProvider().getPaymentMethodSearch(
            getTransactionAmount(),
            persistentData.checkoutPreference.getExcludedPaymentTypes(),
            persistentData.checkoutPreference.getExcludedPaymentMethods(),
            payer,
            persistentData.checkoutPreference.getSite(),
            onPaymentMethodSearchRetrieved(),
            onCustomerRetrieved()
        );
    }

    public BigDecimal getTransactionAmount() {
        BigDecimal amount;

        if (persistentData.discount != null && persistentData.flowPreference.isDiscountEnabled()) {
            amount = persistentData.discount.getAmountWithDiscount(persistentData.checkoutPreference.getTotalAmount());
        } else {
            amount = persistentData.checkoutPreference.getTotalAmount();
        }

        return amount;
    }

    private TaggedCallback<PaymentMethodSearch> onPaymentMethodSearchRetrieved() {
        return new TaggedCallback<PaymentMethodSearch>(ApiUtil.RequestOrigin.PAYMENT_METHOD_SEARCH) {
            @Override
            public void onSuccess(final PaymentMethodSearch paymentMethodSearch) {
                if (isViewAttached()) {
                    CheckoutPresenter.this.persistentData.paymentMethodSearch = paymentMethodSearch;
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
                    persistentData.customerId = customer.getId();
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                //Do nothing
            }
        };
    }

    @VisibleForTesting
    void startFlow() {
        if (persistentData.paymentDataInput != null) {
            showReviewAndConfirm();
        } else if (persistentData.paymentResultInput != null &&
            persistentData.paymentResultInput.getPaymentData() != null) {
            checkStartPaymentResultActivity(persistentData.paymentResultInput);
        } else {
            getView().showPaymentMethodSelection();
        }
    }

    private void showReviewAndConfirm() {
        getView().showReviewAndConfirm();
        persistentData.paymentMethodEditionRequested = false;
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
        return !persistentData.flowPreference.isPaymentResultScreenEnabled()
            || (persistentData.flowPreference.getCongratsDisplayTime() != null &&
            persistentData.flowPreference.getCongratsDisplayTime() == 0 &&
            Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus))
            || Payment.StatusCodes.STATUS_APPROVED.equals(paymentStatus) &&
            !persistentData.flowPreference.isPaymentApprovedScreenEnabled()
            || Payment.StatusCodes.STATUS_REJECTED.equals(paymentStatus) &&
            !persistentData.flowPreference.isPaymentRejectedScreenEnabled()
            || Payment.StatusCodes.STATUS_PENDING.equals(paymentStatus) &&
            !persistentData.flowPreference.isPaymentPendingScreenEnabled();
    }

    private boolean isReviewAndConfirmEnabled() {
        return persistentData.flowPreference.isReviewAndConfirmScreenEnabled();
    }

    public boolean isInstallmentsReviewScreenEnabled() {
        return persistentData.flowPreference.isInstallmentsReviewScreenEnabled();
    }

    public boolean isESCEnabled() {
        return persistentData.flowPreference.isESCEnabled();
    }

    public Card getSelectedCard() {
        return persistentData.selectedCard;
    }

    public ServicePreference getServicePreference() {
        return persistentData.servicePreference;
    }

    private void retrieveCheckoutPreference(final String checkoutPreferenceId) {
        getResourcesProvider().getCheckoutPreference(checkoutPreferenceId,
            new TaggedCallback<CheckoutPreference>(ApiUtil.RequestOrigin.GET_PREFERENCE) {

                @Override
                public void onSuccess(final CheckoutPreference checkoutPreference) {
                    CheckoutPresenter.this.persistentData.checkoutPreference = checkoutPreference;
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
            getView().backToPaymentMethodSelection();
        } else if (noUserInteractionReached() || !isReviewAndConfirmEnabled()) {
            getView().cancelCheckout(mercadoPagoError);
        } else {
            showReviewAndConfirm();
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

    private boolean noUserInteractionReached() {
        return persistentData.selectedPaymentMethod == null;
    }

    public void onPaymentMethodSelectionResponse(final PaymentMethod paymentMethod,
        final Issuer issuer,
        final PayerCost payerCost,
        final Token token,
        final Discount discount,
        final Card card,
        final Payer payer) {
        persistentData.selectedPaymentMethod = paymentMethod;
        persistentData.selectedIssuer = issuer;
        persistentData.selectedPayerCost = payerCost;
        persistentData.createdToken = token;
        this.persistentData.discount = discount;
        persistentData.selectedCard = card;
        persistentData.collectedPayer = payer;
        onPaymentMethodSelected();
    }

    private void onPaymentMethodSelected() {
        if (!showHook2(createPaymentData())) {
            hook2Continue();
        }
    }

    public void resolvePaymentDataResponse() {
        if (MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE == persistentData.requestedResult) {
            PaymentData paymentData = createPaymentData();
            getView().finishWithPaymentDataResult(paymentData, persistentData.paymentMethodEdited);
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
                persistentData.checkoutPreference,
                paymentData,
                persistentData.binaryMode,
                persistentData.customerId,
                new TaggedCallback<Payment>(ApiUtil.RequestOrigin.CREATE_PAYMENT) {
                    @Override
                    public void onSuccess(final Payment payment) {
                        persistentData.createdPayment = payment;
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
        persistentData.paymentRecovery =
            new PaymentRecovery(persistentData.createdToken, persistentData.selectedPaymentMethod,
                persistentData.selectedPayerCost, persistentData.selectedIssuer, Payment.StatusCodes.STATUS_REJECTED,
                Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);

        getView().startPaymentRecoveryFlow(persistentData.paymentRecovery);
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

    private void finishCheckout() {
        if (persistentData.createdPayment == null) {
            getView().finishWithPaymentResult();
        } else {
            getView().finishWithPaymentResult(persistentData.createdPayment);
        }
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
        persistentData.createdPayment = new Payment();
        persistentData.createdPayment.setStatus(Payment.StatusCodes.STATUS_IN_PROCESS);
        persistentData.createdPayment.setStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY);
        PaymentResult paymentResult = createPaymentResult(persistentData.createdPayment, createPaymentData());
        getView().showPaymentResult(paymentResult);
        cleanTransactionId();
    }

    private void resolveBadRequestError(final MercadoPagoError mercadoPagoError) {
        getView().showError(mercadoPagoError);
    }

    public void onPaymentMethodSelectionError(final MercadoPagoError mercadoPagoError) {
        if (!persistentData.paymentMethodEditionRequested) {
            getView().cancelCheckout(mercadoPagoError);
        } else {
            persistentData.paymentMethodEditionRequested = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onPaymentMethodSelectionCancel() {
        if (!persistentData.paymentMethodEditionRequested) {
            getView().cancelCheckout();
        } else {
            persistentData.paymentMethodEditionRequested = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onPaymentConfirmation() {
        if (!showHook3(createPaymentData())) {
            resolvePaymentDataResponse();
        }
    }

    public void changePaymentMethod() {
        if (persistentData.flowPreference.shouldExitOnPaymentMethodChange()) {
            getView().finishFromReviewAndConfirm();
        } else {
            persistentData.paymentMethodEdited = true;
            persistentData.paymentMethodEditionRequested = true;
            getView().startPaymentMethodEdition();
        }
    }

    public void onReviewAndConfirmCancel() {
        if (persistentData.flowPreference.shouldExitOnPaymentMethodChange() && !isUniquePaymentMethod()) {
            getView().finishFromReviewAndConfirm();
        } else if (isUniquePaymentMethod()) {
            getView().cancelCheckout();
        } else {
            persistentData.paymentMethodEdited = true;
            getView().backToPaymentMethodSelection();
        }
    }

    public void onReviewAndConfirmCancelPayment() {
        getView().cancelCheckout();
    }

    public void onReviewAndConfirmError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onPaymentResultCancel(final String nextAction) {
        if (!TextUtils.isEmpty(nextAction)) {
            if (nextAction.equals(PaymentResult.SELECT_OTHER_PAYMENT_METHOD)) {
                persistentData.paymentMethodEdited = true;
                getView().backToPaymentMethodSelection();
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

        persistentData.selectedPaymentMethod = paymentMethod;
        persistentData.selectedIssuer = issuer;
        persistentData.selectedPayerCost = payerCost;
        persistentData.createdToken = token;
        persistentData.discount = discount;

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
        persistentData.paymentMethodEdited = true;
        getView().backToPaymentMethodSelection();
    }

    public void onCustomReviewAndConfirmResponse(final Integer customResultCode) {
        getView().cancelCheckout(customResultCode, persistentData.paymentMethodEdited);
    }

    public void onCustomPaymentResultResponse(final Integer customResultCode) {
        if (persistentData.createdPayment == null) {
            getView().finishWithPaymentResult(customResultCode);
        } else {
            getView().finishWithPaymentResult(customResultCode, persistentData.createdPayment);
        }
    }

    public boolean isUniquePaymentMethod() {
        final CheckoutStore store = CheckoutStore.getInstance();
        int pluginCount = store.getPaymentMethodPluginCount();
        int groupCount = 0;
        int customCount = 0;

        if (persistentData.paymentMethodSearch != null && persistentData.paymentMethodSearch.hasSearchItems()) {
            groupCount = persistentData.paymentMethodSearch.getGroups().size();
            if (pluginCount == 0 && groupCount == 1 &&
                persistentData.paymentMethodSearch.getGroups().get(0).isGroup()) {
                return false;
            }
        }

        if (persistentData.paymentMethodSearch != null && persistentData.paymentMethodSearch.hasCustomSearchItems()) {
            customCount = persistentData.paymentMethodSearch.getCustomSearchItems().size();
        }

        return groupCount + customCount + pluginCount == 1;
    }

    private PaymentResult createPaymentResult(final Payment payment, final PaymentData paymentData) {
        return new PaymentResult.Builder()
            .setPaymentData(paymentData)
            .setPaymentId(payment.getId())
            .setPaymentStatus(payment.getStatus())
            .setPaymentStatusDetail(payment.getStatusDetail())
            .setPayerEmail(persistentData.checkoutPreference.getPayer().getEmail())
            .setStatementDescription(payment.getStatementDescriptor())
            .build();
    }

    @VisibleForTesting
    PaymentData createPaymentData() {

        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(persistentData.selectedPaymentMethod);
        paymentData.setPayerCost(persistentData.selectedPayerCost);
        paymentData.setIssuer(persistentData.selectedIssuer);
        paymentData.setToken(persistentData.createdToken);
        paymentData.setTransactionAmount(persistentData.checkoutPreference.getTotalAmount());

        Payer payer = createPayerFrom(persistentData.checkoutPreference.getPayer(), persistentData.collectedPayer);
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
                persistentData.paymentResultInput == null ? CheckoutStore.getInstance().getPaymentResult()
                    : persistentData.paymentResultInput;
            String paymentStatus = persistentData.createdPayment == null ? paymentResult.getPaymentStatus()
                : persistentData.createdPayment.getStatus();
            String paymentStatusDetail = persistentData.createdPayment == null ? paymentResult.getPaymentStatusDetail()
                : persistentData.createdPayment.getStatusDetail();
            persistentData.paymentRecovery =
                new PaymentRecovery(persistentData.createdToken, persistentData.selectedPaymentMethod,
                    persistentData.selectedPayerCost, persistentData.selectedIssuer, paymentStatus,
                    paymentStatusDetail);
            getView().startPaymentRecoveryFlow(persistentData.paymentRecovery);
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
        return persistentData.paymentRecovery != null && persistentData.paymentRecovery.isTokenRecoverable();
    }

    @VisibleForTesting
    String getTransactionID() {
        if (!existsTransactionId() || persistentData.paymentMethodEdited) {
            persistentData.currentPaymentIdempotencyKey = createNewTransactionId();
        }
        return persistentData.currentPaymentIdempotencyKey;
    }

    private String createNewTransactionId() {
        return persistentData.idempotencyKeySeed + Calendar.getInstance().getTimeInMillis();
    }

    private boolean existsTransactionId() {
        return persistentData.currentPaymentIdempotencyKey != null;
    }

    private void cleanTransactionId() {
        persistentData.currentPaymentIdempotencyKey = null;
    }

    public PaymentMethod getSelectedPaymentMethod() {
        return persistentData.selectedPaymentMethod;
    }

    public Issuer getIssuer() {
        return persistentData.selectedIssuer;
    }

    public PayerCost getSelectedPayerCost() {
        return persistentData.selectedPayerCost;
    }

    public Token getCreatedToken() {
        return persistentData.createdToken;
    }

    public Payment getCreatedPayment() {
        return persistentData.createdPayment;
    }

    public PaymentResultScreenPreference getPaymentResultScreenPreference() {
        return persistentData.paymentResultScreenPreference;
    }

    public Integer getCongratsDisplay() {
        return persistentData.flowPreference.getCongratsDisplayTime();
    }

    public CheckoutPreference getCheckoutPreference() {
        return persistentData.checkoutPreference;
    }

    public PaymentMethodSearch getPaymentMethodSearch() {
        return persistentData.paymentMethodSearch;
    }

    public void onPaymentMethodSelected(final PaymentMethodSearch paymentMethodSearch) {
        this.persistentData.paymentMethodSearch = paymentMethodSearch;
    }

    public Discount getDiscount() {
        return persistentData.discount;
    }

    public Campaign getCampaign() {
        return persistentData.campaign;
    }

    public Boolean getShowBankDeals() {
        return persistentData.flowPreference.isBankDealsEnabled() &&
            persistentData.servicePreference.shouldShowBankDeals();
    }

    public boolean shouldShowAllSavedCards() {
        return persistentData.flowPreference.isShowAllSavedCardsEnabled();
    }

    public Integer getMaxSavedCardsToShow() {
        return persistentData.flowPreference.getMaxSavedCardsToShow();
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
        persistentData.paymentMethodEditionRequested = false;
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
            new BusinessPaymentModel(businessPayment, persistentData.discount, paymentData.getPaymentMethod(),
                paymentData.getPayerCost(),
                persistentData.checkoutPreference.getSite().getCurrencyId(),
                paymentData.getTransactionAmount(), lastFourDigits);
        getView().showBusinessResult(model);
    }
}