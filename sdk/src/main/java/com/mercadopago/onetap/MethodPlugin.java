package com.mercadopago.onetap;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.core.CheckoutStore;
import com.mercadopago.plugins.model.PaymentMethodInfo;
import javax.annotation.Nonnull;

class MethodPlugin extends CompactComponent<MethodPlugin.Props, OneTap.Actions> {

    /* default */ static class Props {

        /* default */ @NonNull final String paymentMethodId;

        /* default */ Props(@NonNull final String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }

        /* default */
        static Props createFrom(final CompactPaymentMethod.Props props) {
            return new Props(props.paymentMethodId);
        }
    }

    /* default */ MethodPlugin(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        //TODO refactor - bad way to get the icon.
        final PaymentMethodInfo pluginInfo =
            CheckoutStore.getInstance().getPaymentMethodPluginInfoById(props.paymentMethodId, parent.getContext());
        final View main = inflate(parent, R.layout.mpsdk_payment_method_plugin_compact);
        final ImageView logo = main.findViewById(R.id.icon);
        final TextView name = main.findViewById(R.id.name);
        logo.setImageResource(pluginInfo.icon);
        name.setText(pluginInfo.name);
        return main;
    }
}
