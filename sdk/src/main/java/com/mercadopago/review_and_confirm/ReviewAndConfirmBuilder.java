package com.mercadopago.review_and_confirm;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.mercadopago.R;
import com.mercadopago.internal.di.AmountModule;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Item;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.review_and_confirm.models.ItemsModel;
import com.mercadopago.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.review_and_confirm.models.PaymentModel;
import com.mercadopago.review_and_confirm.models.SummaryModel;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.util.TextUtils;
import java.util.List;

public class ReviewAndConfirmBuilder {

    private Issuer issuer;
    private Discount discount;
    private Token token;
    private Boolean hasExtraPaymentMethods;
    private String merchantPublicKey;
    private CheckoutPreference checkoutPreference;

    public ReviewAndConfirmBuilder setIssuer(final Issuer issuer) {
        this.issuer = issuer;
        return this;
    }

    public ReviewAndConfirmBuilder setMerchantPublicKey(final String merchantPublicKey) {
        this.merchantPublicKey = merchantPublicKey;
        return this;
    }

    public ReviewAndConfirmBuilder setToken(final Token token) {
        this.token = token;
        return this;
    }

    public ReviewAndConfirmBuilder setHasExtraPaymentMethods(final boolean hasExtraPaymentMethods) {
        this.hasExtraPaymentMethods = hasExtraPaymentMethods;
        return this;
    }

    public ReviewAndConfirmBuilder setDiscount(Discount discount) {
        this.discount = discount;
        return this;
    }

    private void validate(final Activity activity) {
        if (activity == null) {
            throw new IllegalStateException("activity can't be null");
        }
        if (checkoutPreference == null) {
            throw new IllegalStateException("Checkout preference can't be null");
        }
    }

    public void startForResult(@NonNull final Activity activity) {

        validate(activity);

        final AmountModule amountModule = new AmountModule(activity);
        final UserSelectionRepository userSelectionRepository =
            amountModule.getConfigurationModule().getUserSelectionRepository();

        final AmountRepository amountRepository = amountModule.getAmountRepository();

        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();


        final List<Item> items = checkoutPreference.getItems();

        final Site site = checkoutPreference.getSite();

        final String title = SummaryModel.resolveTitle(items,
                activity.getResources().getString(R.string.mpsdk_review_summary_product),
                activity.getResources().getString(R.string.mpsdk_review_summary_products));

        boolean termsAndConditionsEnabled = TextUtils.isEmpty(checkoutPreference.getPayer().getAccessToken());

        final TermsAndConditionsModel mercadoPagoTermsAndConditions =
                termsAndConditionsEnabled ? new TermsAndConditionsModel(site.getTermsAndConditionsUrl(),
                    activity.getString(R.string.mpsdk_terms_and_conditions_message),
                    activity.getString(R.string.mpsdk_terms_and_conditions_linked_message),
                    merchantPublicKey,
                    LineSeparatorType.TOP_LINE_SEPARATOR) : null;

        final TermsAndConditionsModel discountTermsAndConditions =
                discount != null ? new TermsAndConditionsModel(discount.getDiscountTermsUrl(),
                    activity.getString(R.string.mpsdk_discount_terms_and_conditions_message),
                    activity.getString(R.string.mpsdk_discount_terms_and_conditions_linked_message),
                    merchantPublicKey,
                    LineSeparatorType.BOTTOM_LINE_SEPARATOR) : null;

        final PaymentModel paymentModel = new PaymentModel(paymentMethod, token, issuer, hasExtraPaymentMethods);

        final SummaryModel summaryModel =
            new SummaryModel(amountRepository.getAmountToPay(), paymentMethod, site,
                userSelectionRepository.getPayerCost(), discount, title,
                checkoutPreference.getTotalAmount(),
                amountRepository.getAppliedCharges());

        final ItemsModel itemsModel = new ItemsModel(site.getCurrencyId(), items);

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
