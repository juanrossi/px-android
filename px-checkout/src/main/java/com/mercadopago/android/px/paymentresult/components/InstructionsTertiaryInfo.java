package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.paymentresult.props.InstructionsTertiaryInfoProps;

/**
 * Created by vaserber on 11/14/17.
 */

public class InstructionsTertiaryInfo extends Component<InstructionsTertiaryInfoProps, Void> {

    public InstructionsTertiaryInfo(@NonNull final InstructionsTertiaryInfoProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }
}
