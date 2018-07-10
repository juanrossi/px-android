package com.mercadopago.views;

import android.content.Context;
import com.mercadopago.model.PayerCost;
import com.mercadopago.uicontrollers.installments.InstallmentsReviewView;

@Deprecated
public class MercadoPagoUI {

    public static class Activities {

        public static final int CUSTOMER_CARDS_REQUEST_CODE = 0;
    }

    public static class Views {

        public static class InstallmentsReviewViewBuilder {
            private Context context;
            private PayerCost payerCost;
            private String currencyId;

            public InstallmentsReviewViewBuilder setContext(Context context) {
                this.context = context;
                return this;
            }

            public InstallmentsReviewViewBuilder setPayerCost(PayerCost payerCost) {
                this.payerCost = payerCost;
                return this;
            }

            public InstallmentsReviewViewBuilder setCurrencyId(String currencyId) {
                this.currencyId = currencyId;
                return this;
            }

            public InstallmentsReviewView build() {
                return new InstallmentsReviewView(context, payerCost, currencyId);
            }
        }
    }
}
