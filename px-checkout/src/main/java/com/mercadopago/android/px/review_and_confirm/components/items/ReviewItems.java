package com.mercadopago.android.px.review_and_confirm.components.items;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.components.CompactComponent;
import com.mercadopago.android.px.components.Renderer;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.review_and_confirm.models.ItemModel;
import com.mercadopago.android.px.review_and_confirm.models.ItemsModel;
import javax.annotation.Nonnull;

public class ReviewItems extends CompactComponent<ReviewItems.Props, Void> {

    public static class Props {

        public ItemsModel getItemsModel() {
            return itemsModel;
        }

        final ItemsModel itemsModel;

        @Nullable
        public Integer getCollectorIcon() {
            return collectorIcon;
        }

        @DrawableRes final Integer collectorIcon;
        final String quantityLabel;
        final String unitPriceLabel;

        public Props(final ItemsModel itemsModel,
            @Nullable @DrawableRes final Integer collectorIcon,
            final String quantityLabel,
            final String unitPriceLabel) {
            this.itemsModel = itemsModel;
            this.collectorIcon = collectorIcon;
            this.quantityLabel = quantityLabel;
            this.unitPriceLabel = unitPriceLabel;
        }
    }

    public ReviewItems(@NonNull final Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final Context context = parent.getContext();
        final LinearLayout linearLayout = createMainLayout(context);

        for (final ItemModel itemModel : props.itemsModel.itemsModelList) {
            addReviewItem(new ReviewItem(new ReviewItem.Props(
                    itemModel,
                    getIcon(props),
                    props.quantityLabel,
                    props.unitPriceLabel)),
                linearLayout);
        }
        return linearLayout;
    }

    @VisibleForTesting
    @DrawableRes
    int getIcon(final ReviewItems.Props props) {
        if (props.getItemsModel().hasUniqueItem()) {
            return props.getCollectorIcon() == null ? R.drawable.mpsdk_review_item_default : props.getCollectorIcon();
        } else {
            return R.drawable.mpsdk_review_item_default;
        }
    }

    @NonNull
    private LinearLayout createMainLayout(@NonNull final Context context) {
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        return linearLayout;
    }

    private void addReviewItem(@NonNull final ReviewItem reviewItem,
        final ViewGroup container) {
        final Renderer renderer = RendererFactory.create(container.getContext(), reviewItem);
        renderer.render(container);
    }
}
