package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.reflect.TypeToken;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.presenters.CardVaultPresenter;
import com.mercadopago.providers.CardVaultProviderImpl;
import com.mercadopago.tracking.utils.TrackingUtil;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.views.CardVaultView;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

public class CardVaultActivity extends AppCompatActivity implements CardVaultView {

    protected CardVaultPresenter presenter;
    protected boolean activityActive;

    //Parameters
    protected String publicKey;
    protected String privateKey;

    //View controls
    private Boolean showBankDeals;
    private Boolean escEnabled;

    public static final String EXTRA_DISCOUNT = "discount";
    public static final String EXTRA_CAMPAIGN = "campaign";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOrientation();
        activityActive = true;
        setContentView();

        presenter = new CardVaultPresenter();
        presenter.attachView(this);

        if (savedInstanceState == null) {
            initializeCardFlow();
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        presenter.detachResourceProvider();
        super.onDestroy();
    }

    private void initializeCardFlow() {
        getActivityParameters();
        configurePresenter();
        initialize();
    }

    private void configurePresenter() {
        presenter
            .attachResourcesProvider(new CardVaultProviderImpl(this, publicKey, privateKey, escEnabled));
    }

    private void setScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public void restoreInstanceState(Bundle savedInstanceState) {

        publicKey = savedInstanceState.getString("merchantPublicKey");
        privateKey = savedInstanceState.getString("payerAccessToken");
        BigDecimal amountValue = null;
        String amount = savedInstanceState.getString("amount");
        if (amount != null) {
            amountValue = new BigDecimal(amount);
        }
        presenter.setAmount(amountValue);

        List<PaymentMethod> paymentMethods;
        try {
            Type listType = new TypeToken<List<PaymentMethod>>() {
            }.getType();
            paymentMethods =
                JsonUtil.getInstance().getGson().fromJson(savedInstanceState.getString("paymentMethodList"), listType);
        } catch (Exception ex) {
            paymentMethods = null;
        }

        List<PayerCost> payerCosts;
        try {
            Type listType = new TypeToken<List<Card>>() {
            }.getType();
            payerCosts =
                JsonUtil.getInstance().getGson().fromJson(savedInstanceState.getString("payerCostsList"), listType);
        } catch (Exception ex) {
            payerCosts = null;
        }

        presenter.setPaymentMethodList(paymentMethods);
        presenter.setPayerCostsList(payerCosts);
        PaymentPreference paymentPreference =
            JsonUtil.getInstance().fromJson(savedInstanceState.getString("paymentPreference"), PaymentPreference.class);
        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }

        presenter.setPaymentPreference(paymentPreference);
        presenter.setPaymentRecovery(
            JsonUtil.getInstance().fromJson(savedInstanceState.getString("paymentRecovery"), PaymentRecovery.class));
        presenter.setCard(JsonUtil.getInstance().fromJson(savedInstanceState.getString("card"), Card.class));

        presenter.setSite(JsonUtil.getInstance().fromJson(savedInstanceState.getString("site"), Site.class));
        presenter.setPaymentMethod(
            JsonUtil.getInstance().fromJson(savedInstanceState.getString("paymentMethod"), PaymentMethod.class));
        presenter
            .setIssuer(JsonUtil.getInstance().fromJson(savedInstanceState.getString("issuer"), Issuer.class));
        presenter
            .setPayerCost(JsonUtil.getInstance().fromJson(savedInstanceState.getString("payerCost"), PayerCost.class));
        presenter
            .setToken(JsonUtil.getInstance().fromJson(savedInstanceState.getString("token"), Token.class));
        presenter
            .setCardInfo(JsonUtil.getInstance().fromJson(savedInstanceState.getString("cardInfo"), CardInfo.class));
        presenter.setInstallmentsEnabled(savedInstanceState.getBoolean("installmentsEnabled", false));
        presenter
            .setInstallmentsReviewEnabled(savedInstanceState.getBoolean("installmentsReviewEnabled", false));
        presenter.setInstallmentsListShown(savedInstanceState.getBoolean("installmentsListShown", false));
        presenter.setIssuersListShown(savedInstanceState.getBoolean("issuersListShown", false));
        escEnabled = savedInstanceState.getBoolean("escEnabled", false);

        showBankDeals = savedInstanceState.getBoolean("showBankDeals", true);

        configurePresenter();
    }

    private void getActivityParameters() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        Boolean installmentsEnabled = intent.getBooleanExtra("installmentsEnabled", true);
        Boolean installmentsReviewEnabled = intent.getBooleanExtra("installmentsReviewEnabled", true);
        escEnabled = intent.getBooleanExtra("escEnabled", false);

        publicKey = intent.getStringExtra("merchantPublicKey");
        privateKey = intent.getStringExtra("payerAccessToken");

        PaymentPreference paymentPreference =
            JsonUtil.getInstance().fromJson(intent.getStringExtra("paymentPreference"), PaymentPreference.class);

        Site site = JsonUtil.getInstance().fromJson(intent.getStringExtra("site"), Site.class);
        Card card = JsonUtil.getInstance().fromJson(intent.getStringExtra("card"), Card.class);
        PaymentRecovery paymentRecovery =
            JsonUtil.getInstance().fromJson(intent.getStringExtra("paymentRecovery"), PaymentRecovery.class);
        BigDecimal amountValue = null;
        String amount = intent.getStringExtra("amount");

        if (intent.hasExtra(EXTRA_DISCOUNT) && extras != null) {
            presenter.setDiscount((Discount) extras.getParcelable(EXTRA_DISCOUNT));
        }

        if (intent.hasExtra(EXTRA_CAMPAIGN) && extras != null) {
            presenter.setCampaign((Campaign) extras.getParcelable(EXTRA_CAMPAIGN));
        }

        String payerEmail = intent.getStringExtra("payerEmail");
        Boolean automaticSelection = intent.getBooleanExtra("automaticSelection", false);

        if (amount != null) {
            amountValue = new BigDecimal(amount);
        }
        List<PaymentMethod> paymentMethods;
        try {
            Type listType = new TypeToken<List<PaymentMethod>>() {
            }.getType();
            paymentMethods =
                JsonUtil.getInstance().getGson().fromJson(intent.getStringExtra("paymentMethodList"), listType);
        } catch (Exception ex) {
            paymentMethods = null;
        }

        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }

        showBankDeals = intent.getBooleanExtra("showBankDeals", true);

        presenter.setCard(card);
        presenter.setInstallmentsEnabled(installmentsEnabled);
        presenter.setSite(site);
        presenter.setPaymentRecovery(paymentRecovery);
        presenter.setAmount(amountValue);
        presenter.setPaymentMethodList(paymentMethods);
        presenter.setPaymentPreference(paymentPreference);
        presenter.setPayerEmail(payerEmail);
        presenter.setInstallmentsReviewEnabled(installmentsReviewEnabled);
        presenter.setAutomaticSelection(automaticSelection);
    }

    private void setContentView() {
        setContentView(R.layout.mpsdk_activity_card_vault);
    }

    protected void initialize() {
        presenter.initialize();
    }

    @Override
    public void showProgressLayout() {
        LayoutUtil.showProgressLayout(this);
    }

    @Override
    public void askForSecurityCodeFromTokenRecovery() {
        PaymentRecovery paymentRecovery = presenter.getPaymentRecovery();
        String reason = "";
        if (paymentRecovery != null) {
            if (paymentRecovery.isStatusDetailInvalidESC()) {
                reason = TrackingUtil.SECURITY_CODE_REASON_ESC;
            } else if (paymentRecovery.isStatusDetailCallForAuthorize()) {
                reason = TrackingUtil.SECURITY_CODE_REASON_CALL;
            }
        }
        startSecurityCodeActivity(reason);
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void askForSecurityCodeFromInstallments() {
        presenter.checkSecurityCodeFlow();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void askForSecurityCodeWithoutInstallments() {
        presenter.checkSecurityCodeFlow();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void startSecurityCodeActivity(String reason) {
        new MercadoPagoComponents.Activities.SecurityCodeActivityBuilder()
            .setActivity(this)
            .setMerchantPublicKey(publicKey)
            .setSiteId(presenter.getSite().getId())
            .setPaymentMethod(presenter.getPaymentMethod())
            .setCardInfo(presenter.getCardInfo())
            .setToken(presenter.getToken())
            .setCard(presenter.getCard())
            .setPayerAccessToken(privateKey)
            .setESCEnabled(escEnabled)
            .setPaymentRecovery(presenter.getPaymentRecovery())
            .setTrackingReason(reason)
            .startActivity();
    }

    @Override
    public void askForCardInformation() {
        startGuessingCardActivity();
    }

    private void startGuessingCardActivity() {
        final Activity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MercadoPagoComponents.Activities.GuessingCardActivityBuilder()
                    .setActivity(context)
                    .setMerchantPublicKey(publicKey)
                    .setSiteId(presenter.getSite().getId())
                    .setAmount(presenter.getAmount())
                    .setPayerEmail(presenter.getPayerEmail())
                    .setPayerAccessToken(privateKey)
                    .setDiscount(presenter.getDiscount())
                    .setShowBankDeals(showBankDeals)
                    .setPaymentPreference(presenter.getPaymentPreference())
                    .setAcceptedPaymentMethods(presenter.getPaymentMethodList())
                    .setShowDiscount(presenter.getAutomaticSelection())
                    .setPaymentRecovery(presenter.getPaymentRecovery())
                    .startActivity();
                overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MercadoPagoCheckout.TIMER_FINISHED_RESULT_CODE) {
            resolveTimerObserverResult(resultCode);
        } else if (requestCode == MercadoPagoComponents.Activities.GUESSING_CARD_REQUEST_CODE) {
            resolveGuessingCardRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.ISSUERS_REQUEST_CODE) {
            resolveIssuersRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.INSTALLMENTS_REQUEST_CODE) {
            resolveInstallmentsRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.SECURITY_CODE_REQUEST_CODE) {
            resolveSecurityCodeRequest(resultCode, data);
        } else if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            resolveErrorRequest(resultCode, data);
        }
    }

    private void resolveTimerObserverResult(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (presenter != null) {
            outState.putBoolean("installmentsEnabled", presenter.isInstallmentsEnabled());
            outState.putBoolean("installmentsReviewEnabled", presenter.getInstallmentsReviewEnabled());
            outState.putString("merchantPublicKey", publicKey);
            outState.putString("privateKey", privateKey);
            outState.putString("site", JsonUtil.getInstance().toJson(presenter.getSite()));
            outState.putString("card", JsonUtil.getInstance().toJson(presenter.getCard()));
            outState
                .putString("paymentRecovery", JsonUtil.getInstance().toJson(presenter.getPaymentRecovery()));
            outState.putBoolean("showBankDeals", showBankDeals);
            outState.putBoolean("installmentsListShown", presenter.isInstallmentsListShown());
            outState.putBoolean("issuersListShown", presenter.isIssuersListShown());
            outState.putBoolean("escEnabled", escEnabled);

            if (presenter.getPayerCostList() != null) {
                outState
                    .putString("payerCostsList", JsonUtil.getInstance().toJson(presenter.getPayerCostList()));
            }

            if (presenter.getAmount() != null) {
                outState.putString("amount", presenter.getAmount().toString());
            }

            if (presenter.getPaymentMethodList() != null) {
                outState.putString("paymentMethodList",
                    JsonUtil.getInstance().toJson(presenter.getPaymentMethodList()));
            }

            if (presenter.getPaymentPreference() != null) {
                outState.putString("paymentPreference",
                    JsonUtil.getInstance().toJson(presenter.getPaymentPreference()));
            }

            if (presenter.getPaymentMethod() != null) {
                outState
                    .putString("paymentMethod", JsonUtil.getInstance().toJson(presenter.getPaymentMethod()));
            }

            if (presenter.getIssuer() != null) {
                outState.putString("issuer", JsonUtil.getInstance().toJson(presenter.getIssuer()));
            }

            if (presenter.getPayerCost() != null) {
                outState.putString("payerCost", JsonUtil.getInstance().toJson(presenter.getPayerCost()));
            }

            if (presenter.getToken() != null) {
                outState.putString("token", JsonUtil.getInstance().toJson(presenter.getToken()));
            }

            if (presenter.getCardInfo() != null) {
                outState.putString("cardInfo", JsonUtil.getInstance().toJson(presenter.getCardInfo()));
            }
        }
    }

    private void resolveErrorRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.recoverFromFailure();
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    protected void resolveIssuersRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Issuer issuer = JsonUtil.getInstance().fromJson(bundle.getString("issuer"), Issuer.class);

            presenter.resolveIssuersRequest(issuer);
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    protected void resolveInstallmentsRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            PayerCost payerCost = JsonUtil.getInstance().fromJson(bundle.getString("payerCost"), PayerCost.class);
            Discount discount = JsonUtil.getInstance().fromJson(bundle.getString("discount"), Discount.class);

            presenter.resolveInstallmentsRequest(payerCost, discount);
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    protected void resolveGuessingCardRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            PaymentMethod paymentMethod =
                JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);
            Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra("discount"), Discount.class);
            Issuer issuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
            PayerCost payerCost = JsonUtil.getInstance().fromJson(data.getStringExtra("payerCost"), PayerCost.class);

            List<PayerCost> payerCosts;
            try {
                Type listType = new TypeToken<List<PayerCost>>() {
                }.getType();
                payerCosts = JsonUtil.getInstance().getGson().fromJson(data.getStringExtra("payerCosts"), listType);
            } catch (Exception ex) {
                payerCosts = null;
            }

            List<Issuer> issuers;
            try {
                Type listType = new TypeToken<List<Issuer>>() {
                }.getType();
                issuers = JsonUtil.getInstance().getGson().fromJson(data.getStringExtra("issuers"), listType);
            } catch (Exception ex) {
                issuers = null;
            }

            presenter
                .resolveNewCardRequest(paymentMethod, token, payerCost, issuer, payerCosts, issuers,
                    discount);
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    protected void resolveSecurityCodeRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Token token = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);

            presenter.resolveSecurityCodeRequest(token);
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    @Override
    public void cancelCardVault() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void startIssuersActivity() {
        new MercadoPagoComponents.Activities.IssuersActivityBuilder()
            .setActivity(this)
            .setMerchantPublicKey(publicKey)
            .setPayerAccessToken(privateKey)
            .setPaymentMethod(presenter.getPaymentMethod())
            .setCardInfo(presenter.getCardInfo())
            .setIssuers(presenter.getIssuersList())
            .startActivity();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void askForInstallmentsFromIssuers() {
        startInstallmentsActivity();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void askForInstallmentsFromNewCard() {
        startInstallmentsActivity();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void askForInstallments() {
        startInstallmentsActivity();
        animateTransitionSlideInSlideOut();
    }

    private void startInstallmentsActivity() {
        new MercadoPagoComponents.Activities.InstallmentsActivityBuilder()
            .setActivity(this)
            .setMerchantPublicKey(publicKey)
            .setPayerAccessToken(privateKey)
            .setPaymentMethod(presenter.getPaymentMethod())
            .setAmount(presenter.getAmount())
            .setPayerEmail(presenter.getPayerEmail())
            .setDiscount(presenter.getDiscount(),presenter.getCampaign())
            .setIssuer(presenter.getIssuer())
            .setPaymentPreference(presenter.getPaymentPreference())
            .setSite(presenter.getSite())
            .setInstallmentsEnabled(presenter.isInstallmentsEnabled())
            .setInstallmentsReviewEnabled(presenter.getInstallmentsReviewEnabled())
            .setCardInfo(presenter.getCardInfo())
            .setPayerCosts(presenter.getPayerCostList())
            .startActivity();
    }

    public void animateTransitionSlideInSlideOut() {
        overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
    }

    @Override
    public void finishWithResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("payerCost", JsonUtil.getInstance().toJson(presenter.getPayerCost()));
        returnIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(presenter.getPaymentMethod()));
        returnIntent.putExtra("token", JsonUtil.getInstance().toJson(presenter.getToken()));
        returnIntent.putExtra("issuer", JsonUtil.getInstance().toJson(presenter.getIssuer()));
        returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(presenter.getDiscount()));
        returnIntent.putExtra("card", JsonUtil.getInstance().toJson(presenter.getCard()));
        setResult(RESULT_OK, returnIntent);
        finish();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void showApiExceptionError(ApiException exception, String requestOrigin) {
        ApiUtil.showApiExceptionError(this, exception, publicKey, requestOrigin);
    }

    @Override
    public void showError(MercadoPagoError error, String requestOrigin) {
        if (error != null && error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error, publicKey);
        }
    }
}
