package com.mercadopago.presenters;

import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.controllers.PaymentMethodGuessingController;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.Cause;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.SavedESCCardToken;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.providers.CardVaultProvider;
import com.mercadopago.tracking.utils.TrackingUtil;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.TextUtil;
import com.mercadopago.views.CardVaultView;
import java.math.BigDecimal;
import java.util.List;

public class CardVaultPresenter extends MvpPresenter<CardVaultView, CardVaultProvider> {

    private FailureRecovery failureRecovery;
    private String bin;

    //Activity parameters
    private PaymentRecovery paymentRecovery;
    private PaymentPreference paymentPreference;
    private List<PaymentMethod> paymentMethodList;
    private Site site;
    private boolean installmentsEnabled;
    private boolean installmentsReviewEnabled;
    private boolean automaticSelection;
    private BigDecimal amount;
    private String merchantBaseUrl;
    private boolean installmentsListShown;
    private boolean issuersListShown;

    //Activity result
    protected PaymentMethod paymentMethod;
    private PayerCost payerCost;
    private Issuer issuer;

    //Card Info
    private CardInfo cardInfo;
    private Token token;
    private Card card;

    //Discount
    private Discount discount;
    private Campaign campaign;
    private String payerEmail;
    private List<PayerCost> payerCostsList;
    private List<Issuer> issuersList;

    //Security Code
    private String esc;
    private SavedESCCardToken escCardToken;

    public CardVaultPresenter() {
        super();
        installmentsEnabled = true;
        paymentPreference = new PaymentPreference();
    }

    public void initialize() {
        try {
            validateParameters();
            onValidStart();
        } catch (final IllegalStateException exception) {
            getView().showError(new MercadoPagoError(exception.getMessage(), false), "");
        }
    }

    private boolean viewAttached() {
        return getView() != null;
    }

    public void setPaymentRecovery(final PaymentRecovery paymentRecovery) {
        this.paymentRecovery = paymentRecovery;
    }

    public void setPaymentPreference(final PaymentPreference paymentPreference) {
        this.paymentPreference = paymentPreference;
    }

    public void setPaymentMethodList(final List<PaymentMethod> paymentMethodList) {
        this.paymentMethodList = paymentMethodList;
    }

    public void setSite(final Site site) {
        this.site = site;
    }

    public void setInstallmentsEnabled(final boolean installmentsEnabled) {
        this.installmentsEnabled = installmentsEnabled;
    }

    public void setCard(final Card card) {
        this.card = card;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(final Issuer mIssuer) {
        this.issuer = mIssuer;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token mToken) {
        this.token = mToken;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod mPaymentMethod) {
        this.paymentMethod = mPaymentMethod;
    }

    public PayerCost getPayerCost() {
        return payerCost;
    }

    public void setPayerCost(final PayerCost mPayerCost) {
        this.payerCost = mPayerCost;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentRecovery getPaymentRecovery() {
        return paymentRecovery;
    }

    public PaymentPreference getPaymentPreference() {
        return paymentPreference;
    }

    public List<PaymentMethod> getPaymentMethodList() {
        return paymentMethodList;
    }

    public Site getSite() {
        return site;
    }

    public Card getCard() {
        return card;
    }

    public String getESC() {
        return esc;
    }

    public void setESC(final String esc) {
        this.esc = esc;
    }

    public void setCardInfo(final CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        if (this.cardInfo == null) {
            bin = "";
        } else {
            bin = this.cardInfo.getFirstSixDigits();
        }
    }

    public void setPayerEmail(final String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setDiscount(final Discount discount) {
        this.discount = discount;
    }

    public void setCampaign(final Campaign campaign) {
        this.campaign = campaign;
    }

    public Discount getDiscount() {
        return discount;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setInstallmentsReviewEnabled(final boolean installmentReviewEnabled) {
        installmentsReviewEnabled = installmentReviewEnabled;
    }

    public Boolean getInstallmentsReviewEnabled() {
        return installmentsReviewEnabled;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public Integer getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(paymentMethod, bin);
    }

    public void setMerchantBaseUrl(final String merchantBaseUrl) {
        this.merchantBaseUrl = merchantBaseUrl;
    }

    public String getMerchantBaseUrl() {
        return merchantBaseUrl;
    }

    public void setAutomaticSelection(final boolean automaticSelection) {
        this.automaticSelection = automaticSelection;
    }

    public boolean getAutomaticSelection() {
        return automaticSelection;
    }

    public boolean isInstallmentsListShown() {
        return installmentsListShown;
    }

    public boolean isIssuersListShown() {
        return issuersListShown;
    }

    public void setInstallmentsListShown(final boolean installmentsListShown) {
        this.installmentsListShown = installmentsListShown;
    }

    public void setIssuersListShown(final boolean issuersListShown) {
        this.issuersListShown = issuersListShown;
    }

    private void checkStartInstallmentsActivity() {
        if (isInstallmentsEnabled() && payerCost == null) {
            installmentsListShown = true;
            askForInstallments();
        } else {
            finishWithResult();
        }
    }

    private void finishWithResult() {
        if (isSecurityCodeFlowNeeded()) {
            getView().animateTransitionSlideInSlideOut();
        } else {
            getView().transitionWithNoAnimation();
        }
        getView().finishWithResult();
    }

    private void askForInstallments() {
        if (issuersListShown) {
            getView().askForInstallmentsFromIssuers();
        } else if (!savedCardAvailable()) {
            getView().askForInstallmentsFromNewCard();
        } else {
            getView().askForInstallments();
        }
    }

    private void checkStartIssuersActivity() {
        if (issuer == null) {
            issuersListShown = true;
            getView().startIssuersActivity();
        } else {
            checkStartInstallmentsActivity();
        }
    }

    public boolean isInstallmentsEnabled() {
        return installmentsEnabled;
    }

    private void validateParameters() throws IllegalStateException {
        if (installmentsEnabled) {
            if (site == null) {
                throw new IllegalStateException(getResourcesProvider().getMissingSiteErrorMessage());
            } else if (amount == null) {
                throw new IllegalStateException(getResourcesProvider().getMissingAmountErrorMessage());
            }
        }
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    public List<PayerCost> getPayerCostList() {
        return payerCostsList;
    }

    private void getInstallmentsForCardAsync(final Card card) {
        String bin = TextUtil.isEmpty(cardInfo.getFirstSixDigits()) ? "" : cardInfo.getFirstSixDigits();
        Long issuerId = this.card.getIssuer() == null ? null : this.card.getIssuer().getId();
        String paymentMethodId = card.getPaymentMethod() == null ? "" : card.getPaymentMethod().getId();

        getResourcesProvider().getInstallmentsAsync(bin, issuerId, paymentMethodId, getTotalAmount(),
            new TaggedCallback<List<Installment>>(ApiUtil.RequestOrigin.GET_INSTALLMENTS) {
                @Override
                public void onSuccess(final List<Installment> installments) {
                    if (viewAttached()) {
                        resolveInstallmentsList(installments);
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (viewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_INSTALLMENTS);

                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                getInstallmentsForCardAsync(card);
                            }
                        });
                    }
                }
            });
    }

    private void resolveInstallmentsList(final List<Installment> installments) {
        String errorMessage = null;
        if (installments.size() == 0) {
            errorMessage = getResourcesProvider().getMissingInstallmentsForIssuerErrorMessage();
        } else if (installments.size() == 1) {
            resolvePayerCosts(installments.get(0).getPayerCosts());
        } else {
            errorMessage = getResourcesProvider().getMultipleInstallmentsForIssuerErrorMessage();
        }
        if (errorMessage != null && isViewAttached()) {
            getView().showError(new MercadoPagoError(errorMessage, false), "");
        }
    }

    private BigDecimal getTotalAmount() {
        BigDecimal amount;

        if (discount == null) {
            amount = this.amount;
        } else {
            amount = discount.getAmountWithDiscount(this.amount);
        }
        return amount;
    }

    private void resolvePayerCosts(final List<PayerCost> payerCosts) {
        PayerCost defaultPayerCost = paymentPreference.getDefaultInstallments(payerCosts);
        payerCostsList = payerCosts;

        if (defaultPayerCost != null) {
            payerCost = defaultPayerCost;
            askForSecurityCodeWithoutInstallments();
        } else if (payerCostsList.isEmpty()) {
            getView()
                .showError(new MercadoPagoError(getResourcesProvider().getMissingPayerCostsErrorMessage(), false), "");
        } else if (payerCostsList.size() == 1) {
            payerCost = payerCosts.get(0);
            askForSecurityCodeWithoutInstallments();
        } else {
            installmentsListShown = true;
            getView().askForInstallments();
        }
    }

    private void askForSecurityCodeWithoutInstallments() {
        if (isSecurityCodeFlowNeeded()) {
            getView().animateTransitionSlideInSlideOut();
        } else {
            getView().transitionWithNoAnimation();
        }
        startSecurityCodeFlowIfNeeded();
    }

    private void askForSecurityCodeFromInstallments() {
        getView().animateTransitionSlideInSlideOut();
        startSecurityCodeFlowIfNeeded();
    }

    public void resolveIssuersRequest(final Issuer issuer) {
        issuersListShown = true;
        setIssuer(issuer);
        checkStartInstallmentsActivity();
    }

    public void resolveInstallmentsRequest(final PayerCost payerCost, final Discount discount) {
        setSelectedInstallments(payerCost, discount);

        if (savedCardAvailable()) {
            if (installmentsListShown) {
                askForSecurityCodeFromInstallments();
            } else {
                askForSecurityCodeWithoutInstallments();
            }
        } else {
            finishWithResult();
        }
    }

    private void setSelectedInstallments(final PayerCost payerCost, final Discount discount) {
        installmentsListShown = true;
        setPayerCost(payerCost);
        setDiscount(discount);
    }

    public void resolveSecurityCodeRequest(final Token token) {
        setToken(token);
        if (tokenRecoveryAvailable()) {
            setPayerCost(getPaymentRecovery().getPayerCost());
            setIssuer(getPaymentRecovery().getIssuer());
        }
        finishWithResult();
    }

    public void resolveNewCardRequest(final PaymentMethod paymentMethod, final Token token,
        final PayerCost payerCost, final Issuer issuer,
        final List<PayerCost> payerCosts, final List<Issuer> issuers,
        final Discount discount) {

        setPaymentMethod(paymentMethod);
        setToken(token);
        setCardInfo(new CardInfo(token));
        setPayerCost(payerCost);
        setIssuer(issuer);
        setPayerCostsList(payerCosts);
        setIssuersList(issuers);

        if (discount != null) {
            setDiscount(discount);
        }

        checkStartIssuersActivity();
    }

    public void onResultCancel() {
        getView().cancelCardVault();
    }

    private void onValidStart() {
        installmentsListShown = false;
        issuersListShown = false;
        if (viewAttached()) {
            getView().showProgressLayout();
        }
        if (tokenRecoveryAvailable()) {
            startTokenRecoveryFlow();
        } else if (savedCardAvailable()) {
            startSavedCardFlow();
        } else {
            startNewCardFlow();
        }
    }

    private void startTokenRecoveryFlow() {
        setCardInfo(new CardInfo(getPaymentRecovery().getToken()));
        setPaymentMethod(getPaymentRecovery().getPaymentMethod());
        setToken(getPaymentRecovery().getToken());
        getView().askForSecurityCodeFromTokenRecovery();
    }

    private void startSavedCardFlow() {
        setCardInfo(new CardInfo(getCard()));
        setPaymentMethod(getCard().getPaymentMethod());
        setIssuer(getCard().getIssuer());
        if (isInstallmentsEnabled()) {
            getInstallmentsForCardAsync(getCard());
        } else {
            askForSecurityCodeWithoutInstallments();
        }
    }

    private void startNewCardFlow() {
        getView().askForCardInformation();
    }

    private boolean tokenRecoveryAvailable() {
        return getPaymentRecovery() != null && getPaymentRecovery().isTokenRecoverable();
    }

    private boolean savedCardAvailable() {
        return getCard() != null;
    }

    public void setPayerCostsList(final List<PayerCost> payerCostsList) {
        this.payerCostsList = payerCostsList;
    }

    private void setIssuersList(final List<Issuer> issuers) {
        issuersList = issuers;
    }

    public List<Issuer> getIssuersList() {
        return issuersList;
    }

    public void startSecurityCodeFlowIfNeeded() {
        if (isSecurityCodeFlowNeeded()) {
            getView().startSecurityCodeActivity(TrackingUtil.SECURITY_CODE_REASON_SAVED_CARD);
        } else {
            createESCToken();
        }
    }

    public boolean isSecurityCodeFlowNeeded() {
        return !savedCardAvailable() || !isESCSaved();
    }

    private boolean isESCSaved() {
        if (!isESCEmpty()) {
            return true;
        } else {
            setESC(getResourcesProvider().findESCSaved(card.getId()));
            return !isESCEmpty();
        }
    }

    private boolean isESCEmpty() {
        return esc == null || esc.isEmpty();
    }

    private void createESCToken() {
        if (savedCardAvailable() && !isESCEmpty()) {

            escCardToken = SavedESCCardToken.createWithEsc(card.getId(), esc);

            getResourcesProvider()
                .createESCTokenAsync(escCardToken, new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
                    @Override
                    public void onSuccess(final Token token) {
                        CardVaultPresenter.this.token = token;
                        CardVaultPresenter.this.token.setLastFourDigits(card.getLastFourDigits());
                        finishWithResult();
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {

                        if (error.isApiException() &&
                            error.getApiException().getStatus() == ApiUtil.StatusCodes.BAD_REQUEST) {
                            List<Cause> causes = error.getApiException().getCause();
                            if (causes != null && !causes.isEmpty()) {
                                Cause cause = causes.get(0);
                                if (ApiException.ErrorCodes.INVALID_ESC.equals(cause.getCode()) ||
                                    ApiException.ErrorCodes.INVALID_FINGERPRINT.equals(cause.getCode())) {

                                    getResourcesProvider().deleteESC(escCardToken.getCardId());

                                    esc = null;
                                    if (viewAttached()) {
                                        getView().startSecurityCodeActivity(TrackingUtil.SECURITY_CODE_REASON_ESC);
                                    }
                                } else {
                                    recoverCreateESCToken(error);
                                }
                            }
                        } else {
                            recoverCreateESCToken(error);
                        }
                    }
                });
        }
    }

    private void recoverCreateESCToken(final MercadoPagoError error) {
        if (viewAttached()) {
            getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);

            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    createESCToken();
                }
            });
        }
    }
}
