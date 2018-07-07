package com.mercadopago.android.px.paymentresult.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.components.Renderer;
import com.mercadopago.android.px.customviews.MPTextView;

/**
 * Created by vaserber on 11/13/17.
 */

public class AccreditationCommentRenderer extends Renderer<AccreditationComment> {

    @Override
    public View render(final AccreditationComment component, final Context context, final ViewGroup parent) {
        final View accreditationCommentView = inflate(R.layout.px_accreditation_comment, parent);
        final MPTextView commentTextView = accreditationCommentView.findViewById(R.id.mpsdkAccreditationTimeComment);
        setText(commentTextView, component.props.comment);
        return accreditationCommentView;
    }
}
