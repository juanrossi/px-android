package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mercadopago.lite.util.ParcelableUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

public class Campaign implements Serializable, Parcelable {

    private final String id;
    private final String codeType;
    private final BigDecimal maxCouponAmount;

    private static final String CODE_TYPE_SINGLE = "single";
    private static final String CODE_TYPE_MULTIPLE = "multiple";
    private static final String CODE_TYPE_NONE = "none";

    private Campaign(final Builder builder) {
        id = builder.id;
        maxCouponAmount = builder.maxCouponAmount;
        codeType = builder.codeType;
    }

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public BigDecimal getMaxCouponAmount() {
        return maxCouponAmount;
    }

    @SuppressWarnings("unused")
    public String getCodeType() {
        return codeType;
    }

    public boolean hasMaxCouponAmount() {
        return maxCouponAmount != null && BigDecimal.ZERO.compareTo(maxCouponAmount) < 0;
    }

    public boolean isSingleCodeDiscountCampaign() {
        return CODE_TYPE_SINGLE.contains(codeType);
    }

    public boolean isMultipleCodeDiscountCampaign() {
        return CODE_TYPE_MULTIPLE.contains(codeType);
    }

    public boolean isDirectDiscountCampaign() {
        return CODE_TYPE_NONE.contains(codeType);
    }

    private Campaign(Parcel in) {
        id = in.readString();
        maxCouponAmount = ParcelableUtil.getOptionalBigDecimal(in);
        codeType = in.readString();
    }

    public static final Creator<Campaign> CREATOR = new Creator<Campaign>() {
        @Override
        public Campaign createFromParcel(Parcel in) {
            return new Campaign(in);
        }

        @Override
        public Campaign[] newArray(int size) {
            return new Campaign[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        ParcelableUtil.writeOptional(dest, maxCouponAmount);
        dest.writeString(codeType);
    }

    public String getCampaignTermsUrl() {
        return String.format(Locale.US, "https://api.mercadolibre.com/campaigns/%s/terms_and_conditions?format_type=html", id);
    }

    public static class Builder {
        //region mandatory params
        private final String id;
        //endregion mandatory params
        private BigDecimal maxCouponAmount = BigDecimal.ZERO;
        private String codeType;

        /**
         * Builder for campaign construction
         *
         * @param id campaign id
         */
        @SuppressWarnings("unused")
        public Builder(@NonNull String id) {
            this.id = id;
        }

        @SuppressWarnings("unused")
        public Campaign.Builder setMaxCouponAmount(final BigDecimal maxCouponAmount) {
            this.maxCouponAmount = maxCouponAmount;
            return this;
        }

        @SuppressWarnings("unused")
        public Campaign.Builder setCodeType(final String codeType) {
            this.codeType = codeType;
            return this;
        }

        public Campaign build() {
            return new Campaign(this);
        }
    }
}
