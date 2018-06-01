package com.mercadopago.onetap.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.model.CardPaymentMetadata;
import com.mercadopago.model.Discount;
import com.mercadopago.model.OneTapMetadata;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.viewmodel.OneTapModel;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PaymentMethod extends CompactComponent<PaymentMethod.Props, OneTap.Actions> {

    static class Props {
        /* default */ @NonNull final String paymentMethodType;
        /* default */ @NonNull final String paymentMethodId;
        /* default */ @NonNull final CardPaymentMetadata card;
        /* default */ @NonNull final String currencyId;
        /* default */ @Nullable final Discount discount;

        /* default */ Props(@NonNull final String paymentMethodType,
            @NonNull final String paymentMethodId,
            @NonNull final CardPaymentMetadata card,
            @NonNull final String currencyId,
            @Nullable final Discount discount) {

            this.paymentMethodType = paymentMethodType;
            this.paymentMethodId = paymentMethodId;
            this.card = card;
            this.currencyId = currencyId;
            this.discount = discount;
        }

        /* default */
        @NonNull
        String getPaymentMethodType() {
            return paymentMethodType;
        }

        /* default */
        @NonNull
        public String getPaymentMethodId() {
            return paymentMethodId;
        }

        /* default */
        @NonNull
        public CardPaymentMetadata getCard() {
            return card;
        }

        /* default */
        static Props createFrom(final OneTapModel props) {
            final OneTapMetadata oneTapMetadata = props.getPaymentMethods().getOneTapMetadata();
            return new Props(oneTapMetadata.getPaymentTypeId(), oneTapMetadata.getPaymentMethodId(), oneTapMetadata.getCard(),
                props.getCheckoutPreference().getSite().getCurrencyId(), props.getDiscount());
        }

        private boolean hasDiscount() {
            return discount != null;
        }
    }

    /* default */ PaymentMethod(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @VisibleForTesting()
    CompactComponent resolveComponent() {
        if (PaymentTypes.isCardPaymentMethod(props.getPaymentMethodType())) {
            return new MethodCard(MethodCard.Props.createFrom(props), getActions());
        } else if (PaymentTypes.isPlugin(props.getPaymentMethodType())) {
            return new MethodPlugin(MethodPlugin.Props.createFrom(props), getActions());
        } else {
            //TODO should not happen or should have another way to resolve it.
            throw new IllegalStateException("shouldn't happen - one tap payment type not supported");
        }
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup main = (ViewGroup) inflate(parent, R.layout.mpsdk_payment_method_compact_container);
        final View view = resolveComponent().render(main);
        main.addView(view);
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                getActions().changePaymentMethod();
            }
        });
        resolveMargin(main, parent.getContext());
        return main;
    }

    private void resolveMargin(final View main, final Context context) {
        final int margin = context.getResources().getDimensionPixelSize(R.dimen.mpsdk_m_margin);
        ViewUtils.setMarginBottomInView(main, props.hasDiscount() ? 0 : margin);
    }
}
