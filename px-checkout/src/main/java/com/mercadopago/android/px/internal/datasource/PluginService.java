package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.CheckoutStore;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.plugins.PaymentMethodPlugin;
import com.mercadopago.android.px.plugins.model.PaymentMethodInfo;
import java.util.List;

public class PluginService implements PluginRepository {

    @NonNull private final Context context;
    private final List<PaymentMethodPlugin> all;

    public PluginService(@NonNull final Context context) {
        this.context = context;
        //TODO remove
        all = CheckoutStore.getInstance().getPaymentMethodPluginList();
    }

    @Override
    @NonNull
    public PaymentMethodPlugin getPlugin(@NonNull final String pluginId) {
        for (final PaymentMethodPlugin plugin : all) {
            if (plugin.getId().equalsIgnoreCase(pluginId)) {
                return plugin;
            }
        }
        throw new IllegalStateException("there is no plugin with id " + pluginId);
    }

    @Override
    @NonNull
    public PaymentMethod getPluginAsPaymentMethod(@NonNull final String pluginId, @NonNull final String paymentType) {
        final PaymentMethodPlugin plugin = getPlugin(pluginId);
        final PaymentMethodInfo paymentInfo = getPaymentMethodInfo(plugin);
        return new PaymentMethod(paymentInfo.getId(), paymentInfo.getName(), paymentType);
    }

    @Override
    @NonNull
    public PaymentMethodInfo getPaymentMethodInfo(final PaymentMethodPlugin plugin) {
        return plugin.getPaymentMethodInfo(context);
    }

    @NonNull
    @Override
    public PaymentMethodInfo getPaymentMethodInfo(@NonNull final String pluginId) {
        return getPaymentMethodInfo(getPlugin(pluginId));
    }
}
