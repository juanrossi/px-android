package com.mercadopago.android.px.uicontrollers;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mreverter on 30/4/16.
 */
public interface CustomViewController {
    void initializeControls();

    View inflateInParent(ViewGroup parent, boolean attachToRoot);

    View getView();
}
