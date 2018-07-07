package com.mercadopago.onetap.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.android.px.components.CompactComponent;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.util.textformatter.TextFormatter;
import javax.annotation.Nonnull;

class DetailItem extends CompactComponent<Item, Void> {

    DetailItem(@NonNull final Item props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View row = inflate(parent, R.layout.mpsdk_view_onetap_item_detail_row);
        final TextView title = row.findViewById(R.id.title);
        ViewUtils.loadOrGone(resolveItemTitle(parent.getContext()), title);
        final TextView description = row.findViewById(R.id.description);
        ViewUtils.loadOrGone(props.getDescription(), description);

        final TextView itemAmount = row.findViewById(R.id.item_amount);
        TextFormatter.withCurrencyId(props.getCurrencyId())
            .withSpace()
            .amount(Item.getItemTotalAmount(props))
            .normalDecimals()
            .into(itemAmount)
            .normal();

        return row;
    }

    private String resolveItemTitle(final Context context) {
        return props.hasCardinality() ? context
            .getString(R.string.mpsdk_quantity_modal, props.getQuantity(), props.getTitle()) :
            props.getTitle();
    }
}
