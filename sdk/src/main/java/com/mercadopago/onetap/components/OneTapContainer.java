package com.mercadopago.onetap.components;

import android.content.Context;
import android.icu.util.CurrencyAmount;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.R;
import com.mercadopago.components.Action;
import com.mercadopago.components.Button;
import com.mercadopago.components.ButtonPrimary;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.components.TermsAndConditionsComponent;
import com.mercadopago.internal.di.ConfigurationModule;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Item;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.util.ViewUtils;
import com.mercadopago.viewmodel.OneTapModel;

import java.util.List;

import javax.annotation.Nonnull;

public class OneTapContainer extends CompactComponent<OneTapModel, OneTap.Actions> {

    public OneTapContainer(final OneTapModel oneTapModel, final OneTap.Actions callBack) {
        super(oneTapModel, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ConfigurationModule configurationModule = new ConfigurationModule(parent.getContext());
        final PaymentSettingRepository configuration = configurationModule.getConfiguration();
        final Discount discount = configuration.getDiscount();
        final Campaign campaign = configuration.getCampaign();

        addItem(parent, configuration.getCheckoutPreference().getItems());
        addAmount(parent, configuration);
        addPaymentMethod(parent, configuration);
        addTermsAndConditions(parent, campaign);
        addConfirmButton(parent, discount);
        return parent;
    }

    private void addItem(final ViewGroup parent, final List<Item> items) {
        final String defaultMultipleTitle = parent.getContext().getString(R.string.mpsdk_review_summary_products);
        final int icon =
                props.getCollectorIcon() == null ? R.drawable.mpsdk_review_item_default : props.getCollectorIcon();
        final String itemsTitle = com.mercadopago.model.Item
                .getItemsTitle(items, defaultMultipleTitle);
        final View render = new CollapsedItem(new CollapsedItem.Props(icon, itemsTitle)).render(parent);
        parent.addView(render);
    }

    private void addAmount(final ViewGroup parent,
                           final PaymentSettingRepository configuration) {
        final Amount.Props props = Amount.Props.from(this.props, configuration);
        final View view = new Amount(props, getActions())
                .render(parent);
        parent.addView(view);
    }

    private void addPaymentMethod(final ViewGroup parent,
                                  final PaymentSettingRepository configuration) {
        final View view =
                new PaymentMethod(PaymentMethod.Props.createFrom(props, configuration),
                        getActions()).render(parent);
        parent.addView(view);
    }

    private void addTermsAndConditions(final ViewGroup parent, Campaign campaign) {
        if (campaign != null) {
            final Context context = parent.getContext();
            TermsAndConditionsModel model = new TermsAndConditionsModel(campaign.getCampaignTermsUrl(),
                    context.getString(R.string.mpsdk_discount_terms_and_conditions_message),
                    context.getString(R.string.mpsdk_discount_terms_and_conditions_linked_message),
                    props.getPublicKey(),
                    LineSeparatorType.NONE);
            final View view = new TermsAndConditionsComponent(model)
                    .render(parent);
            parent.addView(view);
        }
    }

    private void addConfirmButton(final @Nonnull ViewGroup parent, final Discount discount) {
        final String confirm = parent.getContext().getString(R.string.mpsdk_confirm);
        final Button.Actions actions = new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                getActions().confirmPayment();
            }
        };
        final Button button = new ButtonPrimary(new Button.Props(confirm), actions);
        final View view = button.render(parent);
        final int resMargin = discount != null ? R.dimen.mpsdk_zero_height : R.dimen.mpsdk_m_margin;
        ViewUtils.setMarginTopInView(view, parent.getContext().getResources().getDimensionPixelSize(resMargin));
        parent.addView(view);
    }
}
