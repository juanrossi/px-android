package com.mercadopago.paymentresult.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.R;
import com.mercadopago.android.px.components.Renderer;
import com.mercadopago.customviews.MPTextView;

import java.util.List;

/**
 * Created by vaserber on 11/14/17.
 */

public class InstructionsSecondaryInfoRenderer extends Renderer<InstructionsSecondaryInfo> {

    @Override
    public View render(final InstructionsSecondaryInfo component, final Context context, final ViewGroup parent) {
        final View secondaryInfoView = inflate(R.layout.mpsdk_payment_result_instructions_secondary_info, parent);
        final MPTextView secondaryInfoTextView = secondaryInfoView.findViewById(R.id.msdpkSecondaryInfo);

        setText(secondaryInfoTextView, getSecondaryInfoText(component.props.secondaryInfo));
        return secondaryInfoView;
    }

    private String getSecondaryInfoText(List<String> secondaryInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < secondaryInfo.size(); i++) {
            stringBuilder.append(secondaryInfo.get(i));
            if ( i != secondaryInfo.size() - 1) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
