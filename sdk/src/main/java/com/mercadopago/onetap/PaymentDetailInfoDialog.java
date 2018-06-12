package com.mercadopago.onetap;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.R;
import com.mercadopago.onetap.components.PaymentDetailContainer;
import com.mercadopago.viewmodel.OneTapModel;

public class PaymentDetailInfoDialog extends MeliDialog {

    private static final String TAG = PaymentDetailInfoDialog.class.getName();
    private static final String ARG_ONETAP_MODEL = "arg_onetap_model";
    private OneTapModel oneTapModel;

    public static void showDialog(@NonNull final FragmentManager fragmentManager,
        @NonNull final OneTapModel oneTapModel) {
        final PaymentDetailInfoDialog paymentDetailInfoDialog = new PaymentDetailInfoDialog();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ONETAP_MODEL, oneTapModel);
        paymentDetailInfoDialog.setArguments(bundle);
        paymentDetailInfoDialog.show(fragmentManager, TAG);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oneTapModel = (OneTapModel) getArguments().getSerializable(ARG_ONETAP_MODEL);
        PaymentDetailContainer container = new PaymentDetailContainer(oneTapModel);
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
