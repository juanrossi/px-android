package com.mercadopago.android.px.uicontrollers.installments;

import android.view.View;
import com.mercadopago.android.px.uicontrollers.CustomViewController;

/**
 * Created by mromar on 2/4/17.
 */

public interface InstallmentsView extends CustomViewController {

    void draw();

    void setOnClickListener(View.OnClickListener listener);
}
