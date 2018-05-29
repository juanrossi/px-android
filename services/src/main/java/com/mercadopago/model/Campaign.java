package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Campaign implements Serializable, Parcelable {

    private Long id;
    private String codeType;
    private BigDecimal maxCouponAmount;

    private static final String CODE_TYPE_SINGLE = "single";
    private static final String CODE_TYPE_MULTIPLE = "multiple";
    private static final String CODE_TYPE_NONE = "none";

    public Campaign(Builder builder) {
        this.id = builder.id;
        this.maxCouponAmount = builder.maxCouponAmount;
        this.codeType = builder.codeType;
    }

    @SuppressWarnings("unused")
    public Long getId() {
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
        id = Long.valueOf(in.readString());
        maxCouponAmount = new BigDecimal(in.readString());
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
        dest.writeString(id.toString());
        dest.writeString(maxCouponAmount.toString());
        dest.writeString(codeType);
    }

    public static class Builder {
        //region mandatory params
        private Long id;
        //endregion mandatory params
        private BigDecimal maxCouponAmount = BigDecimal.ZERO;
        private String codeType;

        /**
         * Builder for campaign construction
         *
         * @param id campaign id
         */
        @SuppressWarnings("unused")
        public Builder(@NonNull Long id) {
            this.id = id;
        }

        @SuppressWarnings("unused")
        public Campaign.Builder setMaxCouponAmount(BigDecimal maxCouponAmount) {
            this.maxCouponAmount = maxCouponAmount;
            return this;
        }

        @SuppressWarnings("unused")
        public Campaign.Builder setCodeType(String codeType) {
            this.codeType = codeType;
            return this;
        }

        public Campaign build() {
            return new Campaign(this);
        }
    }

}
