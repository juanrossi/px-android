package com.mercadopago.android.px.providers;

import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.mvp.ResourcesProvider;
import com.mercadopago.android.px.mvp.TaggedCallback;

/**
 * Created by mromar on 4/11/17.
 */

public interface CustomerCardsProvider extends ResourcesProvider {

    void getCustomer(TaggedCallback<Customer> taggedCallback);

    String getLastDigitsLabel();

    String getConfirmPromptYes();

    String getConfirmPromptNo();

    int getIconDialogAlert();
}
