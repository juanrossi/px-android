package com.mercadopago.onetap;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import javax.annotation.Nonnull;

class Item extends CompactComponent<Item.Props, Void> {

    /* default */ static class Props {

        @DrawableRes final int icon;
        @NonNull final String itemTitle;

        public Props(final int icon, @NonNull final String itemTitle) {

            this.icon = icon;
            this.itemTitle = itemTitle;
        }
    }

    public Item(final Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View layout = inflate(parent, R.layout.mpsdk_view_onetap_item);
        final TextView itemTitle = layout.findViewById(R.id.item_title);
        final ImageView itemImage = layout.findViewById(R.id.item_image);
        itemImage.setImageResource(props.icon);
        itemTitle.setText(props.itemTitle);

        return layout;
    }
}
