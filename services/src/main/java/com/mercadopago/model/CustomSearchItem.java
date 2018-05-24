package com.mercadopago.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CustomSearchItem implements Serializable, Parcelable {
    private String description;
    private String id;
    @SerializedName("payment_type_id")
    private String type;
    private String paymentMethodId;

    public CustomSearchItem() {
    }

    protected CustomSearchItem(Parcel in) {
        description = in.readString();
        id = in.readString();
        type = in.readString();
        paymentMethodId = in.readString();
    }

    public static final Creator<CustomSearchItem> CREATOR = new Creator<CustomSearchItem>() {
        @Override
        public CustomSearchItem createFromParcel(Parcel in) {
            return new CustomSearchItem(in);
        }

        @Override
        public CustomSearchItem[] newArray(int size) {
            return new CustomSearchItem[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(paymentMethodId);
    }
}
