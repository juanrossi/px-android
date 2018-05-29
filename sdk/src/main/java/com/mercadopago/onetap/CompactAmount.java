package com.mercadopago.onetap;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.Discount;
import com.mercadopago.model.PayerCost;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.util.textformatter.TextFormatter;
import com.mercadopago.viewmodel.OneTapModel;
import java.math.BigDecimal;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class CompactAmount extends CompactComponent<CompactAmount.Props, OneTap.Actions> {

    static class Props {
        /* default */ @NonNull final BigDecimal amount;
        /* default */ @Nullable final Discount discount;
        /* default */ @NonNull final String currencyId;
        /* default */ @Nullable final PayerCost payerCost;
        /* default */ final boolean hasExtraAmount;

        /* default */ Props(final boolean hasExtraAmount, @NonNull final BigDecimal amount,
            @Nullable final Discount discount,
            @NonNull final String currencyId,
            @Nullable final PayerCost payerCost) {
            this.amount = amount;
            this.discount = discount;
            this.currencyId = currencyId;
            this.payerCost = payerCost;
            this.hasExtraAmount = hasExtraAmount;
        }

        /* default */
        static Props from(final OneTapModel props) {
            final CardPaymentMetadata card = props.paymentMethods.getOneTapMetadata().card;
            final PayerCost payerCost = card != null ? card.getAutoSelectedInstallment() : null;
            return new CompactAmount.Props(
                props.hasExtraAmount,
                props.checkoutPreference.getTotalAmount(), props.discount,
                props.checkoutPreference.getSite().getCurrencyId(), payerCost);
        }

        boolean hasExtrasAmount() {
            return hasExtraAmount;
        }

        boolean hasDiscount() {
            return discount != null;
        }
    }

    /* default */ CompactAmount(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View content = inflate(parent, R.layout.mpsdk_compact_amount);
        final ViewGroup discountLayout = content.findViewById(R.id.discount_detail_layout);
        resolveSmallAmountPlusDiscount(content);
        resolveBigAmount(content);
        resolveOffAmount(discountLayout);
        resolveArrow(content);
        return content;
    }

    private void resolveArrow(@NonNull final View content) {
        final View arrow = content.findViewById(R.id.arrow);
        boolean hasExtraDialog = hasExtraInfo();
        if (hasExtraDialog) {
            arrow.setVisibility(View.VISIBLE);
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    getActions().onAmountShowMore();
                }
            });
        } else {
            arrow.setVisibility(View.GONE);
            content.setClickable(false);
        }
    }

    private boolean hasExtraInfo() {
        return props.hasDiscount() || props.payerCost.hasMultipleInstallments() ||
            props.hasExtrasAmount();
    }

    private void resolveOffAmount(@NonNull final ViewGroup discountLayout) {
        final boolean hasDiscount = props.discount != null;
        final TextView discountMessage = discountLayout.findViewById(R.id.discount_message);
        ViewUtils.showOrGone(discountLayout, hasDiscount);

        if (hasDiscount && props.discount.hasPercentOff()) {
            TextFormatter.withCurrencyId(props.currencyId)
                .noSpace().noSymbol()
                .amount(props.discount.getPercentOff())
                .normalDecimals()
                .into(discountMessage)
                .holder(R.string.mpsdk_discount_percent_off_percent);
        } else if (hasDiscount) {
            TextFormatter.withCurrencyId(props.currencyId)
                .withSpace()
                .amount(props.discount.getCouponAmount())
                .normalDecimals()
                .into(discountMessage)
                .holder(R.string.mpsdk_discount_percent_off_amount);
        }

        ViewUtils.showOrGone(discountMessage, hasDiscount);
    }

    private void resolveSmallAmountPlusDiscount(final View content) {
        final TextView amount = content.findViewById(R.id.amount);
        TextFormatter.withCurrencyId(props.currencyId)
            .withSpace()
            .amount(props.amount)
            .normalDecimals()
            .into(amount)
            .strike()
            .visible(props.hasDiscount() && !props.payerCost.hasMultipleInstallments());
    }

    private void resolveBigAmount(final View content) {
        // amount with discount included.
        final TextView amountWithDiscount = content.findViewById(R.id.amount_with_discount);

        final CharSequence charSequence = TextFormatter.withCurrencyId(props.currencyId)
            .withSpace()
            .amount(props.payerCost.getInstallmentAmount())
            .smallDecimals()
            .into(amountWithDiscount)
            .toSpannable();

        if (props.payerCost.hasMultipleInstallments()) {
            final String x = amountWithDiscount.getContext().getString(R.string.mpsdk_installments_by);
            final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(props.payerCost.getInstallments().toString())
                .append(x)
                .append(" ")
                .append(charSequence);
            amountWithDiscount.setText(spannableStringBuilder);
        } else {
            amountWithDiscount.setText(charSequence);
        }
    }
}
