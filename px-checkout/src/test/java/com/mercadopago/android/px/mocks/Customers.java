package com.mercadopago.android.px.mocks;

import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.utils.ResourcesUtil;
import com.mercadopago.android.px.util.JsonUtil;

public class Customers {
    public static Customer getCustomerWithCards() {
        String json = ResourcesUtil.getStringResource("customer_cards.json");
        return JsonUtil.getInstance().fromJson(json, Customer.class);
    }
}
