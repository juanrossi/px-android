package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.paymentresult.props.InstructionsSubtitleProps;

/**
 * Created by vaserber on 11/13/17.
 */

public class InstructionsSubtitle extends Component<InstructionsSubtitleProps, Void> {

    public InstructionsSubtitle(@NonNull final InstructionsSubtitleProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }
}
