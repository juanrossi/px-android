package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class SavedESCCardToken extends SavedCardToken {

    // used in network call
    @SuppressWarnings("unused")
    @SerializedName("require_esc")
    private final boolean requireEsc;

    // used in network call
    @SuppressWarnings("unused")
    private String esc;

    private SavedESCCardToken(String cardId, String securityCode, String esc) {
        super(cardId, securityCode);
        this.requireEsc = true;
        this.esc = esc;
    }

    public static SavedESCCardToken createWithSecurityCode(@NonNull String cardId, @NonNull final String securityCode) {
        return new SavedESCCardToken(cardId, securityCode, "");
    }

    public static SavedESCCardToken createWithEsc(@NonNull final String cardId, @NonNull final String esc) {
        return new SavedESCCardToken(cardId, "", esc);
    }
}
