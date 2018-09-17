package com.mercadopago.android.px.internal.features.business_result;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.internal.features.business_result.components.BusinessPaymentContainer;
import com.mercadopago.android.px.internal.tracker.Tracker;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.ComponentManager;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.ExitAction;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;
import static com.mercadopago.android.px.tracking.internal.utils.TrackingUtil.SCREEN_ID_PAYMENT_RESULT_BUSINESS;
import static com.mercadopago.android.px.tracking.internal.utils.TrackingUtil.SCREEN_NAME_PAYMENT_RESULT;

public class BusinessPaymentResultActivity extends AppCompatActivity implements ActionDispatcher {

    private static final String EXTRA_BUSINESS_PAYMENT_MODEL = "extra_business_payment_model";

    public static Intent getIntent(@NonNull final Context context,
        @NonNull final BusinessPaymentModel model) {
        final Intent intent = new Intent(context, BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BusinessPaymentModel model = parseBusinessPaymentModel();

        if (model != null) {
            initializeView(model);
        } else {
            throw new IllegalStateException("BusinessPayment can't be loaded");
        }
        Tracker.trackScreen(SCREEN_ID_PAYMENT_RESULT_BUSINESS, SCREEN_NAME_PAYMENT_RESULT,
            getApplicationContext());
    }

    @Nullable
    private BusinessPaymentModel parseBusinessPaymentModel() {
        return getIntent().getExtras() != null ? (BusinessPaymentModel) getIntent()
            .getExtras()
            .getParcelable(EXTRA_BUSINESS_PAYMENT_MODEL) : null;
    }

    private void initializeView(final BusinessPaymentModel model) {
        final BusinessPaymentContainer businessPaymentContainer = new BusinessPaymentContainer(
            new BusinessPaymentContainer.Props(model.payment, model.getPaymentMethodProps()), this);
        final ComponentManager componentManager = new ComponentManager(this);
        componentManager.render(businessPaymentContainer);
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof ExitAction) {
            processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException("this Action class can't be executed in this screen");
        }
    }

    @Override
    public void onBackPressed() {
        processCustomExit(new ExitAction("exit", RESULT_OK));
    }

    private void processCustomExit(final ExitAction action) {
        final Intent intent = action.toIntent();
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }
}
