package com.mercadopago.review_and_confirm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mercadopago.TermsAndConditionsActivity;
import com.mercadopago.components.Action;
import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.ComponentManager;
import com.mercadopago.mvp.MvpView;
import com.mercadopago.review_and_confirm.actions.TermsAndConditionAction;
import com.mercadopago.review_and_confirm.components.ReviewAndConfirmContainer;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;

public class ReviewAndConfirmActivity extends AppCompatActivity implements ActionDispatcher, MvpView {

    private static final String EXTRA_TERMS_AND_CONDITIONS = "extra_terms_and_conditions";

    public static void start(final Context context, final TermsAndConditionsModel termsAndConditions) {
        Intent intent = new Intent(context, ReviewAndConfirmActivity.class);
        intent.putExtra(EXTRA_TERMS_AND_CONDITIONS, termsAndConditions);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ReviewAndConfirmContainer.Props props = getActivityParameters();
        final ComponentManager manager = new ComponentManager(this);
        final ReviewAndConfirmContainer container = new ReviewAndConfirmContainer(props);

        container.setDispatcher(this);
        manager.render(container);

    }

    private ReviewAndConfirmContainer.Props getActivityParameters() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        TermsAndConditionsModel termsAndConditionsModel = null;
        if (extras != null) {
            termsAndConditionsModel = extras.getParcelable(EXTRA_TERMS_AND_CONDITIONS);
        }
        return new ReviewAndConfirmContainer.Props(termsAndConditionsModel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void dispatch(Action action) {
        if (action instanceof TermsAndConditionAction) {
            TermsAndConditionsActivity.start(this, ((TermsAndConditionAction) action).getSiteId());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
