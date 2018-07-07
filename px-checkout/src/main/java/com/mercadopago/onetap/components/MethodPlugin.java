package com.mercadopago.onetap.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.android.px.components.CompactComponent;
import com.mercadopago.internal.datasource.PluginService;
import com.mercadopago.internal.repository.PluginRepository;
import com.mercadopago.plugins.model.PaymentMethodInfo;
import com.mercadopago.util.ViewUtils;
import javax.annotation.Nonnull;

class MethodPlugin extends CompactComponent<MethodPlugin.Props, Void> {

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

    /* default */ MethodPlugin(final Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View main = inflate(parent, R.layout.mpsdk_payment_method_plugin_compact);
        final PluginRepository pluginService = new PluginService(parent.getContext());
        final PaymentMethodInfo pluginInfo = pluginService.getPaymentMethodInfo(props.paymentMethodId);
        final ImageView logo = main.findViewById(R.id.icon);
        final TextView name = main.findViewById(R.id.name);
        final TextView description = main.findViewById(R.id.description);
        logo.setImageResource(pluginInfo.icon);
        ViewUtils.loadOrGone(pluginInfo.getName(), name);
        ViewUtils.loadOrGone(pluginInfo.getDescription(), description);

        return main;
    }
}
