package com.mercadopago.onetap.components;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.components.DetailDirectDiscount;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.internal.repository.DiscountRepository;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Item;
import java.util.List;
import javax.annotation.Nonnull;

public class PaymentDetailContainer extends CompactComponent<PaymentDetailContainer.Props, Void> {

    public static class Props {
        /* default */ final DiscountRepository discountRepository;
        /* default */ final List<Item> items;

        public Props(final DiscountRepository discountRepository, final List<Item> items) {
            this.discountRepository = discountRepository;
            this.items = items;
        }
    }

    public PaymentDetailContainer(@NonNull final PaymentDetailContainer.Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addItemDetails(parent);
        addDiscount(parent);
        return null;
    }

    private void addItemDetails(@NonNull final ViewGroup parent) {
        for (final Item item : props.items) {
            parent.addView(new DetailItem(item).render(parent));
        }
    }

    private void addDiscount(@NonNull final ViewGroup parent) {
        final Discount discount = props.discountRepository.getDiscount();
        final Campaign campaign = props.discountRepository.getCampaign();
        if (discount != null && campaign != null) {
            final View discountView =
                new DetailDirectDiscount(new DetailDirectDiscount.Props(discount, campaign))
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
