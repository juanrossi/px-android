package com.mercadopago.android.px.model;

import android.support.annotation.Nullable;
import java.io.Serializable;

public class Payer implements Serializable {


    private String id;
    /**
     * @deprecated This method is deprecated, access token should be added
     * as private key.
     */
    @Deprecated
    private String accessToken;

    private Identification identification;
    private String type;
    private String email;
    private String firstName;
    private String lastName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @deprecated This method is deprecated, access token should be added
     * as private key.
     */
    @Deprecated
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @deprecated This method is deprecated, access token should be added
     * as private key.
     */
    @Deprecated
    public void setAccessToken(@Nullable final String accessToken) {
        this.accessToken = accessToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


}
