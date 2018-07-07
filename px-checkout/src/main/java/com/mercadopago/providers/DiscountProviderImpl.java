package com.mercadopago.providers;

import android.content.Context;

import com.mercadopago.R;
import com.mercadopago.android.px.core.MercadoPagoServicesAdapter;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.mvp.TaggedCallback;

public class DiscountProviderImpl implements DiscountsProvider {

    //Errors
    private static final String DISCOUNT_ERROR_AMOUNT_DOESNT_MATCH = "amount-doesnt-match";
    private static final String DISCOUNT_ERROR_RUN_OUT_OF_USES = "run out of uses";
    private static final String DISCOUNT_ERROR_CAMPAIGN_DOESNT_MATCH = "campaign-doesnt-match";
    private static final String DISCOUNT_ERROR_CAMPAIGN_EXPIRED = "campaign-expired";

    private final MercadoPagoServicesAdapter mercadoPago;

    private final Context context;


    public DiscountProviderImpl(Context context, String publicKey) {
        this.context = context;
        mercadoPago = new MercadoPagoServicesAdapter(context, publicKey);
    }

    @Override
    public void getDirectDiscount(String amount, String payerEmail, final TaggedCallback<Discount> taggedCallback) {
        mercadoPago.getDirectDiscount(amount, payerEmail, taggedCallback);
    }

    @Override
    public void getCodeDiscount(String transactionAmount, String payerEmail, String discountCode, final TaggedCallback<Discount> taggedCallback) {
        mercadoPago.getCodeDiscount(transactionAmount, payerEmail, discountCode, taggedCallback);
    }


    @Override
    public String getApiErrorMessage(String error) {
        String message;
        if (error == null) {
            message = context.getString(R.string.mpsdk_something_went_wrong);
        } else {
            if (error.equals(DISCOUNT_ERROR_CAMPAIGN_DOESNT_MATCH)) {
                message = context.getString(R.string.mpsdk_merchant_without_discount_available);
            } else if (error.equals(DISCOUNT_ERROR_RUN_OUT_OF_USES)) {
                message = context.getString(R.string.mpsdk_ran_out_of_quantity_uses_quantity);
            } else if (error.equals(DISCOUNT_ERROR_AMOUNT_DOESNT_MATCH)) {
                message = context.getString(R.string.mpsdk_amount_doesnt_match);
            } else if (error.equals(DISCOUNT_ERROR_CAMPAIGN_EXPIRED)) {
                message = context.getString(R.string.mpsdk_campaign_expired);
            } else {
                message = context.getString(R.string.mpsdk_invalid_code);
            }
        }
        return message;
    }

    @Override
    public String getStandardErrorMessage() {
        return context.getString(R.string.mpsdk_standard_error_message);
    }
}
