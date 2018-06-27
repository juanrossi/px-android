package com.mercadopago.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.util.textformatter.TextFormatter;
import javax.annotation.Nonnull;

public class DetailDirectDiscount extends CompactComponent<DetailDirectDiscount.Props, Void> {

    public static class Props {

        @NonNull
        private final Discount discount;
        @NonNull
        private final Campaign campaign;

        public Props(@NonNull final Discount discount, @NonNull final Campaign campaign) {
            this.discount = discount;
            this.campaign = campaign;
        }
    }

    public DetailDirectDiscount(final Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View mainContainer = inflate(parent, R.layout.mpsdk_view_detail_discount_direct);
        configureDetailMessage(mainContainer);
        configureOffMessage(mainContainer);
        return mainContainer;
    }

    private void configureDetailMessage(final View mainContainer) {
        TextView detailMessage = mainContainer.findViewById(R.id.detail);
        if (props.campaign.hasMaxCouponAmount()) {
            TextFormatter.withCurrencyId(props.discount.getCurrencyId())
                .withSpace()
                .amount(props.campaign.getMaxCouponAmount())
                .normalDecimals()
                .into(detailMessage)
                .holder(R.string.mpsdk_max_coupon_amount);
        } else {
            detailMessage.setVisibility(View.GONE);
        }
    }

    private void configureOffMessage(final View mainContainer) {
        TextView subtitle = mainContainer.findViewById(R.id.subtitle);
        if (props.discount.hasPercentOff()) {
            subtitle.setText(subtitle.getContext()
                .getString(R.string.mpsdk_discount_percent_off, props.discount.getPercentOff()));
        } else {
            TextFormatter.withCurrencyId(props.discount.getCurrencyId())
                .withSpace()
                .amount(props.discount.getAmountOff())
                .normalDecimals()
                .into(subtitle)
                .holder(R.string.mpsdk_discount_amount_off);
        }
    }
}
