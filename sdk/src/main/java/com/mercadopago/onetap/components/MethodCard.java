package com.mercadopago.onetap.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.Discount;
import com.mercadopago.model.PayerCost;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.util.ResourceUtil;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.util.textformatter.TextFormatter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class MethodCard extends CompactComponent<MethodCard.Props, OneTap.Actions> {

    static class Props {

        /* default */ @NonNull final CardPaymentMetadata card;
        /* default */ @NonNull final String paymentMethodId;
        /* default */ @NonNull final String currencyId;
        /* default */ @Nullable final Discount discount;

        /* default */ Props(@NonNull final CardPaymentMetadata card,
            @NonNull final String paymentMethodId,
            @NonNull final String currencyId,
            @Nullable final Discount discount) {
            this.card = card;
            this.paymentMethodId = paymentMethodId;
            this.currencyId = currencyId;
            this.discount = discount;
        }

        /* default */
        static Props createFrom(final PaymentMethod.Props props) {
            return new Props(props.card, props.paymentMethodId, props.currencyId, props.discount);
        }
    }

    /* default */ MethodCard(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View main = inflate(parent, R.layout.mpsdk_payment_method_card_compact);
        final ImageView logo = main.findViewById(R.id.icon);
        final TextView name = main.findViewById(R.id.name);
        final String cardDescription =
            parent.getContext()
                .getString(R.string.mpsdk_card_hint, props.card.getIssuer().getName(), props.card.getLastFourDigits());
        name.setText(cardDescription);
        logo.setImageResource(ResourceUtil.getIconResource(parent.getContext(), props.paymentMethodId));
        resolveCft(main);
        resolveAmount(main);
        return main;
    }

    private void resolveAmount(final View main) {
        final ViewGroup row = main.findViewById(R.id.installments_row);
        final PayerCost autoSelectedInstallment = props.card.getAutoSelectedInstallment();
        if (autoSelectedInstallment.hasMultipleInstallments()) {
            row.setVisibility(View.VISIBLE);
            showAmount(row, autoSelectedInstallment);
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void showAmount(@NonNull final ViewGroup row, @NonNull final PayerCost autoSelectedInstallment) {

        final TextView totalAmountText = row.findViewById(R.id.total_amount);
        final TextView amountLessDiscountText = row.findViewById(R.id.amount_less_discount);

        TextFormatter
            .withCurrencyId(props.currencyId)
            .withSpace()
            .amount(autoSelectedInstallment.getTotalAmount())
            .add(props.discount)
            .normalDecimals()
            .into(totalAmountText)
            .strike()
            .visible(props.discount != null);

        TextFormatter.withCurrencyId(props.currencyId)
            .withSpace()
            .amount(autoSelectedInstallment.getTotalAmount())
            .normalDecimals()
            .into(amountLessDiscountText)
            .holder(R.string.mpsdk_total_amount_holder);

    }

    private void resolveCft(@NonNull final View main) {
        final PayerCost autoSelectedInstallment = props.card.getAutoSelectedInstallment();
        final TextView cft = main.findViewById(R.id.cft);
        final Context context = main.getContext();
        cft.setVisibility(autoSelectedInstallment.hasCFT() ? View.VISIBLE : View.GONE);
        cft.setText(autoSelectedInstallment.hasCFT() ? context
            .getString(R.string.mpsdk_installments_cft, autoSelectedInstallment.getCFTPercent()) : "");
    }
}
