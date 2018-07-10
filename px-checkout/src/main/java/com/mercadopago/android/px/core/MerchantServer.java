package com.mercadopago.android.px.core;

import android.content.Context;
import com.mercadopago.android.px.internal.datasource.MerchantService;
import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.model.MerchantPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.adapters.ErrorHandlingCallAdapter;
import com.mercadopago.android.px.services.callbacks.Callback;
import com.mercadopago.android.px.services.util.HttpClientUtil;
import com.mercadopago.android.px.util.JsonUtil;
import java.util.Map;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Deprecated
public class MerchantServer {

    public static void createPreference(Context context, String merchantBaseUrl, String merchantCreatePreferenceUri,
        Map<String, Object> checkoutData, Callback<CheckoutPreference> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.createPreference(ripFirstSlash(merchantCreatePreferenceUri), checkoutData).enqueue(callback);
    }

    public static void createPayment(Context context, String merchantBaseUrl, String merchantCreatePaymentUri,
        MerchantPayment payment, Callback<Payment> callback) {
        MerchantService service = getService(context, merchantBaseUrl);
        service.createPayment(ripFirstSlash(merchantCreatePaymentUri), payment).enqueue(callback);
    }

    public static void getCustomer(Context context, String merchantBaseUrl, String merchantGetCustomerUri,
        String merchantAccessToken, Callback<Customer> callback) {
        MerchantService service = getService(context, merchantBaseUrl);
        service.getCustomer(ripFirstSlash(merchantGetCustomerUri), merchantAccessToken).enqueue(callback);
    }

    private static String ripFirstSlash(String uri) {

        return uri.startsWith("/") ? uri.substring(1, uri.length()) : uri;
    }

    private static Retrofit getRetrofit(Context context, String endPoint) {

        return new Retrofit.Builder()
            .baseUrl(endPoint)
            .client(HttpClientUtil.getClient(context, 20, 20, 20))
            .addConverterFactory(GsonConverterFactory.create(JsonUtil.getInstance().getGson()))
            .addCallAdapterFactory(new ErrorHandlingCallAdapter.ErrorHandlingCallAdapterFactory())
            .build();
    }

    private static MerchantService getService(Context context, String endPoint) {

        Retrofit retrofit = getRetrofit(context, endPoint);
        return retrofit.create(MerchantService.class);
    }
}
