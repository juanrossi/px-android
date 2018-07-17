package com.mercadopago.android.px.providers;

import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.mvp.ResourcesProvider;
import com.mercadopago.android.px.mvp.TaggedCallback;
import java.util.List;

/**
 * Created by mreverter on 1/5/17.
 */

public interface PaymentMethodsProvider extends ResourcesProvider {
    void getPaymentMethods(TaggedCallback<List<PaymentMethod>> resourcesRetrievedCallback);
}
