package com.mercadopago.onetap;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.R;
import com.mercadopago.internal.di.ConfigurationModule;
import com.mercadopago.onetap.components.PaymentDetailContainer;

public class PaymentDetailInfoDialog extends MeliDialog {

    private static final String TAG = PaymentDetailInfoDialog.class.getName();

    public static void showDialog(@NonNull final FragmentManager fragmentManager) {
        final PaymentDetailInfoDialog paymentDetailInfoDialog = new PaymentDetailInfoDialog();
        final Bundle bundle = new Bundle();
        paymentDetailInfoDialog.setArguments(bundle);
        paymentDetailInfoDialog.show(fragmentManager, TAG);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PaymentDetailContainer container =
            new PaymentDetailContainer(new ConfigurationModule(view.getContext()).getConfiguration());
        container.render((ViewGroup) view.findViewById(R.id.main_container));
    }

    @Override
    public int getContentView() {
        return R.layout.mpsdk_onetap_fragment_dialog;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getContext().getString(R.string.mpsdk_payment_detail_title_modal);
    }

}
