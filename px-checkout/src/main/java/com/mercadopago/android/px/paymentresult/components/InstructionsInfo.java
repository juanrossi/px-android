package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.paymentresult.props.InstructionsInfoProps;

/**
 * Created by vaserber on 11/13/17.
 */

public class InstructionsInfo extends Component<InstructionsInfoProps, Void> {

    public InstructionsInfo(@NonNull final InstructionsInfoProps props, @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }
}
