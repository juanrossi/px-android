package com.mercadopago.android.px.services;

import com.mercadopago.android.px.services.adapters.MPCall;
import com.mercadopago.android.px.model.Customer;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mromar on 10/20/17.
 */

public interface CustomerService {

    @GET("/customers")
    MPCall<Customer> getCustomer(@Query("preference_id") String preferenceId);
}