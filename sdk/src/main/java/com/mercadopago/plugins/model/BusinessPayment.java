package com.mercadopago.plugins.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.mercadopago.R;
import com.mercadopago.model.ExternalFragment;
import com.mercadopago.util.TextUtils;

@SuppressWarnings("unused")
public final class BusinessPayment implements PluginPayment, Parcelable {

    private final String help;
    private final int iconId;
    private final String title;
    private final Status status;
    private final boolean shouldShowPaymentMethod;
    private final ExitAction exitActionPrimary;
    private final ExitAction exitActionSecondary;
    private final String statementDescription;
    private final String receiptId;
    private final String imageUrl;

    private final ExternalFragment topFragment;
    private final ExternalFragment bottomFragment;

    private BusinessPayment(Builder builder) {
        help = builder.help;
        title = builder.title;
        status = builder.status;
        iconId = builder.iconId;
        shouldShowPaymentMethod = builder.shouldShowPaymentMethod;
        exitActionPrimary = builder.buttonPrimary;
        exitActionSecondary = builder.buttonSecondary;
        statementDescription = builder.statementDescription;
        receiptId = builder.receiptId;
        imageUrl = builder.imageUrl;
        topFragment = builder.topFragment;
        bottomFragment = builder.bottomFragment;

    }

    protected BusinessPayment(Parcel in) {
        iconId = in.readInt();
        title = in.readString();
        shouldShowPaymentMethod = in.readByte() != 0;
        exitActionPrimary = in.readParcelable(ExitAction.class.getClassLoader());
        exitActionSecondary = in.readParcelable(ExitAction.class.getClassLoader());
        status = Status.fromName(in.readString());
        help = in.readString();
        statementDescription = in.readString();
        receiptId = in.readString();
        imageUrl = in.readString();
        topFragment = in.readParcelable(ExternalFragment.class.getClassLoader());
        bottomFragment = in.readParcelable(ExternalFragment.class.getClassLoader());
    }

    public static final Creator<BusinessPayment> CREATOR = new Creator<BusinessPayment>() {
        @Override
        public BusinessPayment createFromParcel(Parcel in) {
            return new BusinessPayment(in);
        }

        @Override
        public BusinessPayment[] newArray(int size) {
            return new BusinessPayment[size];
        }
    };

    @Override
    public void process(final Processor processor) {
        processor.process(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(iconId);
        dest.writeString(title);
        dest.writeByte((byte) (shouldShowPaymentMethod ? 1 : 0));
        dest.writeParcelable(exitActionPrimary, flags);
        dest.writeParcelable(exitActionSecondary, flags);
        dest.writeString(status.name);
        dest.writeString(help);
        dest.writeString(statementDescription);
        dest.writeString(receiptId);
        dest.writeString(imageUrl);
        dest.writeParcelable(topFragment, 0);
        dest.writeParcelable(bottomFragment, 0);
    }

    public boolean hasReceipt() {
        return receiptId != null;
    }

    public boolean hasTopFragment() {
        return topFragment != null;
    }

    public boolean hasBottomFragment() {
        return bottomFragment != null;
    }

    public Status getStatus() {
        return status;
    }

    public int getIcon() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasHelp() {
        return TextUtils.isNotEmpty(help);
    }

    public ExitAction getSecondaryAction() {
        return exitActionSecondary;
    }

    public ExitAction getPrimaryAction() {
        return exitActionPrimary;
    }

    public String getHelp() {
        return help;
    }

    public boolean shouldShowPaymentMethod() {
        return shouldShowPaymentMethod;
    }

    public String getStatementDescription() {
        return statementDescription;
    }

    public String getReceipt() {
        return receiptId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ExternalFragment getTopFragment() {
        return topFragment;
    }

    public ExternalFragment getBottomFragment() {
        return bottomFragment;
    }

    public enum Status {
        APPROVED("APPROVED", R.color.ui_components_success_color, R.drawable.mpsdk_badge_check, 0),
        REJECTED("REJECTED", R.color.ui_components_error_color, R.drawable.mpsdk_badge_error, R.string.mpsdk_rejection_label),
        PENDING("PENDING", R.color.mpsdk_ui_components_warning_color, R.drawable.mpsdk_badge_pending_orange, 0);

        public final String name;
        public final int resColor;
        public final int badge;
        public final int message;

        Status(final String name,
               @ColorRes final int resColor,
               @DrawableRes final int badge,
               @StringRes final int message) {
            this.name = name;
            this.resColor = resColor;
            this.badge = badge;
            this.message = message;
        }

        public static Status fromName(String text) {
            for (Status s : Status.values()) {
                if (s.name.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalStateException("Invalid status");
        }
    }


    public static class Builder {

        // Mandatory values
        @NonNull
        private final Status status;
        @DrawableRes
        private final int iconId;
        @NonNull
        private final String title;

        private String imageUrl;

        // Optional values
        private boolean shouldShowPaymentMethod;
        private String statementDescription;
        private ExitAction buttonPrimary;
        private ExitAction buttonSecondary;
        private String help;
        private String receiptId;

        private ExternalFragment topFragment;
        private ExternalFragment bottomFragment;

        public Builder(@NonNull final Status status,
                       @DrawableRes final int iconId,
                       @NonNull final String title) {
            this.title = title;
            this.status = status;
            this.iconId = iconId;
            shouldShowPaymentMethod = false;
            buttonPrimary = null;
            buttonSecondary = null;
            help = null;
            receiptId = null;
            imageUrl = null;
        }

        public Builder(@NonNull final Status status,
                       @NonNull final String imageUrl,
                       @NonNull final String title) {
            this(status, R.drawable.mpsdk_icon_default, title);
            this.imageUrl = imageUrl;
        }

        public BusinessPayment build() {
            if (buttonPrimary == null && buttonSecondary == null)
                throw new IllegalStateException("At least one button should be provided for BusinessPayment");
            return new BusinessPayment(this);
        }

        /**
         * if Exit action is set, then a big primary button
         * will appear and the click action will trigger a resCode
         * that will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setPrimaryButton(@Nullable ExitAction exitAction) {
            buttonPrimary = exitAction;
            return this;
        }

        /**
         * if Exit action is set, then a small secondary button
         * will appear and the click action will trigger a resCode
         * that will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setSecondaryButton(@Nullable ExitAction exitAction) {
            buttonSecondary = exitAction;
            return this;
        }

        /**
         * if help is set, then a small box with help instructions will appear
         *
         * @param help a help message
         * @return builder
         */
        public Builder setHelp(@Nullable String help) {
            this.help = help;
            return this;
        }

        /**
         * If value true is set, then payment method box
         * will appear with the amount value and payment method
         * options that were selected by the user.
         *
         * @param visible visibility mode
         * @return builder
         */
        public Builder setPaymentMethodVisibility(boolean visible) {
            this.shouldShowPaymentMethod = visible;
            return this;
        }

        /**
         * If value true is set on {@link #setPaymentMethodVisibility }
         * and the payment method is credit card
         * then the statementDescription will be shown on payment method view.
         *
         * @param statementDescription disclaimer text
         * @return builder
         */
        public Builder setStatementDescription(final String statementDescription) {
            this.statementDescription = statementDescription;
            return this;
        }

        /**
         * If value is set, then receipt view will appear.
         *
         * @param receiptId the receipt id to be shown.
         * @return builder
         */
        public Builder setReceiptId(final String receiptId) {
            this.receiptId = receiptId;
            return this;
        }

        /**
         * Custom fragment that will appear before payment method description
         * inside Business result screen.
         *
         * @param zClass fragment class
         * @return builder
         */
        public Builder setTopFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            this.topFragment = new ExternalFragment(zClass, args);
            return this;
        }

        /**
         * Custom fragment that will appear after payment method description
         * inside Business result screen.
         *
         * @param zClass fragment class
         * @return builder
         */
        public Builder setBottomFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            this.bottomFragment = new ExternalFragment(zClass, args);
            return this;
        }
    }


}