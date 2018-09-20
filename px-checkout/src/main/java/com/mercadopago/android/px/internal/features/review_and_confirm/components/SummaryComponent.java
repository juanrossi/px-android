package com.mercadopago.android.px.internal.features.review_and_confirm.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.RendererFactory;

public class SummaryComponent extends Component<SummaryComponent.SummaryProps, Void> {

    static {
        RendererFactory.register(SummaryComponent.class, SummaryRenderer.class);
    }

    /* default */ static final class SummaryProps {
        final SummaryModel summaryModel;
        final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration;

        private SummaryProps(final SummaryModel summaryModel,
            final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration) {
            this.summaryModel = summaryModel;
            this.reviewAndConfirmConfiguration = reviewAndConfirmConfiguration;
        }

        static SummaryProps createFrom(final SummaryModel summaryModel,
            final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration) {
            return new SummaryProps(summaryModel, reviewAndConfirmConfiguration);
        }
    }

    /* default */ SummaryComponent(@NonNull final SummaryComponent.SummaryProps props) {
        super(props);
    }

    /* default */  FullSummary getFullSummary() {
        return new FullSummary(props);
    }

    /* default */ CompactSummary getCompactSummary() {
        return new CompactSummary(props.summaryModel);
    }
}
