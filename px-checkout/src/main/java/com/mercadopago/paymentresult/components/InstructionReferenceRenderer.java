package com.mercadopago.paymentresult.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.R;
import com.mercadopago.android.px.components.Renderer;
import com.mercadopago.customviews.MPTextView;

/**
 * Created by vaserber on 11/13/17.
 */

public class InstructionReferenceRenderer extends Renderer<InstructionReferenceComponent> {

    @Override
    public View render(final InstructionReferenceComponent component, final Context context, final ViewGroup parent) {
        final View referenceView = inflate(R.layout.mpsdk_payment_result_instruction_reference, parent);
        final MPTextView labelTextView = referenceView.findViewById(R.id.mpsdkReferenceLabel);
        final MPTextView valueTextView = referenceView.findViewById(R.id.mpsdkReferenceValue);
        final MPTextView commentTextView = referenceView.findViewById(R.id.mpsdkReferenceComment);

        setText(labelTextView, component.props.reference.getLabel());

        String formattedReference = component.props.reference.getFormattedReference();
        setText(valueTextView, formattedReference);

        setText(commentTextView, component.props.reference.getComment());

        return referenceView;
    }
}
