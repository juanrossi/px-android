package com.mercadopago.android.px.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Map;

public class CustomComponent extends Component<CustomComponent.Props, Void> {

    public CustomComponent(@NonNull final Props props) {
        super(props);
    }

    public static class Props {

        public final Map<String, Object> data;
        public final CheckoutPreference checkoutPreference;

        public Props(Map<String, Object> data, CheckoutPreference checkoutPreference) {
            this.data = data;
            this.checkoutPreference = checkoutPreference;
        }
    }
}
