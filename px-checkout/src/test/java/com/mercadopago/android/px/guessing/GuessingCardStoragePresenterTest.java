package com.mercadopago.android.px.guessing;

import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.datasource.CardAssociationService;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivityView;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardStoragePresenter;
import com.mercadopago.android.px.internal.features.providers.GuessingCardProvider;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.internal.services.CardService;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.mocks.Cards;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.internal.matchers.Null;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.CREATE_TOKEN;
import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "PMD.ExcessiveClassLength", "unchecked" })
@RunWith(MockitoJUnitRunner.class)
public class GuessingCardStoragePresenterTest {

    private static final String DUMMY_ACCESS_TOKEN =
        "TEST-8331125484710872-100611-d91fdace2a0a6e42293927d3289b53c3__LC_LB__-99037920";
    private static final String DUMMY_TOKEN_ID = "123";
    private static final String DUMMY_CARD_ID = "1234567890";
    private static final String DUMMY_TOKEN_ESC = "ABCDEFGH";
    final List<PaymentMethod> cardPaymentMethodListMLA = PaymentMethods.getCardPaymentMethodListMLA();
    private GuessingCardStoragePresenter presenter;
    @Mock private CardPaymentMethodRepository cardPaymentMethodRepository;
    @Mock private MercadoPagoESC mercadoPagoESC;
    @Mock private GatewayService gatewayService;
    @Mock private GuessingCardActivityView guessingCardActivityView;
    @Mock private GuessingCardProvider guessingCardProvider;
    @Mock private CardService cardService;
    @Mock private final CardAssociationService cardAssociationService = new CardAssociationService(cardService);

    @Before
    public void setUp() {
        presenter =
            new GuessingCardStoragePresenter(DUMMY_ACCESS_TOKEN, cardPaymentMethodRepository, cardAssociationService,
                mercadoPagoESC, gatewayService);

        presenter.attachView(guessingCardActivityView);
        presenter.attachResourcesProvider(guessingCardProvider);
    }

    @Test
    public void whenPresenterInitializedCorrectlyThenInitializeView() {
        initializePresenterWithValidCardPaymentMethods();

        verify(guessingCardActivityView).onValidStart();
        //We shouldn't be showing bank deals
        verify(guessingCardActivityView).hideBankDeals();
    }

    @Test
    public void cardPaymentMethodListSetIsEmptyThenShowError() {
        final List<PaymentMethod> emptyList = Collections.emptyList();
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(emptyList));

        presenter.initialize();
        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentMethodListSetIsNullThenShowError() {
        final List<PaymentMethod> nullList = null;
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(nullList));

        presenter.initialize();
        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentMethodCallFailsThenShowError() {
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubFailMpCall<List<PaymentMethod>>(PaymentMethods.getDoNotFindPaymentMethodsException()));

        presenter.initialize();
        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentListIsNotEmptyThenStartGuessingForm() {
        initializePresenterWithValidCardPaymentMethods();

        verify(guessingCardActivityView).initializeTitle();
        verify(guessingCardActivityView).setSecurityCodeListeners();
        verify(guessingCardActivityView).setIdentificationTypeListeners();
        verify(guessingCardActivityView).setIdentificationNumberListeners();
    }

    @Test
    public void whenPaymentMethodIsSetAndDeletedThenClearConfiguration() {

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> selected = Collections.singletonList(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(selected, Cards.MOCKED_BIN_VISA);

        verify(guessingCardProvider).getIdentificationTypesAsync(anyString(), any(TaggedCallback.class));
        verify(guessingCardActivityView).resolvePaymentMethodSet(selected.get(0));

        presenter.resolvePaymentMethodCleared();

        assertEquals(Card.CARD_DEFAULT_SECURITY_CODE_LENGTH, presenter.getSecurityCodeLength());
        assertEquals(CardView.CARD_SIDE_BACK, presenter.getSecurityCodeLocation());
        assertTrue(presenter.isSecurityCodeRequired());
        assertEquals(0, presenter.getSavedBin().length());

        verify(guessingCardActivityView).clearErrorView();
        verify(guessingCardActivityView).clearCardNumberInputLength();
        verify(guessingCardActivityView).clearSecurityCodeEditText();
        verify(guessingCardActivityView).checkClearCardView();
    }

    @Test
    public void whenPaymentMethodSetHasIdentificationTypeRequiredThenShowIdentificationView() {

        final List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        mockIdentificationTypesCall(identificationTypesList, true);

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        assertTrue(presenter.isIdentificationNumberRequired());
        verify(guessingCardActivityView).resolvePaymentMethodSet(mockedGuessedPaymentMethods.get(0));
        verify(guessingCardActivityView).initializeIdentificationTypes(identificationTypesList);
    }

    @Test
    public void whenPaymentMethodSetDoNotHaveIdentificationTypeRequiredThenHideIdentificationView() {

        final List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        mockIdentificationTypesCall(identificationTypesList, true);

        final List<PaymentMethod> idNotRequiredPaymentMethods = new ArrayList<>();
        idNotRequiredPaymentMethods.add(PaymentMethods.getPaymentMethodWithIdNotRequired());

        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString()))
            .thenReturn(new StubSuccessMpCall<>(idNotRequiredPaymentMethods));
        presenter.initialize();

        presenter.resolvePaymentMethodListSet(idNotRequiredPaymentMethods, Cards.MOCKED_BIN_CORDIAL);

        verify(guessingCardActivityView).resolvePaymentMethodSet(idNotRequiredPaymentMethods.get(0));

        assertFalse(presenter.isIdentificationNumberRequired());
        verify(guessingCardActivityView, times(0)).initializeIdentificationTypes(any(List.class));
        verify(guessingCardActivityView).hideIdentificationInput();
    }

    @Test
    public void whenIdentificationTypesListSetIsEmptyThenShowError() {
        final List<IdentificationType> identificationTypesList = Collections.emptyList();
        mockIdentificationTypesCall(identificationTypesList, true);

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenIdentificationTypesCallFailsThenThenShowError() {
        final List<IdentificationType> identificationTypesList = IdentificationTypes.getIdentificationTypes();
        mockIdentificationTypesCall(identificationTypesList, false);
        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenIsNullThenShowError() {

        initializePresenterWithValidCardPaymentMethods();

        final Token token = null;

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenCallFailsThenShowError() {

        initializePresenterWithValidCardPaymentMethods();

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubFailMpCall<Token>(new ApiException()));

        presenter.createToken();

        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenNotNullThenCallAssociateCard() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId()))
            .thenReturn(new StubSuccessMpCall<>(new Card()));

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(cardAssociationService)
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId());
    }

    @Test
    public void whenAssociateCardFailsShowError() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId()))
            .thenReturn(new StubFailMpCall<Card>(new ApiException()));

        presenter.createToken();

        verify(guessingCardActivityView).finishCardStorageFlowWithError(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenAssociateCardThenSaveEscAndFinishWithSuccess() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);
        token.setCardId(DUMMY_CARD_ID);
        token.setEsc(DUMMY_TOKEN_ESC);

        final Card stubCard = new Card();

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId()))
            .thenReturn(new StubSuccessMpCall<>(stubCard));

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(mercadoPagoESC).saveESC(DUMMY_CARD_ID, DUMMY_TOKEN_ESC);
        verify(guessingCardActivityView).finishCardStorageFlowWithSuccess();
    }

    @Test
    public void whenSaveEscFailsThenFinishWithSuccessAnyway() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        final Card stubCard = new Card();
        stubCard.setId(DUMMY_CARD_ID);
        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId()))
            .thenReturn(new StubSuccessMpCall<>(stubCard));

        when(gatewayService.createToken((String) isNull(), eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubFailMpCall<Token>(new ApiException()));

        presenter.createToken();

        // Do not save esc, since the tokenization failed
        verify(mercadoPagoESC, never()).saveESC(DUMMY_CARD_ID, DUMMY_TOKEN_ESC);
        verify(guessingCardActivityView).finishCardStorageFlowWithSuccess();
    }

    // --------- Helper methods ----------- //

    private void initializePresenterWithValidCardPaymentMethods() {
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString()))
            .thenReturn(new StubSuccessMpCall<>(cardPaymentMethodListMLA));
        presenter.initialize();
    }

    private void mockIdentificationTypesCall(final List<IdentificationType> identificationTypes,
        final boolean success) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) {
                final TaggedCallback callback = (TaggedCallback) invocation.getArguments()[1];
                if (success) {
                    callback.onSuccess(identificationTypes);
                } else {
                    final MercadoPagoError error =
                        new MercadoPagoError(IdentificationTypes.getDoNotFindIdentificationTypesException(),
                            GET_IDENTIFICATION_TYPES);
                    callback.onFailure(error);
                }
                return null;
            }
        }).when(guessingCardProvider).getIdentificationTypesAsync(anyString(), any(TaggedCallback.class));
    }
}
