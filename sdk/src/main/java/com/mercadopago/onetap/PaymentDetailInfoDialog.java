package com.mercadopago.onetap;

import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.R;

public class PaymentDetailInfoDialog extends MeliDialog {
    @Override
    public int getContentView() {
        return R.layout.mpsdk_onetap_fragment_dialog;
    }
}
