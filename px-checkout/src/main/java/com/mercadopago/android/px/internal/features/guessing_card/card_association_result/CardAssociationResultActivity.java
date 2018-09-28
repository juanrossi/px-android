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
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;

public class CardAssociationResultActivity extends AppCompatActivity {

    public static final String PARAM_IS_ERROR = "isError";
    public static final String PARAM_ACCESS_TOKEN = "accessToken";
    /* default */ String accessToken;
    private boolean isError;

    public static void startCardAssociationResultActivity(final Activity callerActivity, final boolean isError,
        final String accessToken) {
        final Intent intent = new Intent(callerActivity, CardAssociationResultActivity.class);
        intent.putExtra(PARAM_IS_ERROR, isError);
        intent.putExtra(PARAM_ACCESS_TOKEN, accessToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        callerActivity.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        isError = intent.getBooleanExtra(PARAM_IS_ERROR, true);
        accessToken = intent.getStringExtra(PARAM_ACCESS_TOKEN);

        if (isError) {
            setContentView(R.layout.px_card_association_result_error);

            // Retry button is only present in error screen
            final MeliButton retryButton = findViewById(R.id.mpsdkCardAssociationResultRetryButton);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GuessingCardActivity.restartGuessingCardActivityForStorage(CardAssociationResultActivity.this,
                        accessToken);
                    finish();
                }
            });
        } else {
            setContentView(R.layout.px_card_association_result_success);
        }

        setupStatusBarColor(isError);

        final MeliButton exitButton = findViewById(R.id.mpsdkCardAssociationResultExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                returnToCaller();
            }
        });
    }

    void returnToCaller() {
        if (isError) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }

        finish();
        overridePendingTransition(R.anim.px_no_change_animation, R.anim.px_slide_right_to_left_out);
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

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PARAM_IS_ERROR, isError);
        outState.putString(PARAM_ACCESS_TOKEN, accessToken);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isError = savedInstanceState.getBoolean(PARAM_IS_ERROR);
            accessToken = savedInstanceState.getString(PARAM_ACCESS_TOKEN);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
