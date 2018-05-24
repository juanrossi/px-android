package com.mercadopago.review_and_confirm.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lbais on 1/3/18.
 */

public class TermsAndConditionsModel implements Parcelable {
    private final String termsAndConditionsUrl;
    private final boolean isActive;

    public TermsAndConditionsModel(String termsAndConditionsUrl, boolean isActive) {
        this.termsAndConditionsUrl = termsAndConditionsUrl;
        this.isActive = isActive;
    }

    protected TermsAndConditionsModel(Parcel in) {
        termsAndConditionsUrl = in.readString();
        isActive = in.readByte() != 0;
    }

    public static final Creator<TermsAndConditionsModel> CREATOR = new Creator<TermsAndConditionsModel>() {
        @Override
        public TermsAndConditionsModel createFromParcel(Parcel in) {
            return new TermsAndConditionsModel(in);
        }

        @Override
        public TermsAndConditionsModel[] newArray(int size) {
            return new TermsAndConditionsModel[size];
        }
    };

    public String getUrl() {
        return termsAndConditionsUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(termsAndConditionsUrl);
        dest.writeByte((byte) (isActive ? 1 : 0));
    }
}
