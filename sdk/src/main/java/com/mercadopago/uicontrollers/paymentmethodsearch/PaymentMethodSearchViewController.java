package com.mercadopago.uicontrollers.paymentmethodsearch;

import android.view.View;

import com.mercadopago.uicontrollers.CustomViewController;

public interface PaymentMethodSearchViewController extends CustomViewController {
    void draw();
    void setOnClickListener(View.OnClickListener listener);
}
