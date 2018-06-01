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
import com.mercadopago.onetap.OneTapFragment;
import com.mercadopago.plugins.BusinessPaymentResultActivity;
import com.mercadopago.plugins.PaymentProcessorPluginActivity;
import com.mercadopago.plugins.model.BusinessPayment;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.PaymentPreference;
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
import com.mercadopago.viewmodel.CardPaymentModel;
import com.mercadopago.viewmodel.CheckoutStateModel;
import com.mercadopago.viewmodel.OneTapModel;
import com.mercadopago.views.CheckoutView;
import com.squareup.picasso.Picasso;
import java.math.BigDecimal;

import static com.mercadopago.plugins.model.ExitAction.EXTRA_CLIENT_RES_CODE;

public class CheckoutActivity extends MercadoPagoBaseActivity implements CheckoutView, OneTapFragment.CallBack {

    private static final int PLUGIN_PAYMENT_PROCESSOR_REQUEST_CODE = 200;
    private static final int BUSINESS_REQUEST_CODE = 400;

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
    private static final String EXTRA_PERSISTENT_DATA = "extra_persistent_data";
    private static final String EXTRA_PRIVATE_KEY = "extra_private_key";
    private static final String EXTRA_PUBLIC_KEY = "extra_public_key";

    private CheckoutPresenter checkoutPresenter;
    private String merchantPublicKey;
    private String privateKey;
    private Integer requestedResultCode;
    private Intent customDataBundle;

    public static Intent getIntent(@NonNull final Context context,
        final int resultCode,
        @NonNull final MercadoPagoCheckout mercadoPagoCheckout) {

        Intent checkoutIntent = new Intent(context, CheckoutActivity.class);
        checkoutIntent.putExtra(EXTRA_CHECKOUT_CONFIGURATION, mercadoPagoCheckout);
        checkoutIntent.putExtra(EXTRA_RESULT_CODE, resultCode);
        return checkoutIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.mpsdk_activity_checkout);
            initPresenter();
            checkoutPresenter.initialize();
        }
    }

    private void initPresenter() {
        CheckoutStateModel activityParameters = getActivityParameters();
        checkoutPresenter = new CheckoutPresenter(activityParameters);
        configurePresenter();
    }

    private void configurePresenter() {
        CheckoutProvider provider = new CheckoutProviderImpl(this,
            merchantPublicKey,
            privateKey, checkoutPresenter.getServicePreference(),
            checkoutPresenter.isESCEnabled());

        checkoutPresenter.attachResourcesProvider(provider);
        checkoutPresenter.attachView(this);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // To avoid reload from server the checkout preference.
        outState.putSerializable(EXTRA_PERSISTENT_DATA, checkoutPresenter.getState());
        outState.putString(EXTRA_PRIVATE_KEY, privateKey);
        outState.putString(EXTRA_PUBLIC_KEY, merchantPublicKey);
        outState.putInt(EXTRA_RESULT_CODE, requestedResultCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            checkoutPresenter =
                new CheckoutPresenter((CheckoutStateModel) savedInstanceState.getSerializable(EXTRA_PERSISTENT_DATA));
            privateKey = savedInstanceState.getString(EXTRA_PRIVATE_KEY);
            merchantPublicKey = savedInstanceState.getString(EXTRA_PUBLIC_KEY);
            requestedResultCode = savedInstanceState.getInt(EXTRA_RESULT_CODE);
            configurePresenter();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected CheckoutStateModel getActivityParameters() {
        Intent intent = getIntent();
        final MercadoPagoCheckout mercadoPagoCheckout =
            (MercadoPagoCheckout) intent.getSerializableExtra(EXTRA_CHECKOUT_CONFIGURATION);
        final CheckoutPreference checkoutPreference = mercadoPagoCheckout.getCheckoutPreference();
        requestedResultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        final CheckoutStateModel
            persistentData = CheckoutStateModel.from(requestedResultCode, mercadoPagoCheckout);
        privateKey =
            (checkoutPreference != null && checkoutPreference.getPayer() != null) ? checkoutPreference.getPayer()
                .getAccessToken() : "";
        merchantPublicKey = mercadoPagoCheckout.getMerchantPublicKey();
        return persistentData;
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
        BusinessPaymentResultActivity.start(this, model, merchantPublicKey, BUSINESS_REQUEST_CODE);
    }

    @Override
    public void showOneTap(@NonNull final OneTapModel oneTapModel) {
        OneTapFragment instance =
            OneTapFragment.getInstance(merchantPublicKey,
                privateKey,
                oneTapModel);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.one_tap_fragment, instance)
            .commit();
    }

    @Override
    public void hideProgress() {
        LayoutUtil.showRegularLayout(this);
    }

    @Override
    public void initializeMPTracker() {
        //Initialize tracker before creating a token
        MPTracker.getInstance()
            .initTracker(merchantPublicKey, checkoutPresenter.getCheckoutPreference().getSite().getId(),
                BuildConfig.VERSION_NAME, getApplicationContext());
    }

    @Override
    public void trackScreen() {
        final MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, merchantPublicKey)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case ErrorUtil.ERROR_REQUEST_CODE:
            resolveErrorRequest(resultCode, data);
            break;
        case MercadoPagoCheckout.TIMER_FINISHED_RESULT_CODE:
            checkoutPresenter.exitWithCode(resultCode);
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
            resolveBusinessResultActivity(data);
            break;
        default:
            break;
        }
    }

    private void resolveBusinessResultActivity(final Intent data) {
        int resCode = data != null ? data.getIntExtra(EXTRA_CLIENT_RES_CODE, RESULT_OK) : RESULT_OK;
        checkoutPresenter.exitWithCode(resCode);
    }

    private void resolveHook3(final int resultCode) {
        if (resultCode == RESULT_OK) {
            checkoutPresenter.resolvePaymentDataResponse();
        } else {
            backToReviewAndConfirm();
        }
        overrideTransitionIn();
    }

    private void resolveHook2(final int resultCode) {
        if (resultCode == RESULT_OK) {
            checkoutPresenter.hook2Continue();
        } else {
            checkoutPresenter.cancelCheckout();
        }
    }

    private void resolvePaymentProcessor(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            paymentResultOk(data);
        } else {
            cancelCheckout();
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
            checkoutPresenter.onBusinessResult(businessPayment);
        } else {
            final PaymentResult paymentResult = CheckoutStore.getInstance().getPaymentResult();
            checkoutPresenter.checkStartPaymentResultActivity(paymentResult);
        }
    }

    private void resolveReviewAndConfirmRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            checkoutPresenter.onPaymentConfirmation();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CHANGE_PAYMENT_METHOD) {
            checkoutPresenter.onChangePaymentMethodFromReviewAndConfirm();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CANCEL_PAYMENT) {
            resolveCancelReviewAndConfirm(data);
        } else if (resultCode == RESULT_CANCELED) {
            checkoutPresenter.onReviewAndConfirmCancel();
        }
    }

    private void resolveCancelReviewAndConfirm(final Intent data) {
        if (data != null && data.hasExtra(EXTRA_CLIENT_RES_CODE)) {
            Integer customResultCode = data.getIntExtra(EXTRA_CLIENT_RES_CODE, 0);
            checkoutPresenter.onCustomReviewAndConfirmResponse(customResultCode);
        } else {
            MercadoPagoError mercadoPagoError = null;
            if (data != null && data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) != null) {
                mercadoPagoError = JsonUtil.getInstance()
                    .fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            }
            if (mercadoPagoError == null) {
                checkoutPresenter.cancelCheckout();
            } else {
                checkoutPresenter.onReviewAndConfirmError(mercadoPagoError);
            }
        }
    }

    protected void resolveCardVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_DISCOUNT), Discount.class);
            Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ISSUER), Issuer.class);
            PayerCost payerCost =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER_COST), PayerCost.class);
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_TOKEN), Token.class);
            PaymentMethod paymentMethod =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD), PaymentMethod.class);

            checkoutPresenter.onCardFlowResponse(paymentMethod, issuer, payerCost, token, discount);
        } else {
            MercadoPagoError mercadoPagoError =
                (data == null || data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) == null) ? null :
                    JsonUtil.getInstance()
                        .fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            if (mercadoPagoError == null) {
                checkoutPresenter.onCardFlowCancel();
            } else {
                checkoutPresenter.onCardFlowError(mercadoPagoError);
            }
        }
    }

    private void resolvePaymentVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_DISCOUNT), Discount.class);
            Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ISSUER), Issuer.class);
            PayerCost payerCost =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER_COST), PayerCost.class);
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_TOKEN), Token.class);
            PaymentMethod paymentMethod =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD), PaymentMethod.class);
            Card card = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_CARD), Card.class);
            Payer payer = JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_PAYER), Payer.class);
            PaymentMethodSearch paymentMethodSearch =
                JsonUtil.getInstance()
                    .fromJson(data.getStringExtra(EXTRA_PAYMENT_METHOD_SEARCH), PaymentMethodSearch.class);
            if (paymentMethodSearch != null) {
                checkoutPresenter.onPaymentMethodSelected(paymentMethodSearch);
            }

            checkoutPresenter
                .onPaymentMethodSelectionResponse(paymentMethod, issuer, payerCost, token, discount, card, payer);
        } else if (isErrorResult(data)) {
            MercadoPagoError mercadoPagoError =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            checkoutPresenter.onPaymentMethodSelectionError(mercadoPagoError);
        } else {
            checkoutPresenter.onPaymentMethodSelectionCancel();
        }
    }

    private boolean isErrorResult(Intent data) {
        return data != null && !TextUtil.isEmpty(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR));
    }

    @Override
    public void showReviewAndConfirm() {
        overrideTransitionOut();
        overrideTransitionIn();
        new ReviewAndConfirmBuilder()
            .setMerchantPublicKey(merchantPublicKey)
            .setPreference(checkoutPresenter.getCheckoutPreference())
            .setDiscount(checkoutPresenter.getDiscount())
            .setPaymentMethod(checkoutPresenter.getSelectedPaymentMethod())
            .setPayerCost(checkoutPresenter.getSelectedPayerCost())
            .setToken(checkoutPresenter.getCreatedToken())
            .setIssuer(checkoutPresenter.getIssuer())
            .setHasExtraPaymentMethods(!checkoutPresenter.isUniquePaymentMethod())
            .setTermsAndConditionsEnabled(!isUserLogged())
            .startForResult(this);
    }

    @Override
    public void backToReviewAndConfirm() {
        showReviewAndConfirm();
        overrideTransitionOut();
    }

    @Override
    public void showPaymentMethodSelection() {
        if (isActive()) {
            new MercadoPagoComponents.Activities.PaymentVaultActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(merchantPublicKey)
                .setPayerAccessToken(privateKey)
                .setPayerEmail(checkoutPresenter.getCheckoutPreference().getPayer().getEmail())
                .setSite(checkoutPresenter.getCheckoutPreference().getSite())
                .setAmount(checkoutPresenter.getCheckoutPreference().getTotalAmount())
                .setPaymentPreference(checkoutPresenter.getCheckoutPreference().getPaymentPreference())
                .setPaymentMethodSearch(checkoutPresenter.getPaymentMethodSearch())
                .setDiscount(checkoutPresenter.getDiscount(), checkoutPresenter.getCampaign())
                .setInstallmentsEnabled(true)
                .setInstallmentsReviewEnabled(checkoutPresenter.isInstallmentsReviewScreenEnabled())
                .setShowBankDeals(checkoutPresenter.getShowBankDeals())
                .setMaxSavedCards(checkoutPresenter.getMaxSavedCardsToShow())
                .setShowAllSavedCardsEnabled(checkoutPresenter.shouldShowAllSavedCards())
                .setESCEnabled(checkoutPresenter.isESCEnabled())
                .setCheckoutPreference(checkoutPresenter.getCheckoutPreference())
                .startActivity();
        }
    }

    @Override
    public void showPaymentResult(PaymentResult paymentResult) {
        BigDecimal amount =
            checkoutPresenter.getCreatedPayment() == null ? checkoutPresenter.getCheckoutPreference().getTotalAmount()
                : checkoutPresenter.getCreatedPayment().getTransactionAmount();

        new MercadoPagoComponents.Activities.PaymentResultActivityBuilder()
            .setMerchantPublicKey(merchantPublicKey)
            .setPayerAccessToken(privateKey)
            .setActivity(this)
            .setPaymentResult(paymentResult)
            .setDiscount(checkoutPresenter.getDiscount())
            .setCongratsDisplay(checkoutPresenter.getCongratsDisplay())
            .setSite(checkoutPresenter.getCheckoutPreference().getSite())
            .setPaymentResultScreenPreference(checkoutPresenter.getPaymentResultScreenPreference())
            .setAmount(amount)
            .setServicePreference(checkoutPresenter.getServicePreference())
            .startActivity();

        overrideTransitionFadeInFadeOut();
    }

    private boolean isUserLogged() {
        return checkoutPresenter.getCheckoutPreference().getPayer() != null &&
            !TextUtil.isEmpty(checkoutPresenter.getCheckoutPreference().getPayer().getAccessToken());
    }

    private void resolvePaymentResultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            String nextAction = data.getStringExtra(EXTRA_NEXT_ACTION);
            checkoutPresenter.onPaymentResultCancel(nextAction);
        } else {
            if (data != null && data.hasExtra(EXTRA_RESULT_CODE)) {
                Integer finalResultCode = data.getIntExtra(EXTRA_RESULT_CODE, MercadoPagoCheckout.PAYMENT_RESULT_CODE);
                customDataBundle = data;
                checkoutPresenter.onCustomPaymentResultResponse(finalResultCode);
            } else {
                checkoutPresenter.onPaymentResultResponse();
            }
        }
    }

    @Override
    public void finishWithPaymentResult() {
        checkoutPresenter.exitWithCode(RESULT_OK);
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
        if (customDataBundle != null) {
            intent.putExtras(customDataBundle);
        }
        setResult(paymentResultCode, intent);
        finish();
    }

    @Override
    public void finishWithPaymentResult(Integer resultCode, Payment payment) {
        Intent intent = new Intent();
        if (customDataBundle != null) {
            intent.putExtras(customDataBundle);
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
        setResult(requestedResultCode, intent);
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
    public void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery) {
        PaymentPreference paymentPreference = checkoutPresenter.getCheckoutPreference().getPaymentPreference();

        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }

        paymentPreference.setDefaultPaymentTypeId(checkoutPresenter.getSelectedPaymentMethod().getPaymentTypeId());

        new MercadoPagoComponents.Activities.CardVaultActivityBuilder()
            .setMerchantPublicKey(merchantPublicKey)
            .setPayerAccessToken(privateKey)
            .setPaymentPreference(paymentPreference)
            .setAmount(checkoutPresenter.getCheckoutPreference().getTotalAmount())
            .setSite(checkoutPresenter.getCheckoutPreference().getSite())
            .setInstallmentsEnabled(true)
            .setAcceptedPaymentMethods(checkoutPresenter.getPaymentMethodSearch().getPaymentMethods())
            .setInstallmentsReviewEnabled(checkoutPresenter.isInstallmentsReviewScreenEnabled())
            .setDiscount(checkoutPresenter.getDiscount())
            .setPaymentRecovery(paymentRecovery)
            .setShowBankDeals(checkoutPresenter.getShowBankDeals())
            .setESCEnabled(checkoutPresenter.isESCEnabled())
            .setCard(checkoutPresenter.getSelectedCard())
            .startActivity(this, MercadoPagoComponents.Activities.CARD_VAULT_REQUEST_CODE);
        overrideTransitionIn();
    }

    @Override
    public void startPaymentMethodEdition() {
        showPaymentMethodSelection();
        overrideTransitionIn();
    }

    private void resolveErrorRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            checkoutPresenter.recoverFromFailure();
        } else {
            MercadoPagoError mercadoPagoError = data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR) == null ? null :
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_MERCADO_PAGO_ERROR), MercadoPagoError.class);
            checkoutPresenter.onErrorCancel(mercadoPagoError);
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
        ErrorUtil.startErrorActivity(this, error, merchantPublicKey);
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
        checkoutPresenter.cancelInitialization();
        checkoutPresenter.detachResourceProvider();
        checkoutPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean isActive() {
        return !isFinishing();
    }

    @Override
    public void onChangePaymentMethod() {
        checkoutPresenter.onChangePaymentMethod();
    }

    @Override
    public void onOneTapPay(@NonNull final PaymentMethod metadata) {
        checkoutPresenter.startPayment(metadata);
    }

    @Override
    public void exitCheckout(final int resCode) {
        setResult(resCode);
        finish();
    }

    @Override
    public void onOneTapPay(@NonNull final CardPaymentModel cardPaymentModel) {
        checkoutPresenter.startCardPayment(cardPaymentModel);
    }

    @Override
    public void onOneTapCanceled() {
        cancelCheckout();
    }

    @Override
    public void cancelCheckout() {
        overrideTransitionOut();
        setResult(RESULT_CANCELED);
        finish();
    }
}