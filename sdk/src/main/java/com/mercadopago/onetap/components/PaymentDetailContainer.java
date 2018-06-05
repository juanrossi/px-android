package com.mercadopago.onetap.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.components.DetailDirectDiscount;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.Item;
import com.mercadopago.viewmodel.OneTapModel;
import javax.annotation.Nonnull;

public class PaymentDetailContainer extends CompactComponent<OneTapModel, Void> {

    public PaymentDetailContainer(@NonNull final OneTapModel oneTapModel) {
        super(oneTapModel);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addItemDetails(parent);
        addDiscount(parent);
        return null;
    }

    private void addItemDetails(@NonNull final ViewGroup parent) {
        for (Item item : props.getCheckoutPreference().getItems()) {
            parent.addView(new DetailItem(item).render(parent));
        }
    }

    private void addDiscount(@NonNull final ViewGroup parent) {
        if (props.hasDiscount()) {
            final View discountView =
                new DetailDirectDiscount(new DetailDirectDiscount.Props(props.getDiscount(), props.getCampaign()))
                    .render(parent);

            parent.addView(addDiscountTitle(parent));
            parent.addView(discountView);
        }
    }

    private View addDiscountTitle(final ViewGroup parent) {
        MPTextView title = (MPTextView) inflate(parent, R.layout.mpsdk_view_modal_title);
        title.setText(R.string.mpsdk_discount_dialog_title);
        return title;
    }
}
