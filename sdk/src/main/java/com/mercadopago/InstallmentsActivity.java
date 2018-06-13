package com.mercadopago;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.reflect.TypeToken;
import com.mercadopago.adapters.PayerCostsAdapter;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.controllers.CheckoutTimer;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.listeners.RecyclerItemClickListener;
import com.mercadopago.lite.controllers.CustomServicesHandler;
import com.mercadopago.lite.exceptions.ApiException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.CouponDiscount;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Site;
import com.mercadopago.observers.TimerObserver;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.presenters.InstallmentsPresenter;
import com.mercadopago.providers.InstallmentsProviderImpl;
import com.mercadopago.tracker.FlowHandler;
import com.mercadopago.tracker.MPTrackingContext;
import com.mercadopago.tracking.model.ScreenViewEvent;
import com.mercadopago.tracking.utils.TrackingUtil;
import com.mercadopago.uicontrollers.FontCache;
import com.mercadopago.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.uicontrollers.card.FrontCardView;
import com.mercadopago.uicontrollers.installments.InstallmentsReviewView;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.ScaleUtil;
import com.mercadopago.views.AmountView;
import com.mercadopago.views.DiscountDetailDialog;
import com.mercadopago.views.InstallmentsActivityView;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

public class InstallmentsActivity extends MercadoPagoBaseActivity implements InstallmentsActivityView, TimerObserver {

    protected InstallmentsPresenter presenter;

    //Local vars
    protected String publicKey;
    protected String privateKey;
    protected boolean mActivityActive;

    protected String mDefaultBaseURL;

    //View controls
    protected PayerCostsAdapter mPayerCostsAdapter;
    protected RecyclerView mInstallmentsRecyclerView;

    //ViewMode
    protected boolean mLowResActive;

    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mLowResTitleToolbar;

    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected FrameLayout mInstallmentsReview;
    protected Toolbar mNormalToolbar;
    protected FrontCardView mFrontCardView;
    protected MPTextView mTimerTextView;

    private MPTextView mNoInstallmentsRateTextView;
    private LinearLayout mNoInstallmentsRate;

    private AmountView amountView;

    public static final String EXTRA_DISCOUNT = "discount";
    public static final String EXTRA_CAMPAIGN = "campaign";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPresenter();
        getActivityParameters();

        setMerchantInfo();

        presenter.attachView(this);
        presenter.attachResourcesProvider(new InstallmentsProviderImpl(this, publicKey, privateKey));

        mActivityActive = true;
        analyzeLowRes();
        setContentView();
        initializeControls();
        initializeView();

        presenter.initialize();
    }

    private void createPresenter() {
        presenter = new InstallmentsPresenter();
    }

    private void getActivityParameters() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        publicKey = intent.getStringExtra("merchantPublicKey");
        privateKey = intent.getStringExtra("payerAccessToken");

        presenter.setSite(JsonUtil.getInstance().fromJson(intent.getStringExtra("site"), Site.class));
        presenter.setPaymentMethod(
            JsonUtil.getInstance().fromJson(intent.getStringExtra("paymentMethod"), PaymentMethod.class));
        presenter.setIssuer(JsonUtil.getInstance().fromJson(intent.getStringExtra("issuer"), Issuer.class));
        presenter.setCardInfo(JsonUtil.getInstance().fromJson(intent.getStringExtra("cardInfo"), CardInfo.class));

        BigDecimal amount = null;
        if (intent.getStringExtra("amount") != null) {
            amount = new BigDecimal(intent.getStringExtra("amount"));
        }

        if (intent.hasExtra(EXTRA_DISCOUNT) && extras != null) {
            presenter.setDiscount((Discount) extras.getParcelable(EXTRA_DISCOUNT));
        }

        if (intent.hasExtra(EXTRA_CAMPAIGN) && extras != null) {
            presenter.setCampaign((Campaign) extras.getParcelable(EXTRA_CAMPAIGN));
        }

        presenter.setAmount(amount);

        List<PayerCost> payerCosts;
        try {
            Type listType = new TypeToken<List<PayerCost>>() {
            }.getType();
            payerCosts = JsonUtil.getInstance().getGson().fromJson(intent.getStringExtra("payerCosts"), listType);
        } catch (Exception ex) {
            payerCosts = null;
        }

        presenter.setPayerCosts(payerCosts);
        presenter.setPaymentPreference(
            JsonUtil.getInstance().fromJson(intent.getStringExtra("paymentPreference"), PaymentPreference.class));
        presenter.setInstallmentsReviewEnabled(intent.getBooleanExtra("installmentsReviewEnabled", true));
        presenter.setPayerEmail(intent.getStringExtra("payerEmail"));
    }

    private void setMerchantInfo() {
        if (CustomServicesHandler.getInstance().getServicePreference() != null) {
            mDefaultBaseURL = CustomServicesHandler.getInstance().getServicePreference().getDefaultBaseURL();
        }
    }

    public void analyzeLowRes() {
        if (presenter.isRequiredCardDrawn()) {
            mLowResActive = ScaleUtil.isLowRes(this);
        } else {
            mLowResActive = true;
        }
    }

    public void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    public void setContentViewLowRes() {
        setContentView(R.layout.mpsdk_activity_installments_lowres);
    }

    public void setContentViewNormal() {
        setContentView(R.layout.mpsdk_activity_installments_normal);
    }

    private void initializeControls() {
        mInstallmentsRecyclerView = findViewById(R.id.mpsdkActivityInstallmentsView);
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);

        amountView = findViewById(R.id.amount_view);

        if (mLowResActive) {
            initializeLowResControls();
        } else {
            initializeNormalControls();
        }

        mInstallmentsReview = findViewById(R.id.mpsdkInstallmentsReview);
    }

    private void initializeLowResControls() {
        mLowResToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);

        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            Toolbar.LayoutParams marginParams =
                new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginParams.setMargins(0, 0, 0, 6);
            mLowResTitleToolbar.setLayoutParams(marginParams);
            mLowResTitleToolbar.setTextSize(19);
            mTimerTextView.setTextSize(17);
        }

        mLowResToolbar.setVisibility(View.VISIBLE);
    }

    private void initializeNormalControls() {
        mCollapsingToolbar = findViewById(R.id.mpsdkCollapsingToolbar);
        mAppBar = findViewById(R.id.mpsdkInstallmentesAppBar);
        mCardContainer = findViewById(R.id.mpsdkActivityCardContainer);
        mNormalToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mNormalToolbar.setVisibility(View.VISIBLE);
    }

    private void initializeView() {
        loadViews();
        hideHeader();
        showTimer();
    }

    protected void trackScreen() {
        MPTrackingContext mTrackingContext = new MPTrackingContext.Builder(this, publicKey)
            .setVersion(BuildConfig.VERSION_NAME)
            .build();

        ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_INSTALLMENTS)
            .setScreenName(TrackingUtil.SCREEN_NAME_CARD_FORM_INSTALLMENTS)
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, presenter.getPaymentMethod().getId())
            .build();

        mTrackingContext.trackEvent(event);
    }

    public void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    @Override
    public void warnAboutBankInterests() {
        mNoInstallmentsRate = findViewById(R.id.mpsdkNoInstallmentsRate);
        mNoInstallmentsRate.setVisibility(View.VISIBLE);
        mNoInstallmentsRateTextView = findViewById(R.id.mpsdkNoInstallmentsRateTextView);
        mNoInstallmentsRateTextView.setVisibility(View.VISIBLE);
        mNoInstallmentsRateTextView.setText(R.string.mpsdk_interest_label);
    }

    public void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.mpsdk_card_installments_title));

        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mLowResTitleToolbar.setTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    public void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.mpsdk_card_installments_title));
        setCustomFontNormal();

        mFrontCardView = new FrontCardView(this, CardRepresentationModes.SHOW_FULL_FRONT_ONLY);
        mFrontCardView.setSize(CardRepresentationModes.MEDIUM_SIZE);
        mFrontCardView.setPaymentMethod(presenter.getPaymentMethod());
        if (presenter.getCardInfo() != null) {
            mFrontCardView.setCardNumberLength(presenter.getCardNumberLength());
            mFrontCardView.setLastFourDigits(presenter.getCardInfo().getLastFourDigits());
        }
        mFrontCardView.inflateInParent(mCardContainer, true);
        mFrontCardView.initializeControls();
        mFrontCardView.draw();
        mFrontCardView.enableEditingCardNumber();
    }

    private void loadToolbarArrow(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInstallmentsReviewVisible()) {
                        hideInstallmentsReviewView();
                        showInstallmentsRecyclerView();
                    } else {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(presenter.getDiscount()));
                        setResult(RESULT_CANCELED, returnIntent);
                        finish();
                    }
                }
            });
        }
    }

    private void setCustomFontNormal() {
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mCollapsingToolbar.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            mCollapsingToolbar.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void hideHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.GONE);
        } else {
            mNormalToolbar.setTitle("");
        }
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            CheckoutTimer.getInstance().addObserver(this);
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void showHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mNormalToolbar.setTitle(getString(R.string.mpsdk_card_installments_title));
            setCustomFontNormal();
        }
    }

    private void initializeAdapter(OnSelectedCallback<Integer> onSelectedCallback) {
        mPayerCostsAdapter = new PayerCostsAdapter(presenter.getSite(), onSelectedCallback);
        initializeAdapterListener(mPayerCostsAdapter, mInstallmentsRecyclerView);
    }

    private void initializeAdapterListener(RecyclerView.Adapter adapter, RecyclerView view) {
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addOnItemTouchListener(new RecyclerItemClickListener(this,
            new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    presenter.onItemSelected(position);
                }
            }));
    }

    @Override
    public void showError(MercadoPagoError error, String requestOrigin) {
        if (error.isApiException()) {
            showApiException(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error, publicKey);
        }
    }

    public void showApiException(ApiException apiException, String requestOrigin) {
        if (mActivityActive) {
            ApiUtil.showApiExceptionError(this, apiException, publicKey, requestOrigin);
        }
    }

    @Override
    public void onTimeChanged(String timeToShow) {
        mTimerTextView.setText(timeToShow);
    }

    @Override
    public void onFinish() {
        setResult(MercadoPagoCheckout.TIMER_FINISHED_RESULT_CODE);
        finish();
    }

    @Override
    public void showInstallments(List<PayerCost> payerCostList, OnSelectedCallback<Integer> onSelectedCallback) {
        //We track after evaluating default installments or autoselected installments
        trackScreen();
        initializeAdapter(onSelectedCallback);
        mPayerCostsAdapter.addResults(payerCostList);
    }

    @Override
    public void showLoadingView() {
        mInstallmentsRecyclerView.setVisibility(View.GONE);
        LayoutUtil.showProgressLayout(this);
    }

    @Override
    public void hideLoadingView() {
        mInstallmentsRecyclerView.setVisibility(View.VISIBLE);
        LayoutUtil.showRegularLayout(this);
    }

    @Override
    public void finishWithResult(PayerCost payerCost) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("payerCost", JsonUtil.getInstance().toJson(payerCost));
        returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(presenter.getDiscount()));
        setResult(RESULT_OK, returnIntent);
        finish();
        animateTransitionSlideInSlideOut();
    }

    public void animateTransitionSlideInSlideOut() {
        overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
    }

    @Override
    public void onBackPressed() {
        if (isInstallmentsReviewVisible()) {
            hideInstallmentsReviewView();
            showInstallmentsRecyclerView();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("backButtonPressed", true);
            returnIntent.putExtra("discount", JsonUtil.getInstance().toJson(presenter.getDiscount()));
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        } else if (requestCode == MercadoPagoComponents.Activities.DISCOUNTS_REQUEST_CODE) {
            resolveDiscountRequest(resultCode, data);
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    protected void resolveDiscountRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (presenter.getDiscount() == null) {
                Discount discount = JsonUtil.getInstance().fromJson(data.getStringExtra("discount"), Discount.class);
                presenter.onDiscountReceived(discount);
            }
        }
    }

    @Override
    public void startDiscountFlow(BigDecimal transactionAmount) {
        MercadoPagoComponents.Activities.DiscountsActivityBuilder mercadoPagoBuilder =
            new MercadoPagoComponents.Activities.DiscountsActivityBuilder();

        mercadoPagoBuilder.setActivity(this)
            .setMerchantPublicKey(publicKey)
            .setPayerEmail(presenter.getPayerEmail())
            .setAmount(transactionAmount)
            .setDiscount(presenter.getDiscount());

        mercadoPagoBuilder.setDiscount(presenter.getDiscount());
        mercadoPagoBuilder.startActivity();
    }

    @Override
    public void initInstallmentsReviewView(final PayerCost payerCost) {
        InstallmentsReviewView installmentsReviewView = new MercadoPagoUI.Views.InstallmentsReviewViewBuilder()
            .setContext(this)
            .setCurrencyId(presenter.getSite().getCurrencyId())
            .setPayerCost(payerCost)
            .build();

        installmentsReviewView.inflateInParent(mInstallmentsReview, true);
        installmentsReviewView.initializeControls();
        installmentsReviewView.draw();
        installmentsReviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithResult(payerCost);
            }
        });
    }

    @Override
    public void hideInstallmentsRecyclerView() {
        mInstallmentsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showInstallmentsRecyclerView() {
        mInstallmentsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInstallmentsReviewView() {
        mInstallmentsReview.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInstallmentsReviewView() {
        mInstallmentsReview.setVisibility(View.GONE);
    }

    private Boolean isInstallmentsReviewVisible() {
        return mInstallmentsReview.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showAmount(@Nullable Discount discount, @Nullable Campaign campaign, final BigDecimal totalAmount,
        final Site site) {
        //TODO refactor -> should be not null // Quick and dirty implementation.
        if (discount == null) {
            amountView.show(totalAmount, site);
        } else {
            amountView.show(discount, campaign, totalAmount, site);
        }

        amountView.setOnClickListener(presenter);
    }

    @Override
    public void showDetailDialog(@NonNull final Discount discount, @NonNull final Campaign campaign) {
        DiscountDetailDialog.showDialog(discount, campaign, getSupportFragmentManager());
    }

    @Override
    public void showDetailDialog(@NonNull final CouponDiscount discount, @NonNull final Campaign campaign) {
        //TODO - Other dialog.
        DiscountDetailDialog.showDialog(null, null, getSupportFragmentManager());
    }

    @Override
    public void showDiscountInputDialog() {
        //TODO - Other dialog.
        DiscountDetailDialog.showDialog(null, null, getSupportFragmentManager());
    }
}
