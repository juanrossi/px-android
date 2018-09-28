package com.mercadopago.android.px.guessing;

import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.datasource.CardAssociationService;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivityView;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardStoragePresenter;
import com.mercadopago.android.px.internal.features.providers.GuessingCardProvider;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.mocks.Cards;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "PMD.ExcessiveClassLength", "unchecked" })
@RunWith(MockitoJUnitRunner.class)
public class GuessingCardStoragePresenterTest {

    private static final String DUMMY_ACCESS_TOKEN =
        "TEST-8331125484710872-100611-d91fdace2a0a6e42293927d3289b53c3__LC_LB__-99037920";
    final List<PaymentMethod> cardPaymentMethodListMLA = PaymentMethods.getCardPaymentMethodListMLA();
    private GuessingCardStoragePresenter presenter;
    @Mock private CardPaymentMethodRepository cardPaymentMethodRepository;
    @Mock private CardAssociationService cardAssociationService;
    @Mock private MercadoPagoESC mercadoPagoESC;
    @Mock private GuessingCardActivityView guessingCardActivityView;
    @Mock private GuessingCardProvider guessingCardProvider;

    @Before
    public void setUp() {
        presenter =
            new GuessingCardStoragePresenter(DUMMY_ACCESS_TOKEN, cardPaymentMethodRepository, cardAssociationService,
                mercadoPagoESC);

        presenter.attachView(guessingCardActivityView);
        presenter.attachResourcesProvider(guessingCardProvider);
    }

    @Test
    public void presenterInitializedShouldInitializeView() {
        initializePresenterWithValidCardPaymentMethods();

        verify(guessingCardActivityView).onValidStart();
        //We shouldn't be showing bank deals
        verify(guessingCardActivityView).hideBankDeals();
    }

    @Test
    public void ifCardPaymentMethodListSetIsEmptyThenShowError() {
        final List<PaymentMethod> emptyList = new ArrayList<>();
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(emptyList));

        presenter.initialize();
        //TODO check for failure call
    }

    @Test
    public void ifCardPaymentListNotEmptyStartGuessingForm() {
        initializePresenterWithValidCardPaymentMethods();

        verify(guessingCardActivityView).initializeTitle();
        verify(guessingCardActivityView).setSecurityCodeListeners();
        verify(guessingCardActivityView).setIdentificationTypeListeners();
        verify(guessingCardActivityView).setIdentificationNumberListeners();
    }

    @Test
    public void ifPaymentMethodSetAndDeletedThenClearConfiguration() {

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
    public void ifPaymentMethodSetHasIdentificationTypeRequiredThenShowIdentificationView() {

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
    public void ifPaymentMethodSetDoNotHaveIdentificationTypeRequiredThenHideIdentificationView() {

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
