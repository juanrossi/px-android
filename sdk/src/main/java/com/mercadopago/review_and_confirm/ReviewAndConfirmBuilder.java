package com.mercadopago.review_and_confirm;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.mercadopago.R;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Item;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.review_and_confirm.models.ItemsModel;
import com.mercadopago.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.review_and_confirm.models.PaymentModel;
import com.mercadopago.review_and_confirm.models.SummaryModel;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.util.MercadoPagoUtil;

import java.math.BigDecimal;
import java.util.List;

public class ReviewAndConfirmBuilder {

    private PaymentMethod paymentMethod;
    private PayerCost payerCost;
    private Issuer issuer;
    private Discount discount;
    private Token token;
    private Boolean hasExtraPaymentMethods;
    private Boolean termsAndConditionsEnabled;
    private String merchantPublicKey;
    private CheckoutPreference checkoutPreference;

    public ReviewAndConfirmBuilder setIssuer(Issuer issuer) {
        this.issuer = issuer;
        return this;
    }

    public ReviewAndConfirmBuilder setMerchantPublicKey(String merchantPublicKey) {
        this.merchantPublicKey = merchantPublicKey;
        return this;
    }

    public ReviewAndConfirmBuilder setPaymentMethod(PaymentMethod paymentMehtod) {
        paymentMethod = paymentMehtod;
        return this;
    }

    public ReviewAndConfirmBuilder setPayerCost(PayerCost payerCost) {
        this.payerCost = payerCost;
        return this;
    }

    public ReviewAndConfirmBuilder setToken(Token token) {
        this.token = token;
        return this;
    }

    public ReviewAndConfirmBuilder setHasExtraPaymentMethods(Boolean hasExtraPaymentMethods) {
        this.hasExtraPaymentMethods = hasExtraPaymentMethods;
        return this;
    }

    public ReviewAndConfirmBuilder setDiscount(Discount discount) {
        this.discount = discount;
        return this;
    }

    public ReviewAndConfirmBuilder setTermsAndConditionsEnabled(Boolean termsAndConditionsEnabled) {
        this.termsAndConditionsEnabled = termsAndConditionsEnabled;
        return this;
    }

    private void validate(final Activity activity) {
        if (activity == null) {
            throw new IllegalStateException("activity can't be null");
        }
        if (checkoutPreference == null) {
            throw new IllegalStateException("Checkout preference can't be null");
        }
        if (paymentMethod == null) {
            throw new IllegalStateException("payment method can't be null");
        }

        if (MercadoPagoUtil.isCard(paymentMethod.getPaymentTypeId())) {
            if (payerCost == null) {
                throw new IllegalStateException("payer cost can't be null");
            }
            if (token == null) {
                throw new IllegalStateException("token can't be null");
            }
        }
    }

    public void startForResult(@NonNull final Activity activity) {

        validate(activity);

        List<Item> items = checkoutPreference.getItems();
        BigDecimal amount = checkoutPreference.getTotalAmount();
        Site site = checkoutPreference.getSite();

        String title = SummaryModel.resolveTitle(items,
                activity.getResources().getString(R.string.mpsdk_review_summary_product),
                activity.getResources().getString(R.string.mpsdk_review_summary_products));

        TermsAndConditionsModel mercadoPagoTermsAndConditions =
                termsAndConditionsEnabled ? new TermsAndConditionsModel(site.getTermsAndConditionsUrl(),
                    activity.getString(R.string.mpsdk_terms_and_conditions_message),
                    activity.getString(R.string.mpsdk_terms_and_conditions_linked_message),
                    merchantPublicKey,
                    LineSeparatorType.TOP_LINE_SEPARATOR) : null;

        TermsAndConditionsModel discountTermsAndConditions =
                discount != null ? new TermsAndConditionsModel(discount.getDiscountTermsUrl(),
                    activity.getString(R.string.mpsdk_discount_terms_and_conditions_message),
                    activity.getString(R.string.mpsdk_discount_terms_and_conditions_linked_message),
                    merchantPublicKey,
                    LineSeparatorType.BOTTOM_LINE_SEPARATOR) : null;

        PaymentModel paymentModel = new PaymentModel(paymentMethod, token, issuer, hasExtraPaymentMethods);
        SummaryModel summaryModel = new SummaryModel(amount, paymentMethod, site, payerCost, discount, title);
        ItemsModel itemsModel = new ItemsModel(site.getCurrencyId(), items);

        ReviewAndConfirmActivity.start(activity,
                merchantPublicKey,
                mercadoPagoTermsAndConditions,
                paymentModel,
                summaryModel,
                itemsModel,
                discountTermsAndConditions);
    }

    public ReviewAndConfirmBuilder setPreference(@NonNull final CheckoutPreference checkoutPreference) {
        this.checkoutPreference = checkoutPreference;
        return this;
    }
}
