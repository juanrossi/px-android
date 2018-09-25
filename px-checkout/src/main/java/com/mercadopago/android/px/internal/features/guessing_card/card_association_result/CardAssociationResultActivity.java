package com.mercadopago.android.px.internal.features.guessing_card.card_association_result;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;

public class CardAssociationResultActivity extends AppCompatActivity {

    public static final String IS_ERROR = "isError";
    public static final String RESULT_KEY = "action";
    public static final String RESULT_ACTION_RETRY = "retry";
    public static final String RESULT_ACTION_EXIT = "exit";

    public static void startCardAssociationResultActivity(final Activity callerActivity,
        final int requestCode, final boolean isError) {
        final Intent intent = new Intent(callerActivity, CardAssociationResultActivity.class);
        intent.putExtra(IS_ERROR, isError);
        callerActivity
            .startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final boolean isError = intent.getBooleanExtra(IS_ERROR, true);

        if (isError) {
            setContentView(R.layout.px_card_association_result_error);
        } else {
            setContentView(R.layout.px_card_association_result_success);
        }

        setupStatusBarColor(isError);

        final MeliButton retryButton = findViewById(R.id.mpsdkCardAssociationResultRetryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                goBackWithResult(RESULT_ACTION_RETRY);
            }
        });

        final MeliButton exitButton = findViewById(R.id.mpsdkCardAssociationResultExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                goBackWithResult(RESULT_ACTION_EXIT);
            }
        });
    }

    void goBackWithResult(final String action) {
        final Intent data = new Intent();
        data.putExtra(RESULT_KEY, action);
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.px_no_change_animation, R.anim.px_slide_down_activity);
    }

    private void setupStatusBarColor(final boolean isError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int color = isError ? R.color.px_orange_status_bar : R.color.px_green_status_bar;
            final int compatColor = ContextCompat.getColor(this, color);
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(compatColor);
        }
    }
}
