package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.paymentresult.props.IconProps;
import com.mercadopago.android.px.util.TextUtils;

/**
 * Created by vaserber on 10/23/17.
 */

public class Icon extends Component<IconProps, Void> {

    static {
        RendererFactory.register(Icon.class, IconRenderer.class);
    }

    public Icon(@NonNull final IconProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    public boolean hasIconFromUrl() {
        return !TextUtils.isEmpty(props.iconUrl);
    }
}
