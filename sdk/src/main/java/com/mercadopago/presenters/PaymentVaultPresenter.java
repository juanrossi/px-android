package com.mercadopago.presenters;

import android.support.annotation.NonNull;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.constants.PaymentMethods;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.hooks.HookHelper;
import com.mercadopago.lite.util.CurrenciesUtil;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.CustomSearchItem;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentMethodSearchItem;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.plugins.PaymentMethodPlugin;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.providers.PaymentVaultProvider;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.MercadoPagoUtil;
import com.mercadopago.views.AmountView;
import com.mercadopago.views.PaymentVaultView;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentVaultPresenter extends MvpPresenter<PaymentVaultView, PaymentVaultProvider> implements
    AmountView.OnClick {

    private static final String MISMATCHING_PAYMENT_METHOD_ERROR = "Payment method in search not found";

    private Site site;
    private Discount discount;
    private Campaign campaign;
    private PaymentMethod selectedPaymentMethod;
    private PaymentMethodSearchItem selectedSearchItem;
    private PaymentMethodSearch paymentMethodSearch;
    private String payerAccessToken;
    private String payerEmail;
    private PaymentPreference paymentPreference;
    private BigDecimal amount;
    private Boolean installmentsReviewEnabled;
    private Boolean discountEnabled = true;
    private Boolean showAllSavedCardsEnabled = false;
    private Integer maxSavedCards;

    private boolean selectAutomatically;
    private FailureRecovery failureRecovery;

    private PaymentMethodSearchItem resumeItem;
    private boolean skipHook = false;
    private boolean hook1Displayed = false;

    public void initialize(boolean selectAutomatically) {
        try {
            this.selectAutomatically = selectAutomatically;
            validateParameters();
            onValidStart();
        } catch (IllegalStateException exception) {
            getView().showError(new MercadoPagoError(exception.getMessage(), false), "");
        }
    }

    private void onValidStart() {
        initPaymentVaultFlow();
    }

    private void initPaymentVaultFlow() {
        initializeAmountRow();

        if (isItemSelected()) {
            checkAvailablePaymentMethods();
        } else {
            initPaymentMethodSearch();
        }
    }

    public void onDiscountOptionSelected() {
        getView().startDiscountFlow(amount);
    }

    public void initializeAmountRow() {
        if (isViewAttached()) {
            getView().showAmount(discount, campaign, amount, site);
        }
    }

    public void onDiscountReceived(Discount discount) {
        setDiscount(discount);
        clearPaymentMethodOptions();
        initializeAmountRow();
        initPaymentVaultFlow();
    }

    public void onPayerInformationReceived(Payer payer) {
        getView().finishPaymentMethodSelection(selectedPaymentMethod, payer);
    }

    private void clearPaymentMethodOptions() {
        getView().cleanPaymentMethodOptions();
        paymentMethodSearch = null;
    }

    private void validateParameters() throws IllegalStateException {
        if (paymentPreference != null) {
            if (!paymentPreference.validMaxInstallments()) {
                throw new IllegalStateException(getResourcesProvider().getInvalidMaxInstallmentsErrorMessage());
            }
            if (!paymentPreference.validDefaultInstallments()) {
                throw new IllegalStateException(getResourcesProvider().getInvalidDefaultInstallmentsErrorMessage());
            }
            if (!paymentPreference.excludedPaymentTypesValid()) {
                throw new IllegalStateException(getResourcesProvider().getAllPaymentTypesExcludedErrorMessage());
            }
        }
        if (!isAmountValid(amount)) {
            throw new IllegalStateException(getResourcesProvider().getInvalidAmountErrorMessage());
        }
        if (!isSiteConfigurationValid()) {
            throw new IllegalStateException(getResourcesProvider().getInvalidSiteConfigurationErrorMessage());
        }
    }

    private Boolean isAmountValid(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isSiteConfigurationValid() {
        boolean isValid = true;
        if (site == null) {
            isValid = false;
        } else if (site.getCurrencyId() == null) {
            isValid = false;
        } else if (!CurrenciesUtil.isValidCurrency(site.getCurrencyId())) {
            isValid = false;
        }
        return isValid;
    }

    public boolean isItemSelected() {
        return selectedSearchItem != null;
    }

    private void initPaymentMethodSearch() {

        trackInitialScreen();

        getView().setTitle(getResourcesProvider().getTitle());

        checkAvailablePaymentMethods();
    }

    private void checkAvailablePaymentMethods() {
        if (paymentMethodSearch == null) {
            getPaymentMethodSearchAsync();
        } else {
            showPaymentMethodGroup();
        }
    }

    private void showPaymentMethodGroup() {
        if (isItemSelected()) {
            showSelectedItemChildren();
        } else {
            resolveAvailablePaymentMethods();
        }
    }

    private void reselectSearchItem() {
        if (selectedSearchItem != null) {
            if (paymentMethodSearch == null || noPaymentMethodsAvailable()) {
                showEmptyPaymentMethodsError();
            } else {
                //Reemplazo el selected item por el nuevo que vino de la api
                List<PaymentMethodSearchItem> groups = paymentMethodSearch.getGroups();
                for (PaymentMethodSearchItem item : groups) {
                    if (item.getId().equals(selectedSearchItem.getId())) {
                        selectedSearchItem = item;
                        break;
                    }
                }
            }
        }
    }

    public BigDecimal getTransactionAmount() {
        BigDecimal amount;

        if (discount != null && discountEnabled) {
            amount = discount.getAmountWithDiscount(this.amount);
        } else {
            amount = this.amount;
        }

        return amount;
    }

    private void getPaymentMethodSearchAsync() {
        if (isViewAttached()) {
            getView().showProgress();
            Payer payer = new Payer();
            payer.setAccessToken(payerAccessToken);

            getResourcesProvider().getPaymentMethodSearch(getTransactionAmount(), paymentPreference, payer, site,
                new TaggedCallback<PaymentMethodSearch>(ApiUtil.RequestOrigin.GET_PAYMENT_METHODS) {

                    @Override
                    public void onSuccess(PaymentMethodSearch paymentMethodSearch) {
                        PaymentVaultPresenter.this.paymentMethodSearch = paymentMethodSearch;
                        reselectSearchItem();
                        showPaymentMethodGroup();
                    }

                    @Override
                    public void onFailure(MercadoPagoError error) {
                        if (isViewAttached()) {
                            getView().showError(error, ApiUtil.RequestOrigin.PAYMENT_METHOD_SEARCH);

                            setFailureRecovery(new FailureRecovery() {
                                @Override
                                public void recover() {
                                    getPaymentMethodSearchAsync();
                                }
                            });
                        }
                    }
                });
        }
    }

    private void setFailureRecovery(FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    private void showSelectedItemChildren() {
        trackChildrenScreen();

        getView().setTitle(selectedSearchItem.getChildrenHeader());
        getView().showSearchItems(selectedSearchItem.getChildren(), getPaymentMethodSearchItemSelectionCallback());
        getView().hideProgress();
    }

    private void resolveAvailablePaymentMethods() {
        if (isViewAttached()) {
            if (noPaymentMethodsAvailable()) {
                showEmptyPaymentMethodsError();
            } else if (selectAutomatically && isOnlyOneItemAvailable()) {
                if (CheckoutStore.getInstance().hasEnabledPaymentMethodPlugin()) {
                    selectPluginPaymentMethod(CheckoutStore.getInstance().getFirstEnabledPluginId());
                } else if (paymentMethodSearch.getGroups() != null && !paymentMethodSearch.getGroups().isEmpty()) {
                    selectItem(paymentMethodSearch.getGroups().get(0), true);
                } else if (
                    paymentMethodSearch.getCustomSearchItems() != null &&
                        !paymentMethodSearch.getCustomSearchItems().isEmpty()) {
                    if (PaymentTypes.CREDIT_CARD.equals(paymentMethodSearch.getCustomSearchItems().get(0).getType())) {
                        selectCard(paymentMethodSearch.getCustomSearchItems().get(0));
                    }
                }
            } else {
                showAvailableOptions();
                getView().hideProgress();
            }
        }
    }

    private void selectItem(PaymentMethodSearchItem item) {
        selectItem(item, false);
    }

    private void selectItem(PaymentMethodSearchItem item, Boolean automaticSelection) {
        if (item.hasChildren()) {
            getView().showSelectedItem(item);
        } else if (item.isPaymentType()) {
            startNextStepForPaymentType(item, automaticSelection);
        } else if (item.isPaymentMethod()) {
            resolvePaymentMethodSelection(item);
        }
    }

    private void selectCard(final Card card) {
        getView().startSavedCardFlow(card, amount);
    }

    private void showAvailableOptions() {
        List<PaymentMethodPlugin> paymentMethodPluginList = CheckoutStore.getInstance().getPaymentMethodPluginList();

        getView().showPluginOptions(paymentMethodPluginList, PaymentMethodPlugin.POSIION_TOP);

        if (paymentMethodSearch.hasCustomSearchItems()) {
            List<CustomSearchItem> shownCustomItems;

            if (showAllSavedCardsEnabled) {
                shownCustomItems = paymentMethodSearch.getCustomSearchItems();
            } else {
                shownCustomItems = getLimitedCustomOptions(paymentMethodSearch.getCustomSearchItems(), maxSavedCards);
            }

            getView().showCustomOptions(shownCustomItems, getCustomOptionCallback());
        }

        if (searchItemsAvailable()) {
            getView().showSearchItems(paymentMethodSearch.getGroups(), getPaymentMethodSearchItemSelectionCallback());
        }

        getView().showPluginOptions(paymentMethodPluginList, PaymentMethodPlugin.POSIION_BOTTOM);
    }

    private OnSelectedCallback<PaymentMethodSearchItem> getPaymentMethodSearchItemSelectionCallback() {
        return new OnSelectedCallback<PaymentMethodSearchItem>() {
            @Override
            public void onSelected(PaymentMethodSearchItem item) {
                selectItem(item);
            }
        };
    }

    private OnSelectedCallback<CustomSearchItem> getCustomOptionCallback() {
        return new OnSelectedCallback<CustomSearchItem>() {
            @Override
            public void onSelected(final CustomSearchItem searchItem) {
                selectCard(searchItem);
            }
        };
    }

    private void selectCard(final CustomSearchItem item) {
        if (MercadoPagoUtil.isCard(item.getType())) {
            Card card = getCardWithPaymentMethod(item);
            selectCard(card);
        }
    }

    public void selectPluginPaymentMethod(final String id) {
        final CheckoutStore store = CheckoutStore.getInstance();
        store.reset();
        store.setSelectedPaymentMethodId(id);

        if (!showHook1(PaymentTypes.PLUGIN, MercadoPagoComponents.Activities.HOOK_1_PLUGIN)) {
            getView().showPaymentMethodPluginConfiguration();
        }
    }

    private Card getCardWithPaymentMethod(CustomSearchItem searchItem) {
        PaymentMethod paymentMethod = paymentMethodSearch.getPaymentMethodById(searchItem.getPaymentMethodId());
        Card selectedCard = getCardById(paymentMethodSearch.getCards(), searchItem.getId());
        if (paymentMethod != null) {
            selectedCard.setPaymentMethod(paymentMethod);
            if (selectedCard.getSecurityCode() == null && paymentMethod.getSettings() != null &&
                paymentMethod.getSettings().get(0) != null) {
                selectedCard.setSecurityCode(paymentMethod.getSettings().get(0).getSecurityCode());
            }
        }
        return selectedCard;
    }

    private Card getCardById(List<Card> savedCards, String cardId) {
        Card foundCard = null;
        for (Card card : savedCards) {
            if (card.getId().equals(cardId)) {
                foundCard = card;
                break;
            }
        }
        return foundCard;
    }

    private void startNextStepForPaymentType(final PaymentMethodSearchItem item, final boolean automaticSelection) {

        if (skipHook || (!hook1Displayed && !showHook1(item.getId()))) {
            skipHook = false;

            if (paymentPreference == null) {
                paymentPreference = new PaymentPreference();
            }

            if (MercadoPagoUtil.isCard(item.getId())) {
                getView().startCardFlow(item.getId(), amount, automaticSelection);
            } else {
                getView().startPaymentMethodsSelection();
            }
        } else {

            resumeItem = item;
        }
    }

    private void resolvePaymentMethodSelection(PaymentMethodSearchItem item) {

        final PaymentMethod selectedPaymentMethod = paymentMethodSearch.getPaymentMethodBySearchItem(item);

        if (skipHook || (!hook1Displayed && !showHook1(selectedPaymentMethod.getPaymentTypeId()))) {
            skipHook = false;

            if (selectedPaymentMethod == null) {
                showMismatchingPaymentMethodError();
            } else if (selectedPaymentMethod.getId().equals(PaymentMethods.BRASIL.BOLBRADESCO)) {
                this.selectedPaymentMethod = selectedPaymentMethod;
                getView().collectPayerInformation();
            } else {
                getView().finishPaymentMethodSelection(selectedPaymentMethod);
            }
        } else {
            resumeItem = item;
        }
    }

    public boolean isOnlyOneItemAvailable() {
        final CheckoutStore store = CheckoutStore.getInstance();

        int groupCount = 0;
        int customCount = 0;
        int pluginCount = 0;

        if (paymentMethodSearch != null && paymentMethodSearch.hasSearchItems()) {
            groupCount = paymentMethodSearch.getGroups().size();
        }

        if (paymentMethodSearch != null && paymentMethodSearch.hasCustomSearchItems()) {
            customCount = paymentMethodSearch.getCustomSearchItems().size();
        }

        pluginCount = store.getPaymentMethodPluginCount();

        return groupCount + customCount + pluginCount == 1;
    }

    private boolean searchItemsAvailable() {
        return paymentMethodSearch != null && paymentMethodSearch.getGroups() != null
            &&
            (!paymentMethodSearch.getGroups().isEmpty() || CheckoutStore.getInstance().hasEnabledPaymentMethodPlugin());
    }

    private boolean noPaymentMethodsAvailable() {
        return (paymentMethodSearch.getGroups() == null || paymentMethodSearch.getGroups().isEmpty())
            &&
            (paymentMethodSearch.getCustomSearchItems() == null || paymentMethodSearch.getCustomSearchItems().isEmpty())
            && !CheckoutStore.getInstance().hasEnabledPaymentMethodPlugin();
    }

    private void showEmptyPaymentMethodsError() {
        String errorMessage = getResourcesProvider().getEmptyPaymentMethodsErrorMessage();
        getView().showError(new MercadoPagoError(errorMessage, false), "");
    }

    private void showMismatchingPaymentMethodError() {
        String errorMessage = getResourcesProvider().getStandardErrorMessage();
        getView().showError(new MercadoPagoError(errorMessage, MISMATCHING_PAYMENT_METHOD_ERROR, false), "");
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site mSite) {
        this.site = mSite;
    }

    public PaymentMethodSearchItem getSelectedSearchItem() {
        return selectedSearchItem;
    }

    public void setSelectedSearchItem(PaymentMethodSearchItem mSelectedSearchItem) {
        this.selectedSearchItem = mSelectedSearchItem;
    }

    public PaymentMethodSearch getPaymentMethodSearch() {
        return paymentMethodSearch;
    }

    public void setPaymentMethodSearch(PaymentMethodSearch mPaymentMethodSearch) {
        this.paymentMethodSearch = mPaymentMethodSearch;
    }

    public PaymentPreference getPaymentPreference() {
        return paymentPreference;
    }

    public void setPaymentPreference(PaymentPreference mPaymentPreference) {
        this.paymentPreference = mPaymentPreference;
    }

    public void setAmount(BigDecimal mAmount) {
        this.amount = mAmount;
    }

    public void setPayerAccessToken(String payerAccessToken) {
        this.payerAccessToken = payerAccessToken;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Campaign getCampaign() {
        return campaign;
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

    public Boolean getInstallmentsReviewEnabled() {
        return installmentsReviewEnabled;
    }

    public void setDiscountEnabled(Boolean discountEnabled) {
        this.discountEnabled = discountEnabled;
    }

    public Boolean getDiscountEnabled() {
        return discountEnabled;
    }

    public void setMaxSavedCards(int maxSavedCards) {
        this.maxSavedCards = maxSavedCards;
    }

    public void setShowAllSavedCardsEnabled(boolean showAll) {
        showAllSavedCardsEnabled = showAll;
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    private List<CustomSearchItem> getLimitedCustomOptions(List<CustomSearchItem> customSearchItems,
        Integer maxSavedCards) {
        List<CustomSearchItem> limitedItems = new ArrayList<>();
        if (maxSavedCards != null && maxSavedCards > 0) {
            int cardsAdded = 0;
            for (CustomSearchItem customSearchItem : customSearchItems) {
                if (MercadoPagoUtil.isCard(customSearchItem.getType()) && cardsAdded < maxSavedCards) {
                    limitedItems.add(customSearchItem);
                    cardsAdded++;
                } else if (!MercadoPagoUtil.isCard(customSearchItem.getType())) {
                    limitedItems.add(customSearchItem);
                }
            }
        } else {
            limitedItems = customSearchItems;
        }

        return limitedItems;
    }

    //###Hooks HACKS #######################################################

    public void onHookContinue() {
        if (resumeItem != null) {
            skipHook = true;
            selectItem(resumeItem, true);
        }
    }

    public void onHookReset() {
        hook1Displayed = false;
        resumeItem = null;
    }

    public boolean showHook1(final String typeId) {
        return showHook1(typeId, MercadoPagoComponents.Activities.HOOK_1);
    }

    public boolean showHook1(final String typeId, final int requestCode) {

        final Map<String, Object> data = CheckoutStore.getInstance().getData();
        final Hook hook = HookHelper.activateBeforePaymentMethodConfig(
            CheckoutStore.getInstance().getCheckoutHooks(), typeId, data);

        if (resumeItem == null && hook != null && getView() != null) {
            hook1Displayed = true;
            getView().showHook(hook, requestCode);
            return true;
        }
        return false;
    }

    public void trackInitialScreen() {
        getResourcesProvider().trackInitialScreen(paymentMethodSearch, site.getId());
    }

    /**
     * When users selects option then track payment selected method.
     * If there is no option selected by the user then track the first available.
     */
    public void trackChildrenScreen() {
        if (selectedSearchItem != null) {
            getResourcesProvider().trackChildrenScreen(selectedSearchItem, site.getId());
        } else if (paymentMethodSearch.hasSearchItems()) {
            getResourcesProvider().trackChildrenScreen(paymentMethodSearch.getGroups().get(0), site.getId());
        } else {
            throw new IllegalStateException("No payment method available to track");
        }
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