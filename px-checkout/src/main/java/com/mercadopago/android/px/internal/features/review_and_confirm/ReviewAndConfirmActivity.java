package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.MercadoPagoBaseActivity;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodeParams;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.features.paymentresult.PaymentResultActivity;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.ReviewAndConfirmContainer;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.CancelPaymentAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ConfirmPaymentAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.ItemsModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.tracker.Tracker;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.ComponentManager;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static com.mercadopago.android.px.core.MercadoPagoCheckout.EXTRA_ERROR;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCELED_RYC;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCEL_PAYMENT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_ERROR;

public final class ReviewAndConfirmActivity extends MercadoPagoBaseActivity implements
    ReviewAndConfirm.View, ActionDispatcher {

    private static final int REQ_CARD_VAULT = 0x01;

    private static final String EXTRA_TERMS_AND_CONDITIONS = "extra_terms_and_conditions";
    private static final String EXTRA_PAYMENT_MODEL = "extra_payment_model";
    private static final String EXTRA_SUMMARY_MODEL = "extra_summary_model";
    private static final String EXTRA_PUBLIC_KEY = "extra_public_key";
    private static final String EXTRA_ITEMS = "extra_items";
    private static final String EXTRA_DISCOUNT_TERMS_AND_CONDITIONS = "extra_discount_terms_and_conditions";

    /* default */ ReviewAndConfirmPresenter presenter;

    private View confirmButton;

    private ExplodingFragment explodingFragment;

    private View floatingConfirmLayout;

    //TODO refactor.
    public static Intent getIntent(@NonNull final Context context,
        @NonNull final String merchantPublicKey,
        @Nullable final TermsAndConditionsModel mercadoPagoTermsAndConditions,
        @NonNull final PaymentModel paymentModel,
        @NonNull final SummaryModel summaryModel,
        @NonNull final ItemsModel itemsModel,
        @Nullable final TermsAndConditionsModel discountTermsAndConditions) {

        final Intent intent = new Intent(context, ReviewAndConfirmActivity.class);
        intent.putExtra(EXTRA_PUBLIC_KEY, merchantPublicKey);
        intent.putExtra(EXTRA_TERMS_AND_CONDITIONS, mercadoPagoTermsAndConditions);
        intent.putExtra(EXTRA_PAYMENT_MODEL, paymentModel);
        intent.putExtra(EXTRA_SUMMARY_MODEL, summaryModel);
        intent.putExtra(EXTRA_ITEMS, itemsModel);
        intent.putExtra(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS, discountTermsAndConditions);

        return intent;
    }

    public static Intent getIntentForAction(@NonNull final Context context,
        @NonNull final String merchantPublicKey,
        @Nullable final TermsAndConditionsModel mercadoPagoTermsAndConditions,
        @NonNull final PaymentModel paymentModel,
        @NonNull final SummaryModel summaryModel,
        @NonNull final ItemsModel itemsModel,
        @Nullable final TermsAndConditionsModel discountTermsAndConditions,
        @NonNull final PostPaymentAction postPaymentAction) {
        final Intent intent = getIntent(context, merchantPublicKey, mercadoPagoTermsAndConditions,
            paymentModel, summaryModel, itemsModel,
            discountTermsAndConditions);

        postPaymentAction.addToIntent(intent);

        return intent;
    }

    /**
     * It is necessary to check if we have a PostPaymentAction when we start Review and confirm
     * so that we don't show the UI for review and confirm right away, and we can start the
     * recover payment process
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_view_container_review_and_confirm);
        initializeViews();
        final Session session = Session.getSession(this);
        presenter = new ReviewAndConfirmPresenter(session.getPaymentRepository(),
            session.getBusinessModelMapper());
        presenter.attachView(this);

        if (savedInstanceState == null) {
            checkIntentActions();
        }
    }

    /**
     * This is called whenever savedInstanceState is null, that means the first time the activity is launched.
     * When we don't have a payment yet, we don't have PostPaymentAction so we don't do anything
     * If we have a PostPaymentAction (CheckoutActivity launches ReviewAndConfirm again to recover the payment)
     * we need to get it and execute the recovery
     */
    private void checkIntentActions() {
        if (PostPaymentAction.hasPostPaymentAction(getIntent())) {
            presenter.executePostPaymentAction(PostPaymentAction.fromBundle(getIntent().getExtras()));
        }
    }

    /**
     * We need to save something on save instance state to cover the cases when the activity is destroyed.
     * If there is something saved (savedInstanceState is not null), that means we already tried to
     * recover the payment if that was necessary.
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt("TOBESAVED", 1);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        presenter.attachView(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        presenter.detachView();
        super.onPause();
    }

    /**
     * On a payment recovery, when it comes back from card vault, we need the view to be initialized
     * before we can try to make the payment again (so we can explode the button).
     * We added the post() method to make sure the view was initialized.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
        case REQ_CARD_VAULT:
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    resolveCardVaultRequest(resultCode, data);
                }
            });
            break;
        case ErrorUtil.ERROR_REQUEST_CODE:
            resolveErrorRequest(resultCode, data);
            break;
        default:
            //Do nothing
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeViews() {
        confirmButton = findViewById(R.id.floating_confirm);
        initToolbar();
        initBody();
    }

    private void initBody() {
        final ViewGroup mainContent = findViewById(R.id.scroll_view);
        initContent(mainContent);
        initFloatingButton(mainContent);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayShowTitleEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.px_activity_checkout_title));
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            collapsingToolbarLayout.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            collapsingToolbarLayout.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void initFloatingButton(final ViewGroup scrollView) {
        floatingConfirmLayout = findViewById(R.id.floating_confirm_layout);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                presenter.onPaymentConfirm();
            }
        });
        configureFloatingBehaviour(scrollView, floatingConfirmLayout);
    }

    private void configureFloatingBehaviour(final ViewGroup scrollView, final View floatingConfirmLayout) {
        addScrollBottomPadding(floatingConfirmLayout, scrollView);
        configureScrollLayoutListener(floatingConfirmLayout, scrollView);
        addScrollListener(floatingConfirmLayout, scrollView);
    }

    private void addScrollBottomPadding(final View floatingConfirmLayout, final View scrollView) {
        final ViewTreeObserver floatingObserver = floatingConfirmLayout.getViewTreeObserver();
        floatingObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int bottomPadding = floatingConfirmLayout.getHeight();
                if (scrollView.getPaddingBottom() != bottomPadding) {
                    scrollView.setPadding(scrollView.getPaddingLeft(), scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(), bottomPadding);
                }
            }
        });
    }

    private void configureScrollLayoutListener(final View floatingConfirmLayout, final ViewGroup scrollView) {
        final ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView);
            }
        });
    }

    private void addScrollListener(final View floatingConfirmLayout, final ViewGroup scrollView) {
        final ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView);
            }
        });
    }

    private void resolveFloatingButtonElevationVisibility(final View floatingConfirmLayout,
        final ViewGroup scrollView) {
        final ViewGroup content = (ViewGroup) scrollView.getChildAt(0);
        final int containerHeight = content.getHeight();
        final float finalSize = containerHeight - scrollView.getHeight();
        setFloatingElevationVisibility(floatingConfirmLayout, scrollView.getScrollY() < finalSize);
    }

    private void initContent(final ViewGroup mainContent) {
        final ReviewAndConfirmContainer.Props props = getActivityParameters();
        final ComponentManager manager = new ComponentManager(this);

        final ReviewAndConfirmContainer container =
            new ReviewAndConfirmContainer(props, this);

        container.setDispatcher(this);
        manager.render(container, mainContent);
    }

    private ReviewAndConfirmContainer.Props getActivityParameters() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            final TermsAndConditionsModel termsAndConditionsModel = extras.getParcelable(EXTRA_TERMS_AND_CONDITIONS);
            final PaymentModel paymentModel = extras.getParcelable(EXTRA_PAYMENT_MODEL);
            final SummaryModel summaryModel = extras.getParcelable(EXTRA_SUMMARY_MODEL);
            final ItemsModel itemsModel = extras.getParcelable(EXTRA_ITEMS);
            final TermsAndConditionsModel discountTermsAndConditions =
                extras.getParcelable(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS);

            final Session session = Session.getSession(this);
            final AdvancedConfiguration advancedConfiguration = session.getConfigurationModule().getPaymentSettings()
                .getAdvancedConfiguration();

            final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
                advancedConfiguration.getReviewAndConfirmConfiguration();

            Tracker.trackReviewAndConfirmScreen(getApplicationContext(),
                paymentModel);
            return new ReviewAndConfirmContainer.Props(termsAndConditionsModel,
                paymentModel,
                summaryModel,
                reviewAndConfirmConfiguration,
                advancedConfiguration.getDynamicFragmentConfiguration(),
                itemsModel, discountTermsAndConditions);
        }

        throw new IllegalStateException("Unsupported parameters for Review and confirm activity");
    }

    private void setFloatingElevationVisibility(final View floatingConfirmLayout, final boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final float elevationInPixels =
                visible ? getBaseContext().getResources().getDimension(R.dimen.px_xxs_margin) : 0;
            floatingConfirmLayout.setElevation(elevationInPixels);
        }
    }

    @Override
    public void trackPaymentConfirmation() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final PaymentModel paymentModel = extras.getParcelable(EXTRA_PAYMENT_MODEL);
            final SummaryModel summaryModel = extras.getParcelable(EXTRA_SUMMARY_MODEL);
            if (paymentModel != null && summaryModel != null) {
                Tracker.trackCheckoutConfirm(getApplicationContext(), paymentModel, summaryModel);
            }
        }
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof ChangePaymentMethodAction) {
            changePaymentMethod();
        } else if (action instanceof CancelPaymentAction) {
            onBackPressed();
        } else if (action instanceof ConfirmPaymentAction) {
            presenter.onPaymentConfirm();
        } else if (action instanceof ExitAction) {
            processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException("action not allowed");
        }
    }

    /**
     * Called when user press back or
     * Cancel action is dispatched.
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED_RYC);
        super.onBackPressed();
    }

    /**
     * Exit review and confirm and notify the origin activity
     * that payment method change button has been pressed.
     */
    private void changePaymentMethod() {
        setResult(RESULT_CHANGE_PAYMENT_METHOD);
        finish();
    }

    /**
     * Custom exit added by Configuration.
     *
     * @param action custom exit action.
     */
    private void processCustomExit(final ExitAction action) {
        setResult(RESULT_CANCEL_PAYMENT, action.toIntent());
        finish();
    }

    // Opens CVV screen
    @Override
    public void showCardCVVRequired(@NonNull final Card card) {
        new Constants.Activities.CardVaultActivityBuilder()
            .setCard(card)
            .startActivity(this, REQ_CARD_VAULT);
    }

    // Opens Card vault with recovery info.
    @Override
    public void startPaymentRecoveryFlow(final PaymentRecovery recovery) {
        new Constants.Activities.CardVaultActivityBuilder()
            .setPaymentRecovery(recovery)
            .startActivity(this, REQ_CARD_VAULT);
    }

    @Override
    public void showErrorScreen(@NonNull final MercadoPagoError error) {
        ErrorUtil.startErrorActivity(this, error);
    }

    /**
     * When payment needs to start the visual payment processor
     * it won't come back to review and confirm.
     * The result for the start activity will be delegated to
     * Checkout activity.
     */
    @Override
    public void showPaymentProcessor() {
        overrideTransitionWithNoAnimation();
        final Intent intent = PaymentProcessorActivity.getIntent(this);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    /**
     * When payment is shown inside congrats
     * the user can't return to review and confirm.
     * Result for this activity will be transferred.
     */
    @Override
    public void showResult(final BusinessPaymentModel businessPaymentModel) {
        overrideTransitionFadeInFadeOut();
        final Intent intent = BusinessPaymentResultActivity.getIntent(this, businessPaymentModel);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    /**
     * When payment is shown inside congrats
     * the user can't return to review and confirm.
     * Result for this activity will be transferred.
     */
    @Override
    public void showResult(@NonNull final PaymentResult paymentResult) {
        overrideTransitionFadeInFadeOut();
        final Intent intent = PaymentResultActivity.getIntent(this, paymentResult,
            PostPaymentAction.OriginAction.REVIEW_AND_CONFIRM);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    @Override
    public void cancelCheckoutAndInformError(@NonNull final MercadoPagoError mercadoPagoError) {
        //TODO handle Error better - It goes back to checkout activity.
        // Goes to Checkout activity and provides error object.
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_ERROR, mercadoPagoError);
        setResult(RESULT_ERROR, intent);
        finish();
    }

    //TODO remove duplication
    private void resolveErrorRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.recoverFromFailure();
        } else {
            final MercadoPagoError mercadoPagoError = data.getStringExtra(EXTRA_ERROR) == null ? null :
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            presenter.onError(mercadoPagoError);
        }
    }

    //TODO remove duplication
    private void resolveCardVaultRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.onCardFlowResponse();
        } else {
            final MercadoPagoError mercadoPagoError =
                (data == null || data.getStringExtra(EXTRA_ERROR) == null) ? null :
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            if (mercadoPagoError == null) {
                presenter.onCardFlowCancel();
            } else {
                presenter.onError(mercadoPagoError);
            }
        }
    }

    @Override
    public void startLoadingButton(final int paymentTimeout) {
        final int[] location = new int[2];

        confirmButton.getLocationOnScreen(location);
        final ExplodeParams explodeParams =
            new ExplodeParams(location[1] - confirmButton.getMeasuredHeight() / 2,
                confirmButton.getMeasuredHeight(),
                (int) getResources().getDimension(R.dimen.px_s_margin),
                getResources().getString(R.string.px_processing_payment_button),
                paymentTimeout);
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        explodingFragment = ExplodingFragment.newInstance(explodeParams);
        supportFragmentManager.beginTransaction()
            .replace(R.id.exploding_frame, explodingFragment)
            .commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
    }

    @Override
    public void cancelLoadingButton() {
        if (explodingFragment != null) {
            final FragmentManager supportFragmentManager = getSupportFragmentManager();
            supportFragmentManager
                .beginTransaction()
                .remove(explodingFragment)
                .commitAllowingStateLoss();
            supportFragmentManager.executePendingTransactions();
            explodingFragment = null;
        }
    }

    @Override
    public void showLoadingFor(@NonNull final ExplodeDecorator decorator,
        @NonNull final ExplodingFragment.ExplodingAnimationListener explodingAnimationListener) {
        getSupportFragmentManager().executePendingTransactions();
        explodingFragment.finishLoading(decorator, explodingAnimationListener);
    }

    @Override
    public void hideConfirmButton() {
        confirmButton.setVisibility(View.GONE);
    }

    @Override
    public void showConfirmButton() {
        confirmButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showErrorSnackBar(@NonNull final MercadoPagoError error) {
        MeliSnackbar.make(floatingConfirmLayout, error.getMessage(), Snackbar.LENGTH_LONG,
            MeliSnackbar.SnackbarType.ERROR).show();
        Tracker.trackError(getApplicationContext(), error);
    }
}
