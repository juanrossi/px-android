package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandlerWrapper;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.EscManager;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.CardMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodMapper;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;

import static com.mercadopago.android.px.internal.util.TextUtil.isEmpty;

public class PaymentService implements PaymentRepository {

    @NonNull private final UserSelectionRepository userSelectionRepository;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final PluginRepository pluginRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final PaymentProcessor paymentProcessor;
    @NonNull private final Context context;
    @NonNull private final TokenRepository tokenRepository;
    @NonNull private final PaymentMethodMapper paymentMethodMapper;
    @NonNull private final CardMapper cardMapper;
    @NonNull private final EscManager escManager;

    @NonNull private final PaymentServiceHandlerWrapper handlerWrapper;

    @Nullable private IPayment payment;

    public PaymentService(@NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final PluginRepository pluginRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final PaymentProcessor paymentProcessor,
        @NonNull final Context context,
        @NonNull final EscManager escManager,
        @NonNull final TokenRepository tokenRepository,
        @NonNull final InstructionsRepository instructionsRepository) {

        this.escManager = escManager;
        this.userSelectionRepository = userSelectionRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.pluginRepository = pluginRepository;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.paymentProcessor = paymentProcessor;
        this.context = context;
        this.tokenRepository = tokenRepository;
        paymentMethodMapper = new PaymentMethodMapper();
        cardMapper = new CardMapper();
        handlerWrapper = new PaymentServiceHandlerWrapper(this, escManager, instructionsRepository);
    }

    @Override
    public void attach(@NonNull final PaymentServiceHandler handler) {
        handlerWrapper.setHandler(handler);
        handlerWrapper.processMessages();
    }

    @Override
    public void detach(@NonNull final PaymentServiceHandler handler) {
        handlerWrapper.detach(handler);
    }

    @Override
    public void storePayment(@NonNull final IPayment iPayment) {
        payment = iPayment;
    }

    @Nullable
    @Override
    public IPayment getPayment() {
        return payment;
    }

    @Override
    public boolean hasPayment() {
        return payment != null;
    }

    @NonNull
    @Override
    public PaymentRecovery createRecoveryForInvalidESC() {
        return new PaymentRecovery(paymentSettingRepository.getToken(), userSelectionRepository.getPaymentMethod(),
            userSelectionRepository.getPayerCost(), userSelectionRepository.getIssuer(),
            Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC);
    }

    @NonNull
    @Override
    public PaymentRecovery createPaymentRecovery() {
        return new PaymentRecovery(paymentSettingRepository.getToken(),
            userSelectionRepository.getPaymentMethod(),
            userSelectionRepository.getPayerCost(),
            userSelectionRepository.getIssuer(), getPayment().getPaymentStatus(),
            getPayment().getPaymentStatusDetail());
    }

    /**
     * This method presets all user information ahead before the payment is processed.
     *
     * @param oneTapModel onetap information.
     */
    @Override
    public void startOneTapPayment(@NonNull final OneTapModel oneTapModel) {
        final OneTapMetadata oneTapMetadata = oneTapModel.getPaymentMethods().getOneTapMetadata();
        final String paymentTypeId = oneTapMetadata.getPaymentTypeId();
        final String paymentMethodId = oneTapMetadata.getPaymentMethodId();

        if (PaymentTypes.isCardPaymentType(paymentTypeId)) {
            // Saved card.
            final Card card = cardMapper.map(oneTapModel);
            userSelectionRepository.select(card);
            userSelectionRepository.select(oneTapMetadata.getCard().getAutoSelectedInstallment());
        } else if (PaymentTypes.isPlugin(paymentTypeId)) {
            // Account money plugin / No second factor.
            userSelectionRepository.select(pluginRepository.getPluginAsPaymentMethod(paymentMethodId, paymentTypeId));
        } else {
            // Other - not implemented
            userSelectionRepository.select(paymentMethodMapper.map(oneTapModel.getPaymentMethods()));
        }

        startPayment();
    }

    @Override
    public void startPayment() {
        //Wrapping the callback is important to assure ESC handling.
        checkPaymentMethod();
    }

    private void checkPaymentMethod() {
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        if (paymentMethod != null) {
            processPaymentMethod(paymentMethod);
        } else {
            handlerWrapper.onPaymentError(getError());
        }
    }

    private void processPaymentMethod(final PaymentMethod paymentMethod) {
        if (PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId())) {
            payWithCard();
        } else {
            pay();
        }
    }

    private void payWithCard() {
        if (hasValidSavedCardInfo()) {
            if (paymentSettingRepository.hasToken()) { // Paying with saved card with token
                pay();
            } else { // Token does not exists - must generate one or ask for CVV.
                checkEscAvailability();
            }
        } else if (hasValidNewCardInfo()) { // New card payment
            pay();
        } else { // Guessing card could not tokenize or obtain card information.
            handlerWrapper.onPaymentError(getError());
        }
    }

    private void checkEscAvailability() {
        //Paying with saved card without token
        final Card card = userSelectionRepository.getCard();

        if (escManager.hasEsc(card)) {
            //Saved card has ESC - Try to tokenize
            tokenRepository.createToken(card).enqueue(new Callback<Token>() {
                @Override
                public void success(final Token token) {
                    checkPaymentMethod();
                }

                @Override
                public void failure(final ApiException apiException) {
                    //Start CVV screen if fail
                    handlerWrapper.onCvvRequired(card);
                }
            });
        } else {
            //Saved card has no ESC saved - CVV is requiered.
            handlerWrapper.onCvvRequired(card);
        }
    }

    private boolean hasValidSavedCardInfo() {
        return userSelectionRepository.hasCardSelected()
            && userSelectionRepository.getPayerCost() != null;
    }

    private boolean hasValidNewCardInfo() {
        return userSelectionRepository.getPaymentMethod() != null
            && userSelectionRepository.getIssuer() != null
            && userSelectionRepository.getPayerCost() != null
            && paymentSettingRepository.hasToken();
    }

    private void pay() {
        if (paymentProcessor.shouldShowFragmentOnPayment()) {
            handlerWrapper.onVisualPayment();
        } else {
            final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
            final PaymentProcessor.CheckoutData checkoutData =
                new PaymentProcessor.CheckoutData(getPaymentData(), checkoutPreference);
            paymentProcessor.startPayment(checkoutData, context, handlerWrapper);
        }
    }

    @Override
    public boolean isExplodingAnimationCompatible() {
        return !paymentProcessor.shouldShowFragmentOnPayment();
    }

    /**
     * Payment data is a dynamic non-mutable object that represents
     * the payment state of the checkout exp.
     *
     * @return payment data at the moment is called.
     */
    @NonNull
    @Override
    public PaymentData getPaymentData() {
        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(userSelectionRepository.getPaymentMethod());
        paymentData.setPayerCost(userSelectionRepository.getPayerCost());
        paymentData.setIssuer(userSelectionRepository.getIssuer());
        paymentData.setToken(paymentSettingRepository.getToken());
        paymentData.setCampaign(discountRepository.getCampaign());
        paymentData.setDiscount(discountRepository.getDiscount());
        paymentData
            .setCouponCode(isEmpty(discountRepository.getDiscountCode()) ? null : discountRepository.getDiscountCode());
        paymentData.setTransactionAmount(amountRepository.getAmountToPay());
        //se agrego payer info a la pref - BOLBRADESCO
        paymentData.setPayer(paymentSettingRepository.getCheckoutPreference().getPayer());
        return paymentData;
    }

    @NonNull
    @Override
    public PaymentResult createPaymentResult(
        @NonNull final IPayment payment) {
        return new PaymentResult.Builder()
            .setPaymentData(getPaymentData())
            .setPaymentId(payment.getId())
            .setPaymentStatus(payment.getPaymentStatus())
            .setStatementDescription(payment.getStatementDescription())
            .setPaymentStatusDetail(payment.getPaymentStatusDetail())
            .build();
    }

    @Override
    public int getPaymentTimeout() {
        return paymentProcessor.getPaymentTimeout();
    }

    public MercadoPagoError getError() {
        return new MercadoPagoError("Something went wrong", false);
    }
}
