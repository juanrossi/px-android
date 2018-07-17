package com.mercadopago.android.px.callbacks;

import com.mercadopago.android.px.exceptions.MercadoPagoError;

/**
 * Created by vaserber on 1/20/17.
 */

public interface ReturnCallback {
    void onCancel();

    void onFailure(MercadoPagoError error);
}
