package com.mercadopago;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.hooks.Hook;
import com.mercadopago.hooks.HookActivity;
import com.mercadopago.model.Card;
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
import com.mercadopago.plugins.BusinessPaymentResultActivity;
import com.mercadopago.plugins.PaymentProcessorPluginActivity;
import com.mercadopago.plugins.model.BusinessPayment;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import com.mercadopago.presenters.CheckoutPresenter;
import com.mercadopago.providers.CheckoutProvider;
import com.mercadopago.providers.CheckoutProviderImpl;
import com.mercadopago.review_and_confirm.ReviewAndConfirmActivity;
import com.mercadopago.review_and_confirm.ReviewAndConfirmBuilder;
import com.mercadopago.tracker.FlowHandler;
import com.mercadopago.tracker.MPTrackingContext;
import com.mercadopago.tracking.model.ActionEvent;
import com.mercadopago.tracking.tracker.MPTracker;
import com.mercadopago.tracking.utils.TrackingUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.TextUtil;
import com.mercadopago.views.CheckoutView;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import static com.mercadopago.plugins.model.ExitAction.EXTRA_CLIENT_RES_CODE;

public class CheckoutActivity extends MercadoPagoBaseActivity implements CheckoutView {


    private static final String RESULT_CODE_BUNDLE = "mRequestedResultCode";
    private static final String PRESENTER_BUNDLE = "mCheckoutPresenter";
    private static final int PLUGIN_PAYMENT_PROCESSOR_REQUEST_CODE = 200;
    private static final int BUSINESS_REQUEST_CODE = 400;

    private static final String EXTRA_MERCHANT_PUBLIC_KEY = "merchantPublicKey";
    private static final String EXTRA_MERCADO_PAGO_ERROR = "mercadoPagoError";
    private static final String EXTRA_DISCOUNT = "discount";
    private static final String EXTRA_ISSUER = "issuer";
    private static final String EXTRA_PAYER_COST = "payerCost";
    private static final String EXTRA_TOKEN = "token";
    private static final String EXTRA_PAYMENT_METHOD = "paymentMethod";
    private static final String EXTRA_PAYMENT_METHOD_SEARCH = "paymentMethodSearch";
    private static final String EXTRA_PAYMENT = "payment";
    private static final String EXTRA_PAYMENT_METHOD_CHANGED = "paymentMethodChanged";
    private static final String EXTRA_PAYMENT_DATA = "paymentData";
    private static final String EXTRA_NEXT_ACTION = "nextAction";
    private static final String EXTRA_RESULT_CODE = "resultCode";

    private static final String EXTRA_CARD = "card";
    private static final String EXTRA_PAYER = "payer";

    private static final String EXTRA_CHECKOUT_CONFIGURATION = "extra_mercadopago_checkout";

    public static Intent getIntent(@NonNull final Context context,
                                   final int resultCode,
                                   @NonNull final MercadoPagoCheckout mercadoPagoCheckout) {

        Intent checkoutIntent = new Intent(context, CheckoutActivity.class);
        checkoutIntent.putExtra(EXTRA_CHECKOUT_CONFIGURATION, mercadoPagoCheckout);
        checkoutIntent.putExtra(EXTRA_RESULT_CODE, resultCode);
        return checkoutIntent;
    }

    //Parameters
    protected String mMerchantPublicKey;
    protected String mPrivateKey;

    //Local vars
    protected CheckoutPresenter mCheckoutPresenter;

    protected Integer mRequestedResultCode;
    protected Intent mCustomDataBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mCheckoutPresenter = new CheckoutPresenter();
            getActivityParameters();
            configurePresenter();
            setContentView(R.layout.mpsdk_activity_checkout);
            mCheckoutPresenter.initialize();
        }
    }

    private void configurePresenter() {
        CheckoutProvider provider =
                new CheckoutProviderImpl(this, mMerchantPublicKey, mPrivateKey, mCheckoutPresenter.getServicePreference(),
                        mCheckoutPresenter.isESCEnabled());
        mCheckoutPresenter.attachResourcesProvider(provider);
        mCheckoutPresenter.attachView(this);
        mCheckoutPresenter.setIdempotencyKeySeed(mMerchantPublicKey);
    }

    protected void getActivityParameters() {

        Intent intent = getIntent();

        final MercadoPagoCheckout mercadoPagoCheckout = (MercadoPagoCheckout) intent.getSerializableExtra(EXTRA_CHECKOUT_CONFIGURATION);

        final PaymentResultScreenPreference paymentResultScreenPreference = mercadoPagoCheckout.getPaymentResultScreenPreference();
        final FlowPreference flowPreference = mercadoPagoCheckout.getFlowPreference();
        final boolean binaryMode = mercadoPagoCheckout.isBinaryMode();
        final Discount discount = mercadoPagoCheckout.getDiscount();
        final PaymentData paymentDataInput = mercadoPagoCheckout.getPaymentData();
        final PaymentResult paymentResultInput = mercadoPagoCheckout.getPaymentResult();
        final ServicePreference servicePreference = mercadoPagoCheckout.getServicePreference();
        final String preferenceId = mercadoPagoCheckout.getPreferenceId();

        mMerchantPublicKey = mercadoPagoCheckout.getMerchantPublicKey();
        mRequestedResultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        mPrivateKey = "";
        // the preference is set by id
        if (TextUtil.isEmpty(preferenceId)) {
            final CheckoutPreference checkoutPreference = mercadoPagoCheckout.getCheckoutPreference();
            mPrivateKey = (checkoutPreference != null && checkoutPreference.getPayer() != null) ? checkoutPreference.getPayer().getAccessToken() : "";
            mCheckoutPresenter.setCheckoutPreference(checkoutPreference);
        }

        mCheckoutPresenter.setCheckoutPreferenceId(preferenceId);
        mCheckoutPresenter.setPaymentResultScreenPreference(paymentResultScreenPreference);
        mCheckoutPresenter.setFlowPreference(flowPreference);
        mCheckoutPresenter.setBinaryMode(binaryMode);
        mCheckoutPresenter.setDiscount(discount);
        mCheckoutPresenter.setPaymentDataInput(paymentDataInput);
        mCheckoutPresenter.setPaymentResultInput(paymentResultInput);
        mCheckoutPresenter.setRequestedResult(mRequestedResultCode);
        mCheckoutPresenter.setServicePreference(servicePreference);
    }

    @Override
    public void fetchImageFromUrl(String url) {
        Picasso.with(this)
                .load(url)
                .fetch();
    }

    @Override
    public void showBusinessResult(final BusinessPaymentModel model) {
        overrideTransitionIn();
        BusinessPaymentResultActivity.start(this, model, mMerchantPublicKey, BUSINESS_REQUEST_CODE);
    }

    @Override
    public void initializeMPTracker() {
        //Initialize tracker before creating a token
        MPTracker.getInstance()
                .initTracker(mMerchantPublicKey, mCheckoutPresenter.getCheckoutPreference().getSite().getId(),
                        BuildConfig.VERSION_NAME, getApplicationContext());
    }

    @Override
    public void trackScreen() {
        final MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, mMerchantPublicKey)
                .setVersion(BuildConfig.VERSION_NAME)
                .build();
        final ActionEvent event = new ActionEvent.Builder()
                .setFlowId(FlowHandler.getInstance().getFlowId())
                .setAction(TrackingUtil.SCREEN_ID_CHECKOUT)
                .build();
        mpTrackingContext.clearExpiredTracks();
        mpTrackingContext.trackEvent(event);
    }

    @Override
    public void finishFromReviewAndConfirm() {
        setResult(MercadoPagoCheckout.PAYMENT_METHOD_CHANGED_REQUESTED);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PRESENTER_BUNDLE, JsonUtil.getInstance().toJson(mCheckoutPresenter));
        outState.putString(EXTRA_MERCHANT_PUBLIC_KEY, mMerchantPublicKey);
        outState.putInt(RESULT_CODE_BUNDLE, mRequestedResultCode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMerchantPublicKey = savedInstanceState.getString(EXTRA_MERCHANT_PUBLIC_KEY);
            mRequestedResultCode = savedInstanceState.getInt(RESULT_CODE_BUNDLE, 0);
            mCheckoutPresenter = JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(PRESENTER_BUNDLE), CheckoutPresenter.class);
            configurePresenter();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ErrorUtil.ERROR_REQUEST_CODE:
                resolveErrorRequest(resultCode, data);
                break;
            case MercadoPagoCheckout.TIMER_FINISHED_RESULT_CODE:
                resolveTimerObserverResult(resultCode);
                break;
            case MercadoPagoComponents.Activities.PAYMENT_VAULT_REQUEST_CODE:
                resolvePaymentVaultRequest(resultCode, data);
                break;
            case MercadoPagoComponents.Activities.PAYMENT_RESULT_REQUEST_CODE:
                resolvePaymentResultRequest(resultCode, data);
                break;
            case MercadoPagoComponents.Activities.CARD_VAULT_REQUEST_CODE:
                resolveCardVaultRequest(resultCode, data);
                break;
            case MercadoPagoComponents.Activities.REVIEW_AND_CONFIRM_REQUEST_CODE:
                resolveReviewAndConfirmRequest(resultCode, data);
                break;
            case MercadoPagoComponents.Activities.HOOK_2:
                resolveHook2(resultCode);
                break;
            case MercadoPagoComponents.Activities.HOOK_3:
                resolveHook3(resultCode);
                break;
            case PLUGIN_PAYMENT_PROCESSOR_REQUEST_CODE:
                resolvePaymentProcessor(resultCode, data);
                break;
            case BUSINESS_REQUEST_CODE:
                resolveBusinessResultActivity(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void resolveBusinessResultActivity(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(data.getIntExtra(EXTRA_CLIENT_RES_CODE, 0));
            finish();
        } else {
            cancelCheckout();
        }
    }

    private void resolveHook3(final int resultCode) {
        if (resultCode == RESULT_OK) {
            mCheckoutPresenter.resolvePaymentDataResponse();
        } else {
            backToReviewAndConfirm();
        }
        overrideTransitionIn();
    }

    private void resolveHook2(final int resultCode) {
        if (resultCode == RESULT_OK) {
            mCheckoutPresenter.hook2Continue();
        } else {
            overrideTransitionOut();
            cancelCheckout();
        }
    }

    private void resolvePaymentProcessor(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            paymentResultOk(data);
        } else {
            overrideTransitionOut();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Depending on intent data it triggers {@link com.mercadopago.paymentresult.PaymentResultActivity}
     * flow or {@link BusinessPaymentResultActivity}.
     *
     * @param data intent data that can contains a {@link BusinessPayment}
     */
    private void paymentResultOk(final Intent data) {

        if (PaymentProcessorPluginActivity.isBusiness(data)) {
            BusinessPayment businessPayment = PaymentProcessorPluginActivity.getBusinessPayment(data);
            mCheckoutPresenter.onBusinessResult(businessPayment);
        } else {
            final PaymentResult paymentResult = CheckoutStore.getInstance().getPaymentResult();
            mCheckoutPresenter.checkStartPaymentResultActivity(paymentResult);
        }
    }

    private void resolveTimerObserverResult(int resultCode) {
        setResult(resultCode);
        finish();
    }

    private void resolveReviewAndConfirmRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mCheckoutPresenter.onPaymentConfirmation();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CHANGE_PAYMENT_METHOD) {
            mCheckoutPresenter.changePaymentMethod();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CANCEL_PAYMENT) {
            resolveCancelReviewAndConfirm(data);
        } else if (resultCode == RESULT_CANCELED) {
            mCheckoutPresenter.onReviewAndConfirmCancel();
        }
    }

    private void resolveCancelReviewAndConfirm(final Intent data) {
        if (data != null && data.hasExtra(EXTRA_CLIENT_RES_CODE)) {
            Integer customResultCode = data.getIntExtra(EXTRA_CLIENT_RES_CODE, 0);
            mCheckoutPresenter.onCustomReviewAndConfirmResponse(customResultCode);
        } else {
            MercadoPagoError mercadoPagoError = null;
            if (data != null && data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) != null) {
                mercadoPagoError = JsonUtil.getInstance()
                        .fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            }
            if (mercadoPagoError == null) {
                mCheckoutPresenter.onReviewAndConfirmCancelPayment();
            } else {
                mCheckoutPresenter.onReviewAndConfirmError(mercadoPagoError);
            }
        }
    }

    protected void resolveCardVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_DISCOUNT), Discount.class);
            Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ISSUER), Issuer.class);
            PayerCost payerCost = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER_COST), PayerCost.class);
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_TOKEN), Token.class);
            PaymentMethod paymentMethod =
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD), PaymentMethod.class);

            mCheckoutPresenter.onCardFlowResponse(paymentMethod, issuer, payerCost, token, discount);
        } else {
            MercadoPagoError mercadoPagoError =
                    (data == null || data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) == null) ? null :
                            JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            if (mercadoPagoError == null) {
                mCheckoutPresenter.onCardFlowCancel();
            } else {
                mCheckoutPresenter.onCardFlowError(mercadoPagoError);
            }
        }
    }

    private void resolvePaymentVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_DISCOUNT), Discount.class);
            Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ISSUER), Issuer.class);
            PayerCost payerCost = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER_COST), PayerCost.class);
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_TOKEN), Token.class);
            PaymentMethod paymentMethod =
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD), PaymentMethod.class);
            Card card = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_CARD), Card.class);
            Payer payer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER), Payer.class);
            PaymentMethodSearch paymentMethodSearch =
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD_SEARCH), PaymentMethodSearch.class);
            if (paymentMethodSearch != null) {
                mCheckoutPresenter.setPaymentMethodSearch(paymentMethodSearch);
            }

            mCheckoutPresenter
                    .onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, discount, card, payer);
        } else if (isErrorResult(data)) {
            MercadoPagoError mercadoPagoError =
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            mCheckoutPresenter.onPaymentMethodSelectionError(mercadoPagoError);
        } else {
            mCheckoutPresenter.onPaymentMethodSelectionCancel();
        }
    }

    private boolean isErrorResult(Intent data) {
        return data != null && !TextUtil.isEmpty(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR));
    }

    @Override
    public void showReviewAndConfirm() {
        overrideTransitionIn();

        new ReviewAndConfirmBuilder()
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPreference(mCheckoutPresenter.getCheckoutPreference())
                .setDiscount(mCheckoutPresenter.getDiscount())
                .setPaymentMethod(mCheckoutPresenter.getSelectedPaymentMethod())
                .setPayerCost(mCheckoutPresenter.getSelectedPayerCost())
                .setToken(mCheckoutPresenter.getCreatedToken())
                .setIssuer(mCheckoutPresenter.getIssuer())
                .setHasExtraPaymentMethods(!mCheckoutPresenter.isUniquePaymentMethod())
                .setTermsAndConditionsEnabled(!isUserLogged())
                .startForResult(this);
    }

    @Override
    public void backToReviewAndConfirm() {
        showReviewAndConfirm();
        overrideTransitionOut();
    }

    @Override
    public void backToPaymentMethodSelection() {
        overrideTransitionOut();
        showPaymentMethodSelection();
    }

    @Override
    public void showPaymentMethodSelection() {
        if (isActive()) {
            new MercadoPagoComponents.Activities.PaymentVaultActivityBuilder()
                    .setActivity(this)
                    .setMerchantPublicKey(mMerchantPublicKey)
                    .setPayerAccessToken(mPrivateKey)
                    .setPayerEmail(mCheckoutPresenter.getCheckoutPreference().getPayer().getEmail())
                    .setSite(mCheckoutPresenter.getCheckoutPreference().getSite())
                    .setAmount(mCheckoutPresenter.getCheckoutPreference().getTotalAmount())
                    .setPaymentMethodSearch(mCheckoutPresenter.getPaymentMethodSearch())
                    .setDiscount(mCheckoutPresenter.getDiscount())
                    .setInstallmentsEnabled(true)
                    .setDiscountEnabled(mCheckoutPresenter.isDiscountEnabled())
                    .setInstallmentsReviewEnabled(mCheckoutPresenter.isInstallmentsReviewScreenEnabled())
                    .setPaymentPreference(mCheckoutPresenter.getCheckoutPreference().getPaymentPreference())
                    .setShowBankDeals(mCheckoutPresenter.getShowBankDeals())
                    .setMaxSavedCards(mCheckoutPresenter.getMaxSavedCardsToShow())
                    .setShowAllSavedCardsEnabled(mCheckoutPresenter.shouldShowAllSavedCards())
                    .setESCEnabled(mCheckoutPresenter.isESCEnabled())
                    .setCheckoutPreference(mCheckoutPresenter.getCheckoutPreference())
                    .startActivity();
        }
    }

    @Override
    public void showPaymentResult(PaymentResult paymentResult) {
        BigDecimal amount =
                mCheckoutPresenter.getCreatedPayment() == null ? mCheckoutPresenter.getCheckoutPreference().getTotalAmount()
                        : mCheckoutPresenter.getCreatedPayment().getTransactionAmount();

        new MercadoPagoComponents.Activities.PaymentResultActivityBuilder()
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPayerAccessToken(mPrivateKey)
                .setActivity(this)
                .setPaymentResult(paymentResult)
                .setDiscount(mCheckoutPresenter.getDiscount())
                .setDiscountEnabled(mCheckoutPresenter.isDiscountEnabled())
                .setCongratsDisplay(mCheckoutPresenter.getCongratsDisplay())
                .setSite(mCheckoutPresenter.getCheckoutPreference().getSite())
                .setPaymentResultScreenPreference(mCheckoutPresenter.getPaymentResultScreenPreference())
                .setAmount(amount)
                .setServicePreference(mCheckoutPresenter.getServicePreference())
                .startActivity();

        overrideTransitionFadeInFadeOut();
    }

    private boolean isUserLogged() {
        return mCheckoutPresenter.getCheckoutPreference().getPayer() != null &&
                !TextUtil.isEmpty(mCheckoutPresenter.getCheckoutPreference().getPayer().getAccessToken());
    }

    private void resolvePaymentResultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            String nextAction = data.getStringExtra(EXTRA_NEXT_ACTION);
            mCheckoutPresenter.onPaymentResultCancel(nextAction);
        } else {
            if (data != null && data.hasExtra(EXTRA_RESULT_CODE)) {
                Integer finalResultCode = data.getIntExtra(EXTRA_RESULT_CODE, MercadoPagoCheckout.PAYMENT_RESULT_CODE);
                mCustomDataBundle = data;
                mCheckoutPresenter.onCustomPaymentResultResponse(finalResultCode);
            } else {
                mCheckoutPresenter.onPaymentResultResponse();
            }
        }
    }

    @Override
    public void finishWithPaymentResult() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void finishWithPaymentResult(Payment payment) {
        Intent data = new Intent();
        data.putExtra(EXTRA_PAYMENT, JsonUtil.getInstance().toJson(payment));
        setResult(MercadoPagoCheckout.PAYMENT_RESULT_CODE, data);
        finish();
    }

    @Override
    public void finishWithPaymentResult(Integer paymentResultCode) {
        Intent intent = new Intent();
        if (mCustomDataBundle != null) {
            intent.putExtras(mCustomDataBundle);
        }
        setResult(paymentResultCode, intent);
        finish();
    }

    @Override
    public void finishWithPaymentResult(Integer resultCode, Payment payment) {
        Intent intent = new Intent();
        if (mCustomDataBundle != null) {
            intent.putExtras(mCustomDataBundle);
        }
        intent.putExtra(EXTRA_PAYMENT, JsonUtil.getInstance().toJson(payment));
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void finishWithPaymentDataResult(PaymentData paymentData, Boolean paymentMethodEdited) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PAYMENT_METHOD_CHANGED, paymentMethodEdited);
        intent.putExtra(EXTRA_PAYMENT_DATA, JsonUtil.getInstance().toJson(paymentData));
        setResult(mRequestedResultCode, intent);
        finish();
    }

    @Override
    public void cancelCheckout(MercadoPagoError mercadoPagoError) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_MERCADO_PAGO_ERROR, JsonUtil.getInstance().toJson(mercadoPagoError));
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void cancelCheckout() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery) {
        PaymentPreference paymentPreference = mCheckoutPresenter.getCheckoutPreference().getPaymentPreference();

        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }

        paymentPreference.setDefaultPaymentTypeId(mCheckoutPresenter.getSelectedPaymentMethod().getPaymentTypeId());

        new MercadoPagoComponents.Activities.CardVaultActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPayerAccessToken(mPrivateKey)
                .setPaymentPreference(paymentPreference)
                .setAmount(mCheckoutPresenter.getCheckoutPreference().getTotalAmount())
                .setSite(mCheckoutPresenter.getCheckoutPreference().getSite())
                .setInstallmentsEnabled(true)
                .setAcceptedPaymentMethods(mCheckoutPresenter.getPaymentMethodSearch().getPaymentMethods())
                .setInstallmentsReviewEnabled(mCheckoutPresenter.isInstallmentsReviewScreenEnabled())
                .setDiscount(mCheckoutPresenter.getDiscount())
                .setDiscountEnabled(mCheckoutPresenter.isDiscountEnabled())
                .setPaymentRecovery(paymentRecovery)
                .setShowBankDeals(mCheckoutPresenter.getShowBankDeals())
                .setESCEnabled(mCheckoutPresenter.isESCEnabled())
                .setCard(mCheckoutPresenter.getSelectedCard())
                .startActivity();

        overrideTransitionIn();
    }

    @Override
    public void startPaymentMethodEdition() {
        showPaymentMethodSelection();
        overrideTransitionIn();
    }

    private void resolveErrorRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mCheckoutPresenter.recoverFromFailure();
        } else {
            MercadoPagoError mercadoPagoError = data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) == null ? null :
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            mCheckoutPresenter.onErrorCancel(mercadoPagoError);
        }
    }

    @Override
    public void cancelCheckout(Integer resultCode, Boolean paymentMethodEdited) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PAYMENT_METHOD_CHANGED, paymentMethodEdited);
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void showError(MercadoPagoError error) {
        ErrorUtil.startErrorActivity(this, error, mMerchantPublicKey);
    }

    @Override
    public void showProgress() {
        LayoutUtil.showProgressLayout(this);
    }

    @Override
    public void showHook(@NonNull final Hook hook, final int requestCode) {
        startActivityForResult(HookActivity.getIntent(this, hook), requestCode);
        overrideTransitionIn();
    }

    @Override
    public void showPaymentProcessor() {
        startActivityForResult(PaymentProcessorPluginActivity.getIntent(this), PLUGIN_PAYMENT_PROCESSOR_REQUEST_CODE);
        overrideTransitionFadeInFadeOut();
    }

    @Override
    protected void onDestroy() {
        mCheckoutPresenter.cancelInitialization();
        mCheckoutPresenter.detachResourceProvider();
        mCheckoutPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean isActive() {
        return !isFinishing();
    }
}