package com.mercadopago.plugins;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mercadopago.components.Action;
import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.ComponentManager;
import com.mercadopago.plugins.components.BusinessPaymentContainer;
import com.mercadopago.plugins.model.BusinessPaymentModel;
import com.mercadopago.plugins.model.ExitAction;
import com.mercadopago.tracker.Tracker;

import static com.mercadopago.tracking.utils.TrackingUtil.SCREEN_ID_PAYMENT_RESULT_BUSINESS;
import static com.mercadopago.tracking.utils.TrackingUtil.SCREEN_NAME_PAYMENT_RESULT;

public class BusinessPaymentResultActivity extends AppCompatActivity implements ActionDispatcher {

    private static final String EXTRA_BUSINESS_PAYMENT_MODEL = "extra_business_payment_model";
    private static final String EXTRA_MERCHANT_PUBLIC_KEY = "extra_merchant_public_key";

    public static void start(final AppCompatActivity activity,
                             final BusinessPaymentModel model,
                             final String merchantPublicKey,
                             int requestCode) {
        Intent intent = new Intent(activity, BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        intent.putExtra(EXTRA_MERCHANT_PUBLIC_KEY, merchantPublicKey);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusinessPaymentModel model = parseBusinessPaymentModel();
        String merchantPublicKey = parseMerchantPublicKey();
        if (model != null) {
            initializeView(model);
        } else {
            throw new IllegalStateException("BusinessPayment can't be loaded");
        }
        Tracker.trackScreen(SCREEN_ID_PAYMENT_RESULT_BUSINESS, SCREEN_NAME_PAYMENT_RESULT, merchantPublicKey, getApplicationContext());
    }

    @Nullable
    private String parseMerchantPublicKey() {
        return getIntent().getExtras() != null ? getIntent()
                .getExtras()
                .getString(EXTRA_MERCHANT_PUBLIC_KEY) : null;
    }

    @Nullable
    private BusinessPaymentModel parseBusinessPaymentModel() {
        return getIntent().getExtras() != null ? (BusinessPaymentModel) getIntent()
                .getExtras()
                .getParcelable(EXTRA_BUSINESS_PAYMENT_MODEL) : null;
    }

    private void initializeView(final BusinessPaymentModel model) {
        BusinessPaymentContainer businessPaymentContainer = new BusinessPaymentContainer(new BusinessPaymentContainer.Props(model.payment, model.getPaymentMethodProps()), this);
        ComponentManager componentManager = new ComponentManager(this);
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

    private void processCustomExit(final ExitAction action) {
        Intent intent = action.toIntent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
