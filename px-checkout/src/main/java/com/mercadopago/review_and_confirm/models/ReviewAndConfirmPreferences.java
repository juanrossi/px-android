package com.mercadopago.review_and_confirm.models;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.components.CustomComponent;
import java.math.BigDecimal;

public class ReviewAndConfirmPreferences {

    private final CustomComponent topComponent;
    private final CustomComponent bottomComponent;
    @DrawableRes
    @Nullable private final
    Integer collectorIcon;
    private final String quantityLabel;
    private final String unitPriceLabel;
    private final BigDecimal productAmount;
    private final BigDecimal shippingAmount;
    private final BigDecimal arrearsAmount;
    private final BigDecimal taxesAmount;
    private final String disclaimerText;
    private final BigDecimal discountAmount;
    private final BigDecimal totalAmount;
    private final String productTitle;
    private final String disclaimerTextColor;
    private final boolean itemsEnabled;

    ReviewAndConfirmPreferences(final Builder builder) {
        topComponent = builder.topView;
        bottomComponent = builder.bottomView;
        itemsEnabled = builder.itemsEnabled;
        collectorIcon = builder.collectorIcon;
        quantityLabel = builder.quantityLabel;
        unitPriceLabel = builder.unitPriceLabel;
        productAmount = builder.productAmount;
        shippingAmount = builder.shippingAmount;
        arrearsAmount = builder.arrearsAmount;
        taxesAmount = builder.taxesAmount;
        discountAmount = builder.discountAmount;
        disclaimerText = builder.disclaimerText;
        productTitle = builder.productTitle;
        disclaimerTextColor = builder.disclaimerTextColor;
        totalAmount = calculateTotalAmount();
    }

    public boolean hasProductAmount() {
        return productAmount != null && productAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasExtrasAmount() {
        return shippingAmount != null && shippingAmount.compareTo(BigDecimal.ZERO) > 0
            || arrearsAmount != null && arrearsAmount.compareTo(BigDecimal.ZERO) > 0
            || taxesAmount != null && taxesAmount.compareTo(BigDecimal.ZERO) > 0
            || discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0
            || productAmount != null && productAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasCustomTopView() {
        return topComponent != null;
    }

    public boolean hasCustomBottomView() {
        return bottomComponent != null;
    }

    public boolean hasItemsEnabled() {
        return itemsEnabled;
    }

    public CustomComponent getTopComponent() {
        return topComponent;
    }

    public CustomComponent getBottomComponent() {
        return bottomComponent;
    }

    @DrawableRes
    @Nullable
    public Integer getCollectorIcon() {
        return collectorIcon;
    }

    public String getQuantityLabel() {
        return quantityLabel;
    }

    public String getUnitPriceLabel() {
        return unitPriceLabel;
    }

    public BigDecimal getProductAmount() {
        return productAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public BigDecimal getArrearsAmount() {
        return arrearsAmount;
    }

    public BigDecimal getTaxesAmount() {
        return taxesAmount;
    }

    public String getDisclaimerText() {
        return disclaimerText;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getDisclaimerTextColor() {
        return disclaimerTextColor;
    }

    private BigDecimal calculateTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        totalAmount = totalAmount.add(productAmount == null ? new BigDecimal(0) : productAmount);
        totalAmount = totalAmount.add(taxesAmount == null ? new BigDecimal(0) : taxesAmount);
        totalAmount = totalAmount.add(shippingAmount == null ? new BigDecimal(0) : shippingAmount);
        totalAmount = totalAmount.add(arrearsAmount == null ? new BigDecimal(0) : arrearsAmount);
        totalAmount =
            totalAmount.subtract(discountAmount == null ? new BigDecimal(0) : discountAmount);
        return totalAmount;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        CustomComponent topView;
        CustomComponent bottomView;
        boolean itemsEnabled = true;
        Integer collectorIcon;
        String quantityLabel;
        String unitPriceLabel;

        BigDecimal productAmount;
        BigDecimal shippingAmount;
        BigDecimal arrearsAmount;
        BigDecimal taxesAmount;
        String disclaimerText;
        BigDecimal discountAmount;
        String productTitle;
        String disclaimerTextColor;

        /**
         * Set product title that will appear in the top of review and confirm summary.
         *
         * @param productTitle the product title
         * @return builder
         */
        public Builder setProductTitle(final String productTitle) {
            this.productTitle = productTitle;
            return this;
        }

        /**
         * Set product amount that will appear in the top of review and confirm summary.
         *
         * @param productAmount the product amount
         * @return builder
         */
        public Builder setProductAmount(final BigDecimal productAmount) {
            this.productAmount = productAmount;
            return this;
        }

        /**
         * Set shipping amount that will appear in the top of review and confirm summary.
         *
         * @param shippingAmount the shipping amount
         * @return builder
         */
        public Builder setShippingAmount(final BigDecimal shippingAmount) {
            this.shippingAmount = shippingAmount;
            return this;
        }

        /**
         * Set arrears amount that will appear in the top of review and confirm summary.
         *
         * @param arrearsAmount the arrears amount
         * @return builder
         */
        public Builder setArrearsAmount(final BigDecimal arrearsAmount) {
            this.arrearsAmount = arrearsAmount;
            return this;
        }

        /**
         * Set taxes amount that will appear in the top of review and confirm summary.
         *
         * @param taxesAmount the taxes amount
         * @return builder
         */
        public Builder setTaxesAmount(final BigDecimal taxesAmount) {
            this.taxesAmount = taxesAmount;
            return this;
        }

        /**
         * Set discount amount that will appear in the top of review and confirm summary.
         *
         * @param discountAmount the discount amount
         * @return builder
         */
        public Builder setDiscountAmount(final BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        /**
         * Set disclaimer text that will appear in the bottom of review and confirm summary.
         *
         * @param disclaimerText the disclaimer text
         * @return builder
         */
        public Builder setDisclaimerText(final String disclaimerText) {
            this.disclaimerText = disclaimerText;
            return this;
        }

        /**
         * Set disclaimer text color.
         *
         * @param disclaimerTextColor the disclaimer text color in hex with hashtag
         * @return builder
         */
        public Builder setDisclaimerTextColor(final String disclaimerTextColor) {
            this.disclaimerTextColor = disclaimerTextColor;
            return this;
        }

        /**
         * Custom view that will appear before payment method description
         * inside Review and confirm screen
         *
         * @param topComponent the top component that renders the top view
         * @return builder
         */
        public Builder setTopComponent(final CustomComponent topComponent) {
            topView = topComponent;
            return this;
        }

        /**
         * Custom view that will appear after payment method description
         * inside Review and confirm screen
         *
         * @param bottomComponent the top bottom component that renders bottom view
         * @return builder
         */
        public Builder setBottomComponent(final CustomComponent bottomComponent) {
            bottomView = bottomComponent;
            return this;
        }

        /**
         * Hide items view in Review and confirm screen
         *
         * @return builder
         */
        public Builder disableItems() {
            itemsEnabled = false;
            return this;
        }

        /**
         * Set custom icon that will appear in items view.
         * It appears only if the item doesn't have a picture url in it
         *
         * @param collectorIcon drawable that will be shown in items view
         * @return builder
         */
        public Builder setCollectorIcon(@DrawableRes final int collectorIcon) {
            this.collectorIcon = collectorIcon;
            return this;
        }

        /**
         * Set a custom text that displays in the quantity label of the items view.
         * It appears only if the item's quantity is greater than 1
         *
         * @param quantityLabel the text that will be shown in the quantity label
         * @return builder
         */
        public Builder setQuantityLabel(final String quantityLabel) {
            this.quantityLabel = quantityLabel;
            return this;
        }

        /**
         * Set a custom text that displays in the unit price label of the items view.
         * It appears only if there are multiple items to show, or
         * if the item's quantity is greater than 1
         *
         * @param unitPriceLabel the text that will be shown in the unit price label
         * @return builder
         */
        public Builder setUnitPriceLabel(final String unitPriceLabel) {
            this.unitPriceLabel = unitPriceLabel;
            return this;
        }

        public ReviewAndConfirmPreferences build() {
            return new ReviewAndConfirmPreferences(this);
        }
    }
}
