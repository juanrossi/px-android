package com.mercadopago.android.px.review_and_confirm.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.review_and_confirm.models.SummaryModel;

class CompactSummary extends Component<SummaryModel, Void> {

    static {
        RendererFactory.register(CompactSummary.class, CompactSummaryRenderer.class);
    }

    CompactSummary(@NonNull final SummaryModel props) {
        super(props);
    }

    DisclaimerComponent getDisclaimerComponent(final String disclaimer) {
        final DisclaimerComponent.Props props = new DisclaimerComponent.Props(disclaimer);
        return new DisclaimerComponent(props);
    }
}
