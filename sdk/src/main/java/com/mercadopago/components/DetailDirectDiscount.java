package com.mercadopago.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import javax.annotation.Nonnull;

import static com.mercadopago.lite.util.CurrenciesUtil.getLocalizedAmountWithCurrencySymbol;

public class DetailDirectDiscount extends CompactComponent<DetailDirectDiscount.Props, Void> {

    public static class Props {

        private final Discount discount;
        private final Campaign campaign;

        public Props(final Discount discount, final Campaign campaign) {
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
            detailMessage.setText(mainContainer.getContext().getString(R.string.mpsdk_max_coupon_amount,
                getLocalizedAmountWithCurrencySymbol(props.campaign.getMaxCouponAmount(),
                    props.discount.getCurrencyId())));
        } else {
            detailMessage.setVisibility(View.GONE);
        }
    }

    private void configureOffMessage(final View mainContainer) {
        TextView subtitle = mainContainer.findViewById(R.id.subtitle);
        final Context context = mainContainer.getContext();
        if (props.discount.hasPercentOff()) {
            subtitle.setText(context.getString(R.string.mpsdk_discount_percent_off, props.discount.getPercentOff()));
        } else {
            subtitle.setText(context.getString(R.string.mpsdk_discount_amount_off,
                getLocalizedAmountWithCurrencySymbol(props.discount.getAmountOff(), props.discount.getCurrencyId())));
        }
    }
}
