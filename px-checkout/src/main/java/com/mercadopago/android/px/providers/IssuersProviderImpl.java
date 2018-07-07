package com.mercadopago.android.px.providers;

import android.content.Context;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.MercadoPagoServicesAdapter;
import com.mercadopago.android.px.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.mvp.TaggedCallback;
import java.util.List;

/**
 * Created by mromar on 4/26/17.
 */

public class IssuersProviderImpl implements IssuersProvider {

    private final Context context;
    private final MercadoPagoServicesAdapter mercadoPago;

    public IssuersProviderImpl(Context context, String publicKey, String privateKey) {
        this.context = context;

        mercadoPago = new MercadoPagoServicesAdapter(context, publicKey, privateKey);
    }

    @Override
    public void getIssuers(String paymentMethodId, String bin, final TaggedCallback<List<Issuer>> taggedCallback) {
        mercadoPago.getIssuers(paymentMethodId, bin, taggedCallback);
    }

    @Override
    public MercadoPagoError getEmptyIssuersError() {
        String message = context.getString(R.string.mpsdk_standard_error_message);
        String detail = context.getString(R.string.mpsdk_error_message_detail_issuers);

        return new MercadoPagoError(message, detail, false);
    }

    @Override
    public String getCardIssuersTitle() {
        return context.getString(R.string.mpsdk_card_issuers_title);
    }
}
