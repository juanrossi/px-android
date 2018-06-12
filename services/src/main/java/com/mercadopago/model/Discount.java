package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.mercadopago.lite.util.ParcelableUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

import static com.mercadopago.lite.util.CurrenciesUtil.isValidCurrency;

public class Discount implements Serializable, Parcelable {
    /**
     * Discount id is the campaign_id
     */
    private String id;

    private String name;
    private String currencyId;
    private BigDecimal percentOff;
    private BigDecimal amountOff;
    private BigDecimal couponAmount;

    protected Discount(Builder builder) {
        this.id = builder.id;
        this.currencyId = builder.currencyId;
        this.couponAmount = builder.couponAmount;
        this.name = builder.name;
        this.percentOff = builder.percentOff;
        this.amountOff = builder.amountOff;
    }

    public BigDecimal getAmountOff() {
        return amountOff;
    }

    public BigDecimal getCouponAmount() {
        return couponAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPercentOff() {
        return percentOff;
    }

    public BigDecimal getAmountWithDiscount(BigDecimal amount) {
        return amount.subtract(couponAmount);
    }

    public boolean hasPercentOff() {
        return percentOff != null && !BigDecimal.ZERO.equals(percentOff);
    }

    private Discount(Parcel in) {
        id = in.readString();
        name = in.readString();
        currencyId = in.readString();
        percentOff = ParcelableUtil.getBigDecimalReadByte(in);
        amountOff = ParcelableUtil.getBigDecimalReadByte(in);
        couponAmount = new BigDecimal(in.readString());
    }

    public static final Creator<Discount> CREATOR = new Creator<Discount>() {
        @Override
        public Discount createFromParcel(Parcel in) {
            return new Discount(in);
        }

        @Override
        public Discount[] newArray(int size) {
            return new Discount[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(currencyId);
        ParcelableUtil.writeByte(dest, percentOff);
        ParcelableUtil.writeByte(dest, amountOff);
        dest.writeString(couponAmount.toString());
    }

    public String getDiscountTermsUrl() {
        return String
            .format(Locale.US, "https://api.mercadolibre.com/campaigns/%s/terms_and_conditions?format_type=html",
                this.id);
    }

    public static class Builder {
        //region mandatory params
        private String id;
        private String currencyId;
        private BigDecimal couponAmount;
        //endregion mandatory params
        private String name;
        private BigDecimal percentOff;
        private BigDecimal amountOff;

        /**
         * Builder for discount construction
         *
         * @param id discount id
         * @param currencyId amount currency id
         * @param couponAmount amount that will be applied in discount
         */
        public Builder(@NonNull String id, @NonNull String currencyId, @NonNull BigDecimal couponAmount) {
            this.id = id;
            this.currencyId = currencyId;
            this.couponAmount = couponAmount;
            setPercentOff(BigDecimal.ZERO);
            setAmountOff(BigDecimal.ZERO);
        }

        @SuppressWarnings("unused")
        public Discount.Builder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        @SuppressWarnings("unused")
        public Discount.Builder setPercentOff(@NonNull BigDecimal percentOff) {
            this.percentOff = percentOff;
            return this;
        }

        @SuppressWarnings("unused")
        public Discount.Builder setAmountOff(@NonNull BigDecimal amountOff) {
            this.amountOff = amountOff;
            return this;
        }

        public Discount build() {
            if (!isValidCurrency(currencyId)) {
                throw new IllegalStateException("invalid currency id");
            }
            return new Discount(this);
        }
    }
}
