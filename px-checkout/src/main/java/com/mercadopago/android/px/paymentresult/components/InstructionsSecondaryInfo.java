package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.paymentresult.props.InstructionsSecondaryInfoProps;

/**
 * Created by vaserber on 11/14/17.
 */

public class InstructionsSecondaryInfo extends Component<InstructionsSecondaryInfoProps, Void> {

    public InstructionsSecondaryInfo(@NonNull final InstructionsSecondaryInfoProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }
}
