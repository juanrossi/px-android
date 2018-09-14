package com.mercadopago.android.px.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TrackingIntent implements Serializable {

    @SerializedName("public_key")
    public String publicKey;
    @SerializedName("token")
    public String cardToken;
    @SerializedName("sdk_flavor")
    public String flavor;
    @SerializedName("sdk_platform")
    public String platform;
    @SerializedName("sdk_type")
    public String type;
    @SerializedName("sdk_version")
    public String version;
    @SerializedName("site_id")
    public String site;

    public TrackingIntent(final String publicKey, final String cardToken, final String flavor,
        final String platform, final String type,
        final String version, final String site) {
        this.publicKey = publicKey;
        this.cardToken = cardToken;
        this.flavor = flavor;
        this.platform = platform;
        this.type = type;
        this.version = version;
        this.site = site;
    }
}
