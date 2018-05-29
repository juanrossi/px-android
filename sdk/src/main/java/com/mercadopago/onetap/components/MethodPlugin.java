package com.mercadopago.onetap.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.util.ResourceUtil;
import javax.annotation.Nonnull;

class MethodPlugin extends CompactComponent<MethodPlugin.Props, OneTap.Actions> {

    /* default */ static class Props {

        /* default */ @NonNull final String paymentMethodId;

        /* default */ Props(@NonNull final String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }

        /* default */
        static Props createFrom(final PaymentMethod.Props props) {
            return new Props(props.paymentMethodId);
        }
    }

    /* default */ MethodPlugin(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final Context context = parent.getContext();
        final int iconResource = ResourceUtil.getIconResource(context, props.paymentMethodId);
        final String resName = ResourceUtil.getPluginName(context, props.paymentMethodId);
        final View main = inflate(parent, R.layout.mpsdk_payment_method_plugin_compact);
        final ImageView logo = main.findViewById(R.id.icon);
        final TextView name = main.findViewById(R.id.name);
        logo.setImageResource(iconResource);
        name.setText(resName);
        return main;
    }
}
