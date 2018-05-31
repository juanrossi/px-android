package com.mercadopago.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.R;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Site;
import java.math.BigDecimal;

import static com.mercadopago.lite.util.CurrenciesUtil.getLocalizedAmountWithCurrencySymbol;

public class DiscountDetailDialog extends MeliDialog {

    private static final String TAG = DiscountDetailDialog.class.getName();
    private static final String ARG_DISCOUNT = "arg_discount";
    private static final String ARG_CAMPAIGN = "arg_campaign";
    private static final String ARG_SITE = "arg_site";

    @Nullable
    private Discount discount;
    @Nullable
    private Campaign campaign;
    @Nullable
    private Site site;

    private TextView subtitle;
    private TextView detailMessage;

    public static void showDialog(@NonNull final Discount discount,
        @NonNull final Campaign campaign,
        @NonNull final Site site,
        final FragmentManager supportFragmentManager) {
        DiscountDetailDialog discountDetailDialog = new DiscountDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DISCOUNT, discount);
        bundle.putParcelable(ARG_CAMPAIGN, campaign);
        bundle.putParcelable(ARG_SITE, site);
        discountDetailDialog.setArguments(bundle);
        discountDetailDialog.show(supportFragmentManager, TAG);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            discount = args.getParcelable(ARG_DISCOUNT);
            campaign = args.getParcelable(ARG_CAMPAIGN);
            site = args.getParcelable(ARG_SITE);
        }

        subtitle = view.findViewById(R.id.subtitle);
        detailMessage = view.findViewById(R.id.detail);
        showMessages(discount, campaign, site);
    }

    private void showMessages(@Nullable final Discount discount, @Nullable final Campaign campaign,
        @Nullable final Site site) {
        if (discount != null && campaign != null && site != null) {
            configureOffMessage(discount, site);
            configureDetailMessage(campaign, site);
        } else {
            dismiss();
        }
    }

    private void configureDetailMessage(@NonNull final Campaign campaign, @NonNull final Site site) {
        if (campaign.hasMaxCouponAmount()) {
            detailMessage.setText(getString(R.string.mpsdk_max_coupon_amount,
                getLocalizedAmountWithCurrencySymbol(campaign.getMaxCouponAmount(), site)));
        } else {
            detailMessage.setVisibility(View.GONE);
        }
    }

    private void configureOffMessage(@NonNull final Discount discount, final Site site) {
        if (discount.hasPercentOff()) {
            subtitle.setText(getString(R.string.mpsdk_discount_percent_off, discount.getPercentOff()));
        } else {
            subtitle.setText(getString(R.string.mpsdk_discount_amount_off,
                getLocalizedAmountWithCurrencySymbol(discount.getAmountOff(), site)));
        }
    }

    @Override
    public int getContentView() {
        return R.layout.mpsdk_dialog_detail_discount;
    }

    @Nullable
    @Override
    public String getSecondaryExitString() {
        return getString(R.string.mpsdk_terms_and_conditions);
    }
}
