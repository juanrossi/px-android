package com.mercadopago.android.px.providers;

import com.mercadopago.android.px.mvp.ResourcesProvider;

/**
 * Created by vaserber on 8/17/17.
 */

public interface ReviewPaymentMethodsProvider extends ResourcesProvider {

    String getEmptyPaymentMethodsListError();

    String getStandardErrorMessage();
}
