package com.mercadopago.review_and_confirm;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.mercadopago.MercadoPagoBaseActivity;
import com.mercadopago.R;
import com.mercadopago.components.Action;
import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.ComponentManager;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.plugins.model.ExitAction;
import com.mercadopago.review_and_confirm.components.ReviewAndConfirmContainer;
import com.mercadopago.review_and_confirm.components.actions.CancelPaymentAction;
import com.mercadopago.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.review_and_confirm.components.actions.ConfirmPaymentAction;
import com.mercadopago.review_and_confirm.models.ItemsModel;
import com.mercadopago.review_and_confirm.models.PaymentModel;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import com.mercadopago.review_and_confirm.models.SummaryModel;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.tracker.Tracker;
import com.mercadopago.uicontrollers.FontCache;

public final class ReviewAndConfirmActivity extends MercadoPagoBaseActivity implements ActionDispatcher {

    public static final int RESULT_CANCEL_PAYMENT = 4;
    public static final int RESULT_CHANGE_PAYMENT_METHOD = 3;

    private static final String EXTRA_TERMS_AND_CONDITIONS = "extra_terms_and_conditions";
    private static final String EXTRA_PAYMENT_MODEL = "extra_payment_model";
    private static final String EXTRA_SUMMARY_MODEL = "extra_summary_model";
    private static final String EXTRA_PUBLIC_KEY = "extra_public_key";
    private static final String EXTRA_ITEMS = "extra_items";
    private static final String EXTRA_DISCOUNT_TERMS_AND_CONDITIONS = "extra_discount_terms_and_conditions";

    public static void start(@NonNull final Activity activity,
                             @NonNull final String merchantPublicKey,
                             @Nullable final TermsAndConditionsModel mercadoPagoTermsAndConditions,
                             @NonNull final PaymentModel paymentModel,
                             @NonNull final SummaryModel summaryModel,
                             @NonNull final ItemsModel itemsModel,
                             @Nullable final TermsAndConditionsModel discountTermsAndConditions) {
        //TODO result code should be changed by the outside.
        Intent intent = new Intent(activity, ReviewAndConfirmActivity.class);
        intent.putExtra(EXTRA_PUBLIC_KEY, merchantPublicKey);
        intent.putExtra(EXTRA_TERMS_AND_CONDITIONS, mercadoPagoTermsAndConditions);
        intent.putExtra(EXTRA_PAYMENT_MODEL, paymentModel);
        intent.putExtra(EXTRA_SUMMARY_MODEL, summaryModel);
        intent.putExtra(EXTRA_ITEMS, itemsModel);
        intent.putExtra(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS, discountTermsAndConditions);
        activity.startActivityForResult(intent, MercadoPagoComponents.Activities.REVIEW_AND_CONFIRM_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mpsdk_view_container_review_and_confirm);
        initializeViews();
    }

    private void initializeViews() {
        initToolbar();
        NestedScrollView mainContent = findViewById(R.id.scroll_view);
        initContent(mainContent);
        initFloatingButton(mainContent);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayShowTitleEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.mpsdk_activity_checkout_title));
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            collapsingToolbarLayout.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            collapsingToolbarLayout.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void initFloatingButton(final NestedScrollView scrollView) {
        final View floatingConfirmLayout = findViewById(R.id.floating_confirm_layout);
        findViewById(R.id.floating_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                confirmPayment();
            }
        });

        configureFloatingBehaviour(scrollView, floatingConfirmLayout);
    }

    private void configureFloatingBehaviour(final NestedScrollView scrollView, final View floatingConfirmLayout) {
        addScrollBottomMargin(floatingConfirmLayout, scrollView);
        configureScrollLayoutListener(floatingConfirmLayout, scrollView);
        addScrollListener(floatingConfirmLayout, scrollView);
    }

    private void addScrollBottomMargin(final View floatingConfirmLayout, final NestedScrollView scrollView) {
        ViewTreeObserver floatingObserver = floatingConfirmLayout.getViewTreeObserver();
        floatingObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
                int bottomMargin = floatingConfirmLayout.getHeight();
                if (bottomMargin != params.bottomMargin) {
                    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);
                    scrollView.setLayoutParams(params);
                }
            }
        });
    }

    private void configureScrollLayoutListener(final View floatingConfirmLayout, final NestedScrollView scrollView) {
        ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView);
            }
        });
    }

    private void addScrollListener(final View floatingConfirmLayout, final NestedScrollView scrollView) {
        ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView);
            }
        });
    }


    private void resolveFloatingButtonElevationVisibility(final View floatingConfirmLayout, final NestedScrollView scrollView) {
        ViewGroup content = (ViewGroup) scrollView.getChildAt(0);
        int containerHeight = content.getHeight();
        float finalSize = containerHeight - scrollView.getHeight();
        setFloatingElevationVisibility(floatingConfirmLayout, scrollView.getScrollY() < finalSize);
    }


    private void initContent(final ViewGroup mainContent) {
        ReviewAndConfirmContainer.Props props = getActivityParameters();
        final ComponentManager manager = new ComponentManager(this);

        ReviewAndConfirmPreferences reviewAndConfirmPreferences = CheckoutStore.getInstance().getReviewAndConfirmPreferences();
        final ReviewAndConfirmContainer container = new ReviewAndConfirmContainer(props, this, new SummaryProviderImpl(this, reviewAndConfirmPreferences));

        container.setDispatcher(this);
        manager.render(container, mainContent);
    }

    private ReviewAndConfirmContainer.Props getActivityParameters() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        TermsAndConditionsModel termsAndConditionsModel = null;
        PaymentModel paymentModel = null;
        SummaryModel summaryModel = null;
        TermsAndConditionsModel discountTermsAndConditions = null;

        ItemsModel itemsModel = null;
        if (extras != null) {
            termsAndConditionsModel = extras.getParcelable(EXTRA_TERMS_AND_CONDITIONS);
            paymentModel = extras.getParcelable(EXTRA_PAYMENT_MODEL);
            summaryModel = extras.getParcelable(EXTRA_SUMMARY_MODEL);
            itemsModel = extras.getParcelable(EXTRA_ITEMS);
            discountTermsAndConditions = extras.getParcelable(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS);
            Tracker.trackReviewAndConfirmScreen(getApplicationContext(), getIntent().getStringExtra(EXTRA_PUBLIC_KEY), paymentModel);
        }

        ReviewAndConfirmPreferences reviewAndConfirmPreferences = CheckoutStore.getInstance().getReviewAndConfirmPreferences();

        return new ReviewAndConfirmContainer.Props(termsAndConditionsModel, paymentModel, summaryModel, reviewAndConfirmPreferences, itemsModel,discountTermsAndConditions);
    }

    private void setFloatingElevationVisibility(View floatingConfirmLayout, boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float elevationInPixels = visible ? getBaseContext().getResources().getDimension(R.dimen.mpsdk_xxs_margin) : 0;
            floatingConfirmLayout.setElevation(elevationInPixels);
        }
    }

    private void confirmPayment() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            PaymentModel paymentModel = extras.getParcelable(EXTRA_PAYMENT_MODEL);
            SummaryModel summaryModel = extras.getParcelable(EXTRA_SUMMARY_MODEL);
            Tracker.trackCheckoutConfirm(getApplicationContext(), getIntent().getStringExtra(EXTRA_PUBLIC_KEY), paymentModel, summaryModel);
        }

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void cancelPayment() {
        setResult(RESULT_CANCEL_PAYMENT);
        super.onBackPressed();
    }

    private void changePaymentMethod() {
        setResult(RESULT_CHANGE_PAYMENT_METHOD);
        finish();
    }

    private void processCustomExit(final ExitAction action) {
        setResult(RESULT_CANCEL_PAYMENT, action.toIntent());
        super.onBackPressed();
    }

    @Override
    public void dispatch(Action action) {
        if (action instanceof ChangePaymentMethodAction) {
            changePaymentMethod();
        } else if (action instanceof CancelPaymentAction) {
            cancelPayment();
        } else if (action instanceof ConfirmPaymentAction) {
            confirmPayment();
        } else if (action instanceof ExitAction) {
            processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
