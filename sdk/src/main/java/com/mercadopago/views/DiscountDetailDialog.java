package com.mercadopago.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadopago.R;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Site;

import static com.mercadopago.lite.util.CurrenciesUtil.getLocalizedAmountWithCurrencySymbol;

public class DiscountDetailDialog extends MeliDialog {

    private static final String TAG = DiscountDetailDialog.class.getName();
    private static final String ARG_DISCOUNT = "arg_discount";
    private static final String ARG_SITE = "arg_site";

    @Nullable private Discount discount;
    @Nullable private Site site;

    private TextView subtitle;
    private TextView detailMessage;

    public static void showDialog(@NonNull final Discount discount,
        @NonNull final Site site,
        final FragmentManager supportFragmentManager) {
        DiscountDetailDialog discountDetailDialog = new DiscountDetailDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_DISCOUNT, discount);
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
            site = args.getParcelable(ARG_SITE);
        }

        subtitle = view.findViewById(R.id.subtitle);
        detailMessage = view.findViewById(R.id.detail);
        showMessages(discount, site);
    }

    private void showMessages(@Nullable final Discount discount, @Nullable final Site site) {
        if (discount != null && site != null) {
            configurePercentOffMessage(discount, site);
            configureDetailMessage(discount, site);
        } else {
            dismiss();
        }
    }

    private void configureDetailMessage(@NonNull final Discount discount, @NonNull final Site site) {
        detailMessage.setText(getLocalizedAmountWithCurrencySymbol(discount.getCouponAmount(), site));
    }

    private void configurePercentOffMessage(@NonNull final Discount discount, final Site site) {
        if (discount.hasPercentOff()) {
            subtitle.setText(getString(R.string.mpsdk_discount_percent_off, discount.getPercentOff()));
        } else {
            subtitle.setText(getLocalizedAmountWithCurrencySymbol(discount.getCouponAmount(), site));
        }
    }

    @Override
    public int getContentView() {
        return R.layout.mpsdk_dialog_detail_discount;
    }

    @Nullable
    @Override
    public String getSecondaryExitString() {
        return getString(R.string.mpsdk_text_terms_and_conditions_linked);
    }
}
