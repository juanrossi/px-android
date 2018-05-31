package com.mercadopago.onetap.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.R;
import com.mercadopago.components.Action;
import com.mercadopago.components.Button;
import com.mercadopago.components.ButtonPrimary;
import com.mercadopago.components.CompactComponent;
import com.mercadopago.components.TermsAndConditionsComponent;
import com.mercadopago.onetap.OneTap;
import com.mercadopago.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.viewmodel.OneTapModel;
import javax.annotation.Nonnull;

public class OneTapContainer extends CompactComponent<OneTapModel, OneTap.Actions> {

    public OneTapContainer(final OneTapModel oneTapModel, final OneTap.Actions callBack) {
        super(oneTapModel, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addItem(parent);
        addAmount(parent);
        addPaymentMethod(parent);
        addTermsAndConditions(parent);
        addConfirmButton(parent);
        return parent;
    }

    private void addItem(final ViewGroup parent) {
        final String defaultMultipleTitle = parent.getContext().getString(R.string.mpsdk_review_summary_products);
        final int icon =
            props.getCollectorIcon() == null ? R.drawable.mpsdk_review_item_default : props.getCollectorIcon();
        final String itemsTitle = com.mercadopago.model.Item
            .getItemsTitle(props.getCheckoutPreference().getItems(), defaultMultipleTitle);
        final View render = new CollapsedItem(new CollapsedItem.Props(icon, itemsTitle)).render(parent);
        parent.addView(render);
    }

    private void addAmount(final ViewGroup parent) {
        final Amount.Props props = Amount.Props.from(this.props);
        final View view = new Amount(props, getActions())
            .render(parent);
        parent.addView(view);
    }

    private void addPaymentMethod(final ViewGroup parent) {
        final View view =
            new PaymentMethod(PaymentMethod.Props.createFrom(props),
                getActions()).render(parent);
        parent.addView(view);
    }

    private void addTermsAndConditions(final ViewGroup parent) {
        if (props.getDiscount() != null) {
            final Context context = parent.getContext();
            TermsAndConditionsModel model = new TermsAndConditionsModel(props.getDiscount().getDiscountTermsUrl(),
                context.getString(R.string.mpsdk_discount_terms_and_conditions_message),
                context.getString(R.string.mpsdk_discount_terms_and_conditions_linked_message),
                LineSeparatorType.NONE);
            final View view = new TermsAndConditionsComponent(model)
                .render(parent);
            parent.addView(view);
        }
    }

    private void addConfirmButton(final @Nonnull ViewGroup parent) {
        final String confirm = parent.getContext().getString(R.string.mpsdk_pay);
        final Button.Actions actions = new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                getActions().confirmPayment();
            }
        };
        final Button button = new ButtonPrimary(new Button.Props(confirm), actions);
        parent.addView(button.render(parent));
    }
}
