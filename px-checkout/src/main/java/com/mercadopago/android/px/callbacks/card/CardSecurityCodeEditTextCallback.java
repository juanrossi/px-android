package com.mercadopago.android.px.callbacks.card;

/**
 * Created by vaserber on 10/20/16.
 */

public interface CardSecurityCodeEditTextCallback {
    void checkOpenKeyboard();
    void saveSecurityCode(CharSequence s);
    void changeErrorView();
    void toggleLineColorOnError(boolean toggle);
}
