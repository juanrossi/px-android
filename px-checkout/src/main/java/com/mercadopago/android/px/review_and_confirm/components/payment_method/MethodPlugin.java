package com.mercadopago.android.px.review_and_confirm.components.payment_method;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.components.CompactComponent;
import com.mercadopago.android.px.internal.datasource.PluginService;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.plugins.model.PaymentMethodInfo;
import com.mercadopago.android.px.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.util.ViewUtils;

class MethodPlugin extends CompactComponent<MethodPlugin.Props, Void> {

    /* default */ static class Props {

        /* default */ private final String paymentMethodId;

        /* default */ Props(final String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }

        /* default */
        static Props createFrom(final PaymentModel props) {
            return new Props(props.paymentMethodId);
        }
    }

    /* default */ MethodPlugin(final Props props) {
        super(props);
    }

    @Override
    public View render(@NonNull final ViewGroup parent) {

        final View paymentView = inflate(parent, R.layout.px_payment_method_plugin);
        final ImageView imageView = paymentView.findViewById(R.id.icon);
        final TextView title = paymentView.findViewById(R.id.title);
        final TextView description = paymentView.findViewById(R.id.description);
        final PluginRepository pluginService = new PluginService(parent.getContext());
        final PaymentMethodInfo pluginInfo = pluginService.getPaymentMethodInfo(props.paymentMethodId);
        ViewUtils.loadOrGone(pluginInfo.getDescription(), description);
        ViewUtils.loadOrGone(pluginInfo.getName(), title);
        imageView.setImageResource(pluginInfo.icon);

        return paymentView;
    }
}
