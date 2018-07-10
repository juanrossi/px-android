package com.mercadopago.review_and_confirm.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.R;
import com.mercadopago.components.Renderer;
import com.mercadopago.components.RendererFactory;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.lite.util.CurrenciesUtil;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.review_and_confirm.models.SummaryModel;

import static com.mercadopago.util.TextUtils.isEmpty;

public class CompactSummaryRenderer extends Renderer<CompactSummary> {

    @Override
    public View render(@NonNull final CompactSummary component, @NonNull final Context context, final ViewGroup parent) {
        final View summaryView = inflate(R.layout.mpsdk_compact_summary_component, parent);
        final MPTextView totalAmountTextView = summaryView.findViewById(R.id.mpsdkTotalAmount);
        final MPTextView itemTitleTextView = summaryView.findViewById(R.id.mpsdkItemTitle);
        final LinearLayout disclaimerLinearLayout = summaryView.findViewById(R.id.disclaimer);

        setText(totalAmountTextView, CurrenciesUtil
            .getSpannedAmountWithCurrencySymbol(component.props.getAmountToPay(), component.props.currencyId));
        setText(itemTitleTextView, getItemTitle(component.props.title, context));

        if (shouldShowCftDisclaimer(component.props)) {
            final String disclaimer = getDisclaimer(component, context);
            final Renderer disclaimerRenderer = RendererFactory.create(context, component.getDisclaimerComponent(disclaimer));
            final View disclaimerView = disclaimerRenderer.render();
            disclaimerLinearLayout.addView(disclaimerView);
        }

        return summaryView;
    }

    @VisibleForTesting
    boolean shouldShowCftDisclaimer(final SummaryModel props) {
        return PaymentTypes.isCreditCardPaymentType(props.getPaymentTypeId())
                && !isEmpty(props.getCftPercent());
    }


    private String getItemTitle(String itemTitle, Context context) {
        return isEmpty(itemTitle) ? getDefaultTitle(context) : itemTitle;
    }

    private String getDefaultTitle(Context context) {
        return context.getString(R.string.mpsdk_review_summary_product);
    }

    private String getDisclaimer(CompactSummary component, Context context) {
        return context.getString(R.string.mpsdk_installments_cft, component.props.getCftPercent());
    }
}
