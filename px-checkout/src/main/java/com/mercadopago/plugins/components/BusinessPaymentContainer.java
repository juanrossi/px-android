package com.mercadopago.plugins.components;


import android.support.annotation.NonNull;

import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.PaymentMethodComponent;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.plugins.model.BusinessPayment;

public class BusinessPaymentContainer extends Component<BusinessPaymentContainer.Props, Void> {

    static {
        RendererFactory.register(BusinessPaymentContainer.class, BusinessPaymentRenderer.class);
    }

    public static class Props {

        /* default */ final BusinessPayment payment;
        /* default */ final PaymentMethodComponent.PaymentMethodProps paymentMethod;

        public Props(@NonNull final BusinessPayment payment,
                     @NonNull final PaymentMethodComponent.PaymentMethodProps paymentMethod) {
            this.payment = payment;
            this.paymentMethod = paymentMethod;
        }
    }

    public BusinessPaymentContainer(@NonNull final BusinessPaymentContainer.Props props,
                                    @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }
}
