package com.mercadopago.android.px.core.internal;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

/**
 * Provides access to card storage flow
 */
public final class MercadoPagoCardStorage {

    private MercadoPagoCardStorage() {
    }

    /**
     * Starts card storage flow
     * <p>
     * The flow can end with a success, returning {@link Activity#RESULT_OK}, as well as the
     * resulting {@code cardId} in the intent as {@code String cardId},
     * <p>
     * In case the flow experiences an error, a {@link MercadoPagoError}
     * will be returned.
     *
     * @param callerActivity the activity that calls the flow
     * @param accessToken logged in user access token
     * @param requestCode it's the number that identifies the checkout flow request for
     * {@link Activity#onActivityResult(int, int, Intent)}
     */
    public static void startCardStorageFlow(@NonNull final Activity callerActivity, @NonNull final String accessToken,
        final int requestCode) {
        GuessingCardActivity.startGuessingCardActivityForStorage(callerActivity, accessToken, requestCode);
    }
}
