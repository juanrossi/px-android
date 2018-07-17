package com.mercadopago.android.px.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.model.InstructionReference;
import com.mercadopago.android.px.paymentresult.props.InstructionsReferencesProps;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vaserber on 11/13/17.
 */

public class InstructionsReferences extends Component<InstructionsReferencesProps, Void> {

    public InstructionsReferences(@NonNull final InstructionsReferencesProps props,
        @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    public List<InstructionReferenceComponent> getReferenceComponents() {
        List<InstructionReferenceComponent> componentList = new ArrayList<>();

        for (InstructionReference reference : props.references) {
            final InstructionReferenceComponent.Props referenceProps = new InstructionReferenceComponent.Props.Builder()
                .setReference(reference)
                .build();

            final InstructionReferenceComponent component =
                new InstructionReferenceComponent(referenceProps, getDispatcher());

            componentList.add(component);
        }

        return componentList;
    }
}
