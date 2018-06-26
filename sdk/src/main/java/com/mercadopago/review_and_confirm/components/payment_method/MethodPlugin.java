package com.mercadopago.review_and_confirm.components.payment_method;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.internal.datasource.PluginService;
import com.mercadopago.internal.repository.PluginRepository;
import com.mercadopago.plugins.model.PaymentMethodInfo;
import com.mercadopago.review_and_confirm.models.PaymentModel;
import com.mercadopago.util.ViewUtils;

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

        final View paymentView = inflate(parent, R.layout.mpsdk_payment_method_plugin);
        final ImageView imageView = paymentView.findViewById(R.id.icon);
        final TextView title = paymentView.findViewById(R.id.title);
        final TextView description = paymentView.findViewById(R.id.description);
        final PluginRepository pluginService = new PluginService(parent.getContext());
        final PaymentMethodInfo pluginInfo = pluginService.getPaymentMethodInfo(props.paymentMethodId);
        ViewUtils.loadOrGone(pluginInfo.description, description);
        ViewUtils.loadOrGone(pluginInfo.name, title);
        imageView.setImageResource(pluginInfo.icon);

        return paymentView;
    }
}
