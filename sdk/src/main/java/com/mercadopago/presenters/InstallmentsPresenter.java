package com.mercadopago.presenters;

import android.support.annotation.NonNull;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.controllers.PaymentMethodGuessingController;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.providers.InstallmentsProvider;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.InstallmentsUtil;
import com.mercadopago.views.AmountView;
import com.mercadopago.views.InstallmentsActivityView;
import java.util.List;

public class InstallmentsPresenter extends MvpPresenter<InstallmentsActivityView, InstallmentsProvider> implements
    AmountView.OnClick {

    @NonNull private final AmountRepository amountRepository;
    private final PaymentSettingRepository configuration;
    private final UserSelectionRepository userSelectionRepository;

    private FailureRecovery mFailureRecovery;

    //Card Info
    private String bin = "";
    private Long issuerId;

    //Activity parameters
    private String payerEmail;
    private PaymentMethod paymentMethod;
    private Issuer issuer;

    private List<PayerCost> payerCosts;
    private PaymentPreference paymentPreference;
    private CardInfo cardInfo;
    private Boolean installmentsReviewEnabled;

    public InstallmentsPresenter(@NonNull final AmountRepository amountRepository,
        final PaymentSettingRepository configuration,
        final UserSelectionRepository userSelectionRepository) {
        this.amountRepository = amountRepository;
        this.configuration = configuration;
        this.userSelectionRepository = userSelectionRepository;
    }

    public void initialize() {
        initializeAmountRow();
        showSiteRelatedInformation();
        loadPayerCosts();
    }

    public void initializeAmountRow() {
        if (isViewAttached()) {
            getView().showAmount(configuration.getDiscount(), configuration.getCampaign(),
                amountRepository.getItemsPlusCharges(), configuration.getCheckoutPreference().getSite());
        }
    }

    private void showSiteRelatedInformation() {
        if (InstallmentsUtil.shouldWarnAboutBankInterests(configuration.getCheckoutPreference().getSiteId())) {
            getView().warnAboutBankInterests();
        }
    }

    private void loadPayerCosts() {
        if (werePayerCostsSet()) {
            resolvePayerCosts(payerCosts);
        } else {
            getInstallmentsAsync();
        }
    }

    private boolean werePayerCostsSet() {
        return payerCosts != null;
    }

    private void resolvePayerCosts(List<PayerCost> payerCosts) {
        PayerCost defaultPayerCost =
            paymentPreference == null ? null : paymentPreference.getDefaultInstallments(payerCosts);
        this.payerCosts =
            paymentPreference == null ? payerCosts : paymentPreference.getInstallmentsBelowMax(payerCosts);

        if (defaultPayerCost != null) {
            getView().finishWithResult(defaultPayerCost);
        } else if (this.payerCosts.isEmpty()) {
            getView().showError(getResourcesProvider().getNoPayerCostFoundError(), "");
        } else if (this.payerCosts.size() == 1) {
            getView().finishWithResult(payerCosts.get(0));
        } else {
            getView().showHeader();
            getView().showInstallments(this.payerCosts, getDpadSelectionCallback());
            getView().hideLoadingView();
        }
    }

    private void getInstallmentsAsync() {
        getView().showLoadingView();

        getResourcesProvider().getInstallments(bin, amountRepository.getAmountToPay(), issuerId, paymentMethod.getId(),
            new TaggedCallback<List<Installment>>(ApiUtil.RequestOrigin.GET_INSTALLMENTS) {
                @Override
                public void onSuccess(List<Installment> installments) {
                    if (installments.size() == 0) {
                        getView().showError(getResourcesProvider().getNoInstallmentsFoundError(), "");
                    } else if (installments.size() == 1) {
                        resolvePayerCosts(installments.get(0).getPayerCosts());
                    } else {
                        getView().showError(getResourcesProvider().getMultipleInstallmentsFoundForAnIssuerError(), "");
                    }
                }

                @Override
                public void onFailure(MercadoPagoError mercadoPagoError) {
                    getView().hideLoadingView();
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            getInstallmentsAsync();
                        }
                    });
                    getView().showError(mercadoPagoError, ApiUtil.RequestOrigin.GET_INSTALLMENTS);
                }
            });
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        if (this.cardInfo != null) {
            bin = this.cardInfo.getFirstSixDigits();
        }
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
        if (this.issuer != null) {
            issuerId = this.issuer.getId();
        }
    }

    public Integer getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(paymentMethod, bin);
    }

    public void setPayerCosts(List<PayerCost> payerCosts) {
        this.payerCosts = payerCosts;
    }

    public void setPaymentPreference(PaymentPreference paymentPreference) {
        this.paymentPreference = paymentPreference;
    }

    public void setFailureRecovery(FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public FailureRecovery getFailureRecovery() {
        return mFailureRecovery;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public boolean isRequiredCardDrawn() {
        return cardInfo != null && paymentMethod != null;
    }

    public void onDiscountReceived(Discount discount) {
        configuration.configure(discount);
        initializeAmountRow();
        getInstallmentsAsync();
    }

    private OnSelectedCallback<Integer> getDpadSelectionCallback() {
        return new OnSelectedCallback<Integer>() {
            @Override
            public void onSelected(Integer position) {
                onItemSelected(position);
            }
        };
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }


    public String getPayerEmail() {
        return payerEmail;
    }

    public void setInstallmentsReviewEnabled(Boolean installmentReviewEnabled) {
        installmentsReviewEnabled = installmentReviewEnabled;
    }

    public String getBin() {
        return bin;
    }

    public Long getIssuerId() {
        return issuerId;
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public void onItemSelected(int position) {
        PayerCost selectedPayerCost = payerCosts.get(position);
        userSelectionRepository.select(selectedPayerCost);
        if (isInstallmentsReviewEnabled() && isInstallmentsReviewRequired(selectedPayerCost)) {
            getView().hideInstallmentsRecyclerView();
            getView().showInstallmentsReviewView();
            getView().initInstallmentsReviewView(selectedPayerCost);
        } else {
            getView().finishWithResult(selectedPayerCost);
        }
    }

    private Boolean isInstallmentsReviewEnabled() {
        return installmentsReviewEnabled != null && installmentsReviewEnabled;
    }

    private Boolean isInstallmentsReviewRequired(PayerCost payerCost) {
        return payerCost != null && payerCost.getCFTPercent() != null;
    }

    @Override
    public void onDetailClicked(@NonNull final Discount discount, @NonNull final Campaign campaign) {
        getView().showDetailDialog(discount, campaign);
    }

    @Override
    public void onDetailClicked(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign) {
        getView().showDetailDialog(discount, campaign);
    }

    @Override
    public void onInputRequestClicked() {
        getView().showDiscountInputDialog();
    }
}
