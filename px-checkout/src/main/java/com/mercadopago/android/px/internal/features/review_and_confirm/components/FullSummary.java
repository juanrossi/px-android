package com.mercadopago.android.px.internal.features.review_and_confirm.components;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.review_and_confirm.props.AmountDescriptionProps;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.internal.viewmodel.Summary;
import com.mercadopago.android.px.internal.viewmodel.SummaryDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mercadopago.android.px.internal.util.TextUtil.isNotEmpty;

public class FullSummary extends Component<SummaryComponent.SummaryProps, Void> {

    static {
        RendererFactory.register(FullSummary.class, FullSummaryRenderer.class);
    }

    /* default */ FullSummary(@NonNull final SummaryComponent.SummaryProps props) {
        super(props);
    }

    public List<AmountDescription> getAmountDescriptionComponents(@NonNull final Context context) {
        final List<AmountDescription> amountDescriptionList = new ArrayList<>();

        for (final SummaryDetail summaryDetail : getSummary(context).getSummaryDetails()) {
            final AmountDescriptionProps amountDescriptionProps = new AmountDescriptionProps(
                summaryDetail.getTotalAmount(),
                summaryDetail.getTitle(),
                props.summaryModel.currencyId,
                summaryDetail.getTextColor(),
                summaryDetail.getSummaryItemType());

            amountDescriptionList.add(new AmountDescription(amountDescriptionProps));
        }

        return amountDescriptionList;
    }

    /* default */
    @VisibleForTesting
    Summary getSummary(@NonNull final Context context) {

        final int defaultTextColor = ContextCompat.getColor(context, R.color.px_summary_text_color);

        final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration = props.reviewAndConfirmConfiguration;
        final Summary.Builder summaryBuilder = new Summary.Builder();

        if (isPrefAmountDifferent(props.summaryModel.getCharges()) &&
            reviewAndConfirmConfiguration.hasProductAmount()) {
            summaryBuilder.addSummaryProductDetail(reviewAndConfirmConfiguration.getProductAmount(), getItemTitle(),
                defaultTextColor)

                .addSummaryShippingDetail(reviewAndConfirmConfiguration.getShippingAmount(),
                    context.getString(R.string.px_review_summary_shipping), defaultTextColor)

                .addSummaryArrearsDetail(reviewAndConfirmConfiguration.getArrearsAmount(),
                    context.getString(R.string.px_review_summary_arrear),
                    defaultTextColor)

                .addSummaryTaxesDetail(reviewAndConfirmConfiguration.getTaxesAmount(),
                    context.getString(R.string.px_review_summary_taxes),
                    defaultTextColor)

                .addSummaryDiscountDetail(getDiscountAmount(),
                    context.getString(R.string.px_review_summary_discount),
                    ContextCompat.getColor(context, R.color.px_discount_description))

                .setDisclaimerText(reviewAndConfirmConfiguration.getDisclaimerText())
                .setDisclaimerColor(getDisclaimerTextColor(context));
        } else {
            summaryBuilder.addSummaryProductDetail(props.summaryModel.getItemsAmount(), getItemTitle(),
                defaultTextColor);

            if (!TextUtil.isEmpty(reviewAndConfirmConfiguration.getDisclaimerText())) {
                summaryBuilder.setDisclaimerText(reviewAndConfirmConfiguration.getDisclaimerText())
                    .setDisclaimerColor(getDisclaimerTextColor(context));
            }

            if (isValidAmount(props.summaryModel.getCouponAmount())) {
                summaryBuilder.addSummaryDiscountDetail(props.summaryModel.getCouponAmount(),
                    context.getString(R.string.px_review_summary_discount),
                    ContextCompat.getColor(context, R.color.px_discount_description));
            }
        }

        if (props.summaryModel.hasCharges()) {
            summaryBuilder.addSummaryChargeDetail(props.summaryModel.getCharges(),
                context.getString(R.string.px_review_summary_charges),
                defaultTextColor);
        }

        return summaryBuilder.build();
    }

    private int getDisclaimerTextColor(@NonNull final Context context) {
        final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
            Session.getSession(context).getConfigurationModule().getPaymentSettings().getAdvancedConfiguration()
                .getReviewAndConfirmConfiguration();
        if (TextUtil.isEmpty(reviewAndConfirmConfiguration.getDisclaimerTextColor())) {
            return ContextCompat.getColor(context, R.color.px_default_disclaimer);
        } else {
            return Color.parseColor(reviewAndConfirmConfiguration.getDisclaimerTextColor());
        }
    }

    private String getItemTitle() {
        String title = props.summaryModel.title;

        if (isNotEmpty(props.reviewAndConfirmConfiguration.getProductTitle())) {
            title = props.reviewAndConfirmConfiguration.getProductTitle();
        }

        return title;
    }

    private BigDecimal getDiscountAmount() {
        final ReviewAndConfirmConfiguration reviewScreenPreference = props.reviewAndConfirmConfiguration;
        BigDecimal discountAmount = reviewScreenPreference.getDiscountAmount();

        if (isValidAmount(props.summaryModel.getCouponAmount())) {
            discountAmount = discountAmount.add(props.summaryModel.getCouponAmount());
        }

        return discountAmount;
    }

    private boolean isValidAmount(final BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) != 0;
    }

    private boolean isPrefAmountDifferent(final BigDecimal charges) {
        final ReviewAndConfirmConfiguration reviewScreenPreference = props.reviewAndConfirmConfiguration;
        final BigDecimal partialCharges = charges != null ? charges : BigDecimal.ZERO;
        final BigDecimal totalAmountPreference = reviewScreenPreference.getTotalAmount().add(partialCharges);
        return totalAmountPreference.compareTo(props.summaryModel.getAmountToPay()) == 0;
    }

    public DisclaimerComponent getDisclaimerComponent(String disclaimer) {
        DisclaimerComponent.Props props = new DisclaimerComponent.Props(disclaimer);
        return new DisclaimerComponent(props);
    }
}