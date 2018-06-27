package com.mercadopago.paymentvault;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.constants.PaymentMethods;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.internal.repository.PluginRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.mocks.PaymentMethodSearchs;
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
import com.mercadopago.mvp.TaggedCallback;
import com.mercadopago.plugins.PaymentMethodPlugin;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.presenters.PaymentVaultPresenter;
import com.mercadopago.providers.PaymentVaultProvider;
import com.mercadopago.utils.Discounts;
import com.mercadopago.views.PaymentVaultView;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentVaultPresenterTest {

    private MockedView mockedView = new MockedView();
    private MockedProvider provider = new MockedProvider();
    private PaymentVaultPresenter presenter;

    @Mock private AmountRepository amountRepository;
    @Mock private PaymentSettingRepository settingRepository;
    @Mock private CheckoutPreference checkoutPreference;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private PluginRepository pluginRepository;

    @Before
    public void setUp() {
        when(checkoutPreference.getTotalAmount()).thenReturn(new BigDecimal(100));
        when(settingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(new PaymentPreference());
        presenter = new PaymentVaultPresenter(amountRepository, settingRepository, userSelectionRepository,
            pluginRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
    }

    @Test
    public void whenItemSelectedAvailableTrackIt() {
        PaymentVaultView mockView = mock(PaymentVaultView.class);
        PaymentVaultProvider mockProvider = mock(PaymentVaultProvider.class);
        Site mockSite = mock(Site.class);
        PaymentMethodSearchItem mockPaymentOptions = mock(PaymentMethodSearchItem.class);
        presenter.attachView(mockView);
        presenter.attachResourcesProvider(mockProvider);
        presenter.setSelectedSearchItem(mockPaymentOptions);
        presenter.trackChildrenScreen();
        verify(mockProvider).trackChildrenScreen(mockPaymentOptions, mockSite.getId());
        verifyNoMoreInteractions(mockProvider);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void whenItemSelectedNotAvailableTrackFirstOfGroup() {
        PaymentVaultView mockView = mock(PaymentVaultView.class);
        PaymentVaultProvider mockProvider = mock(PaymentVaultProvider.class);
        Site mockSite = mock(Site.class);

        PaymentMethodSearch mockPaymentOptions = mock(PaymentMethodSearch.class);
        PaymentMethodSearchItem mockPaymentOptionsItem = mock(PaymentMethodSearchItem.class);

        List<PaymentMethodSearchItem> paymentMethodSearchItems = Arrays.asList(mockPaymentOptionsItem);
        when(mockPaymentOptions.getGroups()).thenReturn(paymentMethodSearchItems);
        when(mockPaymentOptions.hasSearchItems()).thenReturn(true);

        presenter.setPaymentMethodSearch(mockPaymentOptions);
        presenter.attachResourcesProvider(mockProvider);
        presenter.attachView(mockView);
        presenter.trackChildrenScreen();
        verify(mockProvider).trackChildrenScreen(paymentMethodSearchItems.get(0), mockSite.getId());
        verifyNoMoreInteractions(mockProvider);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void ifNoPaymentMethodsAvailableThenShowError() {
        PaymentMethodSearch paymentMethodSearch = new PaymentMethodSearch();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(true);
        assertTrue(mockedView.errorShown.getMessage().equals(MockedProvider.EMPTY_PAYMENT_METHODS));
    }

    @Test
    public void ifPaymentMethodSearchHasItemsShowThem() {
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(true);
        assertEquals(paymentMethodSearch.getGroups(), mockedView.searchItemsShown);
    }

    @Test
    public void ifPaymentMethodSearchHasPayerCustomOptionsShowThem() {
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(true);
        assertEquals(paymentMethodSearch.getCustomSearchItems(), mockedView.customOptionsShown);
    }

    @Test
    public void whenItemWithChildrenSelectedThenShowChildren() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);
        mockedView.simulateItemSelection(1);

        assertEquals(paymentMethodSearch.getGroups().get(1).getChildren(), mockedView.searchItemsShown);
    }

    //Automatic selections

    @Ignore
    @Test
    public void ifOnlyUniqueSearchItemAvailableRestartWithItSelected() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithOnlyTicketMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        assertEquals(paymentMethodSearch.getGroups().get(0), mockedView.itemShown);
    }

    @Ignore
    @Test
    public void ifOnlyCardPaymentTypeAvailableStartCardFlow() {
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(true);
        assertTrue(mockedView.cardFlowStarted);
    }

    @Test
    public void ifOnlyCardPaymentTypeAvailableAndCardAvailableDoNotSelectAutomatically() {

        PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardAndOneCardMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        assertTrue(mockedView.customOptionsShown != null);
        assertFalse(mockedView.cardFlowStarted);
        assertFalse(mockedView.isItemShown);
    }

    @Test
    public void ifOnlyCardPaymentTypeAvailableButAutomaticSelectionDisabledThenDoNotSelectAutomatically() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(false);

        assertFalse(mockedView.cardFlowStarted);
    }

    @Test
    public void ifOnlyCardPaymentTypeAvailableAndAccountMoneyAvailableDoNotSelectAutomatically() {

        PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardAndAccountMoneyMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        assertTrue(mockedView.customOptionsShown != null);
        assertFalse(mockedView.cardFlowStarted);
        assertFalse(mockedView.isItemShown);
    }

    @Test
    public void ifOnlyOffPaymentTypeAvailableAndAccountMoneyAvailableDoNotSelectAutomatically() {

        PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyOneOffTypeAndAccountMoneyMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        assertTrue(mockedView.customOptionsShown != null);
        assertFalse(mockedView.cardFlowStarted);
        assertFalse(mockedView.isItemShown);
    }

    //User selections

    @Test
    public void ifItemSelectedShowItsChildren() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateItemSelection(1);

        assertEquals(paymentMethodSearch.getGroups().get(1).getChildren(), mockedView.searchItemsShown);
        assertEquals(paymentMethodSearch.getGroups().get(1), mockedView.itemShown);
    }

    @Test
    public void ifCardPaymentTypeSelectedStartCardFlow() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateItemSelection(0);

        assertTrue(mockedView.cardFlowStarted);
    }

    @Test
    public void ifSavedCardSelectedStartSavedCardFlow() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateCustomItemSelection(1);

        assertTrue(mockedView.savedCardFlowStarted);
        assertTrue(mockedView.savedCardSelected.equals(paymentMethodSearch.getCards().get(0)));
    }

    //Payment Preference tests
    @Test
    public void ifAllPaymentTypesExcludedShowError() {
        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setExcludedPaymentTypeIds(PaymentTypes.getAllPaymentTypes());

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        provider.setResponse(paymentMethodSearch);

        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        presenter.initialize(true);

        assertEquals(MockedProvider.ALL_TYPES_EXCLUDED, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInvalidDefaultInstallmentsShowError() {
        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(BigDecimal.ONE.negate().intValue());

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        provider.setResponse(paymentMethodSearch);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        presenter.initialize(true);

        assertEquals(MockedProvider.INVALID_DEFAULT_INSTALLMENTS, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInvalidMaxInstallmentsShowError() {
        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setMaxAcceptedInstallments(BigDecimal.ONE.negate().intValue());

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        provider.setResponse(paymentMethodSearch);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        presenter.initialize(true);

        assertEquals(MockedProvider.INVALID_MAX_INSTALLMENTS, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifMaxSavedCardNotSetDoNotLimitCardsShown() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        assertEquals(paymentMethodSearch.getCustomSearchItems().size(), mockedView.customOptionsShown.size());
    }

    @Test
    public void ifMaxSavedCardLimitCardsShown() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.setMaxSavedCards(1);

        presenter.initialize(true);

        //Account money + 1 card
        assertEquals(2, mockedView.customOptionsShown.size());
    }

    //Discounts
    @Test
    public void ifDiscountsAreNotEnabledNotShowDiscountRow() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateItemSelection(0);

        assertTrue(mockedView.showedDiscountRow);
    }

    @Test
    public void ifDiscountsAreEnabledGetDirectDiscount() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        Discount discount = Discounts.getDiscountWithAmountOffMLA();
        provider.setDiscountResponse(discount);

        presenter.initialize(true);
    }

    @Test
    public void ifHasNotDirectDiscountsShowDiscountRow() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        ApiException apiException = Discounts.getDoNotFindCampaignApiException();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setDiscountResponse(mpException);

        presenter.initialize(true);

        mockedView.simulateItemSelection(0);

        assertTrue(provider.failedResponse.getApiException().getError().equals(provider.CAMPAIGN_DOES_NOT_MATCH_ERROR));
        assertTrue(mockedView.showedDiscountRow);
    }

    @Test
    public void ifIsDirectDiscountNotEnabledNotGetDirectDiscount() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateItemSelection(0);

        assertTrue(mockedView.showedDiscountRow);
    }

    @Test
    public void ifResourcesRetrievalFailThenShowError() {

        ApiException apiException = new ApiException();
        apiException.setMessage("Mocked failure");
        MercadoPagoError mercadoPagoError = new MercadoPagoError(apiException, "");
        provider.setResponse(mercadoPagoError);

        presenter.setMaxSavedCards(1);

        presenter.initialize(true);

        assertTrue(mockedView.errorShown.getApiException().equals(mercadoPagoError.getApiException()));
    }

    @Test
    public void whenResourcesRetrievalFailedAndRecoverRequestedThenRepeatRetrieval() {
        //Set Up

        ApiException apiException = new ApiException();
        apiException.setMessage("Mocked failure");
        MercadoPagoError mercadoPagoError = new MercadoPagoError(apiException, "");
        provider.setResponse(mercadoPagoError);

        presenter.setMaxSavedCards(1);

        presenter.initialize(true);
        //Presenter gets resources, fails

        provider.setResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        presenter.recoverFromFailure();

        assertFalse(mockedView.searchItemsShown.isEmpty());
    }

    @Test
    public void whenResourcesRetrievalFailedButNoViewAttachedThenDoNotRepeatRetrieval() {

        ApiException apiException = new ApiException();
        apiException.setMessage("Mocked failure");
        MercadoPagoError mercadoPagoError = new MercadoPagoError(apiException, "");
        provider.setResponse(mercadoPagoError);

        presenter.setMaxSavedCards(1);

        presenter.initialize(true);

        presenter.detachView();

        provider.setResponse(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA());
        presenter.recoverFromFailure();

        assertTrue(mockedView.searchItemsShown == null);
    }

    @Test
    public void ifPaymentMethodSearchSetAndHasItemsThenShowThem() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        presenter.setPaymentMethodSearch(paymentMethodSearch);
        presenter.initialize(true);

        assertEquals(paymentMethodSearch.getGroups(), mockedView.searchItemsShown);
    }

    @Test
    public void ifPaymentMethodSearchItemIsNotCardAndDoesNotHaveChildrenThenStartPaymentMethodsSelection() {

        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        paymentMethodSearch.getGroups().get(1).getChildren()
            .removeAll(paymentMethodSearch.getGroups().get(1).getChildren());

        provider.setResponse(paymentMethodSearch);

        presenter.initialize(true);

        mockedView.simulateItemSelection(1);
        assertTrue(mockedView.paymentMethodSelectionStarted);
    }

    @Test
    public void ifPaymentMethodTypeSelectedThenSelectPaymentMethod() {
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithPaymentMethodOnTop();
        provider.setResponse(paymentMethodSearch);
        presenter.initialize(true);

        mockedView.simulateItemSelection(1);
        assertTrue(paymentMethodSearch.getGroups().get(1).getId().equals(mockedView.selectedPaymentMethod.getId()));
    }

    @Test
    public void ifShowAllSavedCardsTestThenShowThem() {
        // 6 Saved Cards + Account Money
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();

        provider.setResponse(paymentMethodSearch);

        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        // Set show all saved cards
        presenter.setShowAllSavedCardsEnabled(true);
        presenter.setMaxSavedCards(FlowPreference.DEFAULT_MAX_SAVED_CARDS_TO_SHOW);

        presenter.initialize(true);

        assertEquals(mockedView.customOptionsShown.size(), paymentMethodSearch.getCustomSearchItems().size());
    }

    @Test
    public void ifMaxSavedCardsSetThenShowWithLimit() {
        // 6 Saved Cards + Account Money
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
        presenter.setMaxSavedCards(4);
        presenter.initialize(true);
        // 4 Cards + Account Money
        assertEquals(mockedView.customOptionsShown.size(), 5);
    }

    @Test
    public void ifMaxSavedCardsSetThenShowWithLimitAgain() {
        // 6 Saved Cards + Account Money
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.setMaxSavedCards(1);

        presenter.initialize(true);

        // 1 Card + Account Money
        assertEquals(mockedView.customOptionsShown.size(), 2);
    }

    @Test
    public void ifMaxSavedCardsSetAndShowAllSetThenShowAllSavedCards() {
        // 6 Saved Cards + Account Money
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();
        provider.setResponse(paymentMethodSearch);
        presenter.setShowAllSavedCardsEnabled(true);
        presenter.setMaxSavedCards(4);

        presenter.initialize(true);

        assertEquals(mockedView.customOptionsShown.size(), paymentMethodSearch.getCustomSearchItems().size());
    }

    @Test
    public void ifMaxSavedCardsSetMoreThanActualAmountOfCardsThenShowAll() {
        // 6 Saved Cards + Account Money
        PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();
        provider.setResponse(paymentMethodSearch);
        // More cards than we have
        presenter.setMaxSavedCards(8);
        presenter.initialize(true);
        // Show every card we have
        assertEquals(mockedView.customOptionsShown.size(), paymentMethodSearch.getCustomSearchItems().size());
    }

    @Ignore
    @Test
    public void ifBoletoSelectedThenCollectPayerInformation() {

        // Setup mocks
        PaymentMethodSearchItem boletoItem = new PaymentMethodSearchItem();
        boletoItem.setId(PaymentMethods.BRASIL.BOLBRADESCO);
        boletoItem.setType("payment_method");

        PaymentMethodSearchItem anotherItem = new PaymentMethodSearchItem();
        anotherItem.setId(PaymentMethods.BRASIL.HIPERCARD);
        anotherItem.setType("payment_method");

        PaymentMethod boleto = new PaymentMethod();
        boleto.setId(PaymentMethods.BRASIL.BOLBRADESCO);
        boleto.setPaymentTypeId(PaymentTypes.TICKET);

        List<PaymentMethodSearchItem> items = new ArrayList<>();
        items.add(boletoItem);
        items.add(anotherItem);

        PaymentMethodSearch paymentMethodSearch = mock(PaymentMethodSearch.class);
        when(paymentMethodSearch.getGroups()).thenReturn(items);
        when(paymentMethodSearch.getPaymentMethodBySearchItem(boletoItem)).thenReturn(boleto);

        // Setup presenter

        presenter.setPaymentMethodSearch(paymentMethodSearch);
        // Simulate selection
        presenter.initialize(true);
        mockedView.simulateItemSelection(0);

        assertTrue(mockedView.payerInformationStarted);
    }

    @Ignore
    @Test
    public void ifPayerInformationCollectedThenFinishWithPaymentMethodAndPayer() {

        // Setup mocks
        PaymentMethodSearchItem boletoItem = new PaymentMethodSearchItem();
        boletoItem.setId(PaymentMethods.BRASIL.BOLBRADESCO);
        boletoItem.setType("payment_method");

        PaymentMethodSearchItem anotherItem = new PaymentMethodSearchItem();
        anotherItem.setId(PaymentMethods.BRASIL.HIPERCARD);
        anotherItem.setType("payment_method");

        PaymentMethod boleto = new PaymentMethod();
        boleto.setId(PaymentMethods.BRASIL.BOLBRADESCO);
        boleto.setPaymentTypeId(PaymentTypes.TICKET);

        List<PaymentMethodSearchItem> items = new ArrayList<>();
        items.add(boletoItem);
        items.add(anotherItem);

        PaymentMethodSearch paymentMethodSearch = mock(PaymentMethodSearch.class);
        when(paymentMethodSearch.getGroups()).thenReturn(items);
        when(paymentMethodSearch.getPaymentMethodBySearchItem(boletoItem)).thenReturn(boleto);

        Payer payer = new Payer();

        presenter.setPaymentMethodSearch(paymentMethodSearch);

        // Simulate selection
        presenter.initialize(true);
        mockedView.simulateItemSelection(0);

        presenter.onPayerInformationReceived(payer);

        assertEquals(boleto, mockedView.selectedPaymentMethod);
        assertEquals(payer, mockedView.selectedPayer);
    }

    private class MockedProvider implements PaymentVaultProvider {

        private static final String INVALID_SITE = "invalid site";
        private static final String INVALID_AMOUNT = "invalid amount";
        private static final String ALL_TYPES_EXCLUDED = "all types excluded";
        private static final String INVALID_DEFAULT_INSTALLMENTS = "invalid default installments";
        private static final String INVALID_MAX_INSTALLMENTS = "invalid max installments";
        private static final String STANDARD_ERROR_MESSAGE = "standard error";
        private static final String EMPTY_PAYMENT_METHODS = "empty payment methods";
        private static final String CAMPAIGN_DOES_NOT_MATCH_ERROR = "campaign-doesnt-match";

        private boolean shouldFail;
        private boolean shouldDiscountFail;
        private PaymentMethodSearch successfulResponse;
        private Discount successfulDiscountResponse;
        private MercadoPagoError failedResponse;

        public void setResponse(PaymentMethodSearch paymentMethodSearch) {
            shouldFail = false;
            successfulResponse = paymentMethodSearch;
        }

        public void setResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }

        public void setDiscountResponse(Discount discount) {
            shouldDiscountFail = false;
            successfulDiscountResponse = discount;
        }

        public void setDiscountResponse(MercadoPagoError exception) {
            shouldDiscountFail = true;
            failedResponse = exception;
        }

        @Override
        public String getTitle() {
            return "¿Cómo quieres pagar?";
        }

        @Override
        public void getPaymentMethodSearch(final BigDecimal amount, final PaymentPreference paymentPreference,
            final Payer payer, final Site site, final List<String> cardsWithEsc, final List<String> supportedPlugins,
            final TaggedCallback<PaymentMethodSearch> taggedCallback) {
            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulResponse);
            }
        }

        @Override
        public void getDirectDiscount(String amount, String payerEmail, TaggedCallback<Discount> taggedCallback) {
            if (shouldDiscountFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulDiscountResponse);
            }
        }

        @Override
        public String getInvalidSiteConfigurationErrorMessage() {
            return INVALID_SITE;
        }

        @Override
        public String getInvalidAmountErrorMessage() {
            return INVALID_AMOUNT;
        }

        @Override
        public String getAllPaymentTypesExcludedErrorMessage() {
            return ALL_TYPES_EXCLUDED;
        }

        @Override
        public String getInvalidDefaultInstallmentsErrorMessage() {
            return INVALID_DEFAULT_INSTALLMENTS;
        }

        @Override
        public String getInvalidMaxInstallmentsErrorMessage() {
            return INVALID_MAX_INSTALLMENTS;
        }

        @Override
        public String getStandardErrorMessage() {
            return STANDARD_ERROR_MESSAGE;
        }

        @Override
        public String getEmptyPaymentMethodsErrorMessage() {
            return EMPTY_PAYMENT_METHODS;
        }

        @Override
        public void trackInitialScreen(PaymentMethodSearch paymentMethodSearch, String siteId) {

        }

        @Override
        public void trackChildrenScreen(@NonNull PaymentMethodSearchItem paymentMethodSearchItem,
            @NonNull String siteId) {

        }

        @Override
        public List<String> getCardsWithEsc() {
            return new ArrayList<>();
        }
    }

    private class MockedView implements PaymentVaultView {

        private List<PaymentMethodSearchItem> searchItemsShown;
        private MercadoPagoError errorShown;
        private List<CustomSearchItem> customOptionsShown;
        private PaymentMethodSearchItem itemShown;
        private boolean cardFlowStarted = false;
        private boolean isItemShown;
        private PaymentMethod selectedPaymentMethod;
        private OnSelectedCallback<PaymentMethodSearchItem> itemSelectionCallback;
        private OnSelectedCallback<CustomSearchItem> customItemSelectionCallback;
        private String title;
        private boolean savedCardFlowStarted;
        private boolean payerInformationStarted;
        private Card savedCardSelected;
        private Boolean showedDiscountRow;

        private boolean paymentMethodSelectionStarted = false;
        private Payer selectedPayer;

        @Override
        public void startSavedCardFlow(final Card card) {
            this.savedCardFlowStarted = true;
            this.savedCardSelected = card;
        }

        @Override
        public void showPaymentMethodPluginActivity() {
            //Not yet tested
        }

        @Override
        public void showSelectedItem(PaymentMethodSearchItem item) {
            this.itemShown = item;
            this.isItemShown = true;
            this.searchItemsShown = item.getChildren();
        }

        @Override
        public void showProgress() {
            //Not yet tested
        }

        @Override
        public void hideProgress() {
            //Not yet tested
        }

        @Override
        public void showCustomOptions(List<CustomSearchItem> customSearchItems,
            OnSelectedCallback<CustomSearchItem> customSearchItemOnSelectedCallback) {
            this.customOptionsShown = customSearchItems;
            this.customItemSelectionCallback = customSearchItemOnSelectedCallback;
        }

        @Override
        public void showPluginOptions(List<PaymentMethodPlugin> items, String position) {

        }

        @Override
        public void showSearchItems(List<PaymentMethodSearchItem> searchItems,
            OnSelectedCallback<PaymentMethodSearchItem> paymentMethodSearchItemSelectionCallback) {
            this.searchItemsShown = searchItems;
            this.itemSelectionCallback = paymentMethodSearchItemSelectionCallback;
        }

        @Override
        public void showError(MercadoPagoError mpException, String requestOrigin) {
            errorShown = mpException;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void startCardFlow(final Boolean automaticallySelection) {
            cardFlowStarted = true;
        }

        @Override
        public void startPaymentMethodsSelection(final PaymentPreference paymentPreference) {
            paymentMethodSelectionStarted = true;
        }

        @Override
        public void finishPaymentMethodSelection(PaymentMethod selectedPaymentMethod) {
            this.selectedPaymentMethod = selectedPaymentMethod;
        }

        @Override
        public void finishPaymentMethodSelection(PaymentMethod paymentMethod, Payer payer) {
            this.selectedPaymentMethod = paymentMethod;
            this.selectedPayer = payer;
        }

        @Override
        public void showAmount(@Nullable final Discount discount, @Nullable final Campaign campaign,
            final BigDecimal totalAmount,
            final Site site) {
            this.showedDiscountRow = true;
        }

        @Override
        public void startDiscountFlow(CheckoutPreference preference) {
            // Not tested yet.
        }

        @Override
        public void collectPayerInformation() {
            this.payerInformationStarted = true;
        }

        @Override
        public void cleanPaymentMethodOptions() {
            //Not yet tested
        }

        @Override
        public void showHook(Hook hook, int code) {
            //Not yet tested
        }

        @Override
        public void showDetailDialog(@NonNull final Discount discount, @NonNull final Campaign campaign) {
            //Do nothing
        }

        @Override
        public void showDetailDialog(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign) {
            //Do nothing
        }

        @Override
        public void showDiscountInputDialog() {
            //Do nothing
        }

        private void simulateItemSelection(int index) {
            itemSelectionCallback.onSelected(searchItemsShown.get(index));
        }

        private void simulateCustomItemSelection(int index) {
            customItemSelectionCallback.onSelected(customOptionsShown.get(index));
        }
    }
}
