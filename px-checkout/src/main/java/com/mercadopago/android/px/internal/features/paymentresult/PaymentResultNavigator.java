package com.mercadopago.android.px.internal.features.paymentresult;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.model.exceptions.ApiException;

/**
 * Created by vaserber on 10/27/17.
 */

public interface PaymentResultNavigator {

    void showApiExceptionError(ApiException exception, String requestOrigin);

    void showError(MercadoPagoError error, String requestOrigin);

    void openLink(String url);

    void finishWithResult(final int resultCode);

    void changePaymentMethod();

    void recoverPayment(@NonNull final PostPaymentAction.OriginAction originAction);

    void trackScreen(ScreenViewEvent event);
}
