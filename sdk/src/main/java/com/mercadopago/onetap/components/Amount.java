package com.mercadopago.onetap.components;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Installment;
import com.mercadopago.model.PayerCost;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.util.textformatter.AmountFormatter;
import com.mercadopago.util.textformatter.CurrencyFormatter;
import com.mercadopago.util.textformatter.TextFormatter;
import com.mercadopago.viewmodel.OneTapModel;
import java.math.BigDecimal;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class Amount extends CompactComponent<Amount.Props, OneTap.Actions> {

    static class Props {
        /* default */ @NonNull final BigDecimal amount;
        /* default */ @Nullable final Discount discount;
        /* default */ @NonNull final String currencyId;
        /* default */ @Nullable final PayerCost payerCost;
        /* default */ final int installment;
        /* default */ final boolean hasExtraAmount;
        /* default */ final boolean hasMaxDiscountLabel;

        /* default */ Props(final boolean hasExtraAmount, @NonNull final BigDecimal amount,
            @Nullable final Discount discount,
            @NonNull final String currencyId,
            @Nullable final PayerCost payerCost,
            final int installment,
            final boolean hasMaxDiscountLabel) {
            this.amount = amount;
            this.discount = discount;
            this.currencyId = currencyId;
            this.payerCost = payerCost;
            this.installment = installment;
            this.hasExtraAmount = hasExtraAmount;
            this.hasMaxDiscountLabel = hasMaxDiscountLabel;
        }

        /* default */
        static Props from(final OneTapModel props) {
            final CardPaymentMetadata card = props.getPaymentMethods().getOneTapMetadata().getCard();
            final PayerCost payerCost = card != null ? card.getAutoSelectedInstallment() : null;
            return new Amount.Props(
                props.hasExtraAmount(),
                props.getCheckoutPreference().getTotalAmount(), props.getDiscount(),
                props.getCheckoutPreference().getSite().getCurrencyId(),
                payerCost,
                payerCost == null ? PayerCost.NO_INSTALLMENTS : payerCost.getInstallments(),
                props.hasMaxDiscountLabel());
        }

        /* default */ boolean hasExtrasAmount() {
            return hasExtraAmount;
        }

        /* default */ boolean hasDiscount() {
            return discount != null;
        }

        /* default */ boolean shouldShowPercentOff() {
            return hasDiscount() && discount.hasPercentOff();
        }

        /* default */ boolean hasMultipleInstallments() {
            return installment > 1;
        }

        /* default */ boolean hasMaxDiscountLabel() {
            return hasMaxDiscountLabel;
        }
    }

    /* default */ Amount(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View content = inflate(parent, R.layout.mpsdk_compact_amount);
        final ViewGroup discountLayout = content.findViewById(R.id.discount_detail_layout);
        resolveSmallAmountPlusDiscount(content);
        resolveBigAmount(content);
        resolveOffAmount(discountLayout);
        resolveMaxDiscount(discountLayout);
        resolveArrow(content);
        return content;
    }

    private void resolveArrow(@NonNull final View content) {
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                getActions().onAmountShowMore();
            }
        });
    }

    private void resolveOffAmount(@NonNull final ViewGroup discountLayout) {

        final TextView discountMessage = discountLayout.findViewById(R.id.discount_message);
        ViewUtils.showOrGone(discountLayout, props.hasDiscount());

        if (props.shouldShowPercentOff()) {
            TextFormatter.withCurrencyId(props.currencyId)
                .noSpace().noSymbol()
                .amount(props.discount.getPercentOff())
                .normalDecimals()
                .into(discountMessage)
                .holder(R.string.mpsdk_discount_percent_off_percent);
        } else if (props.hasDiscount()) {
            TextFormatter.withCurrencyId(props.currencyId)
                .withSpace()
                .amount(props.discount.getCouponAmount())
                .normalDecimals()
                .into(discountMessage)
                .holder(R.string.mpsdk_discount_percent_off_amount);
        }

        ViewUtils.showOrGone(discountMessage, props.hasDiscount());
    }

    private void resolveMaxDiscount(@NonNull final ViewGroup discountLayout) {
        final TextView discountMaxLabel = discountLayout.findViewById(R.id.discount_max_label);
        ViewUtils.showOrGone(discountMaxLabel, props.hasDiscount() && props.hasMaxDiscountLabel());
    }

    private void resolveSmallAmountPlusDiscount(final View content) {
        final TextView amount = content.findViewById(R.id.amount);
        TextFormatter.withCurrencyId(props.currencyId)
            .withSpace()
            .amount(props.amount)
            .normalDecimals()
            .into(amount)
            .strike()
            .visible(props.hasDiscount() && !props.hasMultipleInstallments());
    }

    private void resolveBigAmount(final View content) {
        // amount with discount included.
        final TextView amountWithDiscount = content.findViewById(R.id.amount_with_discount);

        final CurrencyFormatter currencyFormatter = TextFormatter.withCurrencyId(props.currencyId)
            .withSpace();

        final AmountFormatter amountFormatter = props.hasMultipleInstallments() ?
            currencyFormatter.amount(props.payerCost.getInstallmentAmount()) :
            currencyFormatter.amount(resolveAmountWithDiscount(props.amount));

        final CharSequence charSequence = amountFormatter
            .smallDecimals()
            .into(amountWithDiscount)
            .toSpannable();

        if (props.hasMultipleInstallments()) {
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

    private BigDecimal resolveAmountWithDiscount(@NonNull final BigDecimal amount) {
        return props.discount == null ? amount : amount.subtract(props.discount.getCouponAmount());
    }
}
