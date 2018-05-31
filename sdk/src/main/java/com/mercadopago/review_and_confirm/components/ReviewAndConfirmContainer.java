package com.mercadopago.review_and_confirm.components;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.Component;
import com.mercadopago.components.RendererFactory;
import com.mercadopago.review_and_confirm.SummaryProvider;
import com.mercadopago.review_and_confirm.models.ItemsModel;
import com.mercadopago.review_and_confirm.models.PaymentModel;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import com.mercadopago.review_and_confirm.models.SummaryModel;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;

public class ReviewAndConfirmContainer extends Component<ReviewAndConfirmContainer.Props, Void> {

    private final SummaryProvider summaryProvider;

    static {
        RendererFactory.register(ReviewAndConfirmContainer.class, ReviewAndConfirmRenderer.class);
    }

    public ReviewAndConfirmContainer(@NonNull final Props props,
                                     @NonNull ActionDispatcher dispatcher,
                                     @NonNull final SummaryProvider summaryProvider) {
        super(props, dispatcher);
        this.summaryProvider = summaryProvider;
    }

    SummaryProvider getSummaryProvider() {
        return summaryProvider;
    }

    public boolean hasItemsEnabled() {
        return props.preferences == null || props.preferences.hasItemsEnabled();
    }

    public boolean hasDiscountTermsAndConditions() {
        return props.discountTermsAndConditionsModel != null;
    }

    public boolean hasMercadoPagoTermsAndConditions() {
        return props.mercadoPagoTermsAndConditionsModel != null;
    }

    public static class Props {
        /* default */ final TermsAndConditionsModel mercadoPagoTermsAndConditionsModel;
        /* default */ final PaymentModel paymentModel;
        /* default */ final SummaryModel summaryModel;
        /* default */ final ReviewAndConfirmPreferences preferences;
        /* default */ final ItemsModel itemsModel;
        /* default */ final TermsAndConditionsModel discountTermsAndConditionsModel;


        public Props(@Nullable final TermsAndConditionsModel mercadoPagoTermsAndConditionsModel,
                     @NonNull final PaymentModel paymentModel,
                     @NonNull final SummaryModel summaryModel,
                     @NonNull final ReviewAndConfirmPreferences preferences,
                     @NonNull final ItemsModel itemsModel,
                     @Nullable final TermsAndConditionsModel discountTermsAndConditionsModel) {

            this.mercadoPagoTermsAndConditionsModel = mercadoPagoTermsAndConditionsModel;
            this.paymentModel = paymentModel;
            this.summaryModel = summaryModel;
            this.preferences = preferences;
            this.itemsModel = itemsModel;
            this.discountTermsAndConditionsModel = discountTermsAndConditionsModel;
        }
    }

}
