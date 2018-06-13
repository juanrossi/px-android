package com.mercadopago.review_and_confirm.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.components.ActionDispatcher;
import com.mercadopago.components.CustomComponent;
import com.mercadopago.components.Renderer;
import com.mercadopago.components.RendererFactory;
import com.mercadopago.components.TermsAndConditionsComponent;
import com.mercadopago.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.review_and_confirm.components.items.ReviewItems;
import com.mercadopago.review_and_confirm.components.payment_method.PaymentMethodComponent;
import com.mercadopago.review_and_confirm.models.PaymentModel;

public class ReviewAndConfirmRenderer extends Renderer<ReviewAndConfirmContainer> {

    @Override
    protected View render(@NonNull final ReviewAndConfirmContainer component,
                          @NonNull final Context context,
                          @Nullable final ViewGroup parent) {

        LinearLayout linearLayout = createMainLayout(context);

        addSummary(component, linearLayout);

        if (component.hasDiscountTermsAndConditions()) {
            addDiscountTermsAndConditions(component, linearLayout);
        }

        if (component.hasItemsEnabled()) {
            addReviewItems(component, linearLayout);
        }

        if (component.props.preferences.hasCustomTopView()) {
            CustomComponent topComponent = component.props.preferences.getTopComponent();
            topComponent.setDispatcher(component.getDispatcher());
            Renderer renderer = RendererFactory.create(context, topComponent);
            renderer.render(linearLayout);
        }

        addPaymentMethod(component.props.paymentModel, component.getDispatcher(), linearLayout);

        if (component.props.preferences.hasCustomBottomView()) {
            CustomComponent bottomComponent = component.props.preferences.getBottomComponent();
            bottomComponent.setDispatcher(component.getDispatcher());
            Renderer renderer = RendererFactory.create(context, bottomComponent);
            renderer.render(linearLayout);
        }

        if (component.hasMercadoPagoTermsAndConditions()) {
            addTermsAndConditions(component, linearLayout);
        }

        parent.addView(linearLayout);

        return parent;
    }

    private void addSummary(@NonNull ReviewAndConfirmContainer component, LinearLayout linearLayout) {
        Renderer summary = RendererFactory.create(linearLayout.getContext(),
                new SummaryComponent(SummaryComponent.SummaryProps.createFrom(component.props.summaryModel,
                        component.props.preferences),
                        component.getSummaryProvider()));
        summary.render(linearLayout);
    }

    private void addDiscountTermsAndConditions(@NonNull ReviewAndConfirmContainer component, final ViewGroup parent) {

        TermsAndConditionsComponent discountTermsAndConditionsComponent = new TermsAndConditionsComponent(component.props.discountTermsAndConditionsModel);

        View discountTermsAndConditionsView = discountTermsAndConditionsComponent.render(parent);
        parent.addView(discountTermsAndConditionsView);
    }

    @NonNull
    private LinearLayout createMainLayout(@NonNull final Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return linearLayout;
    }

    private void addPaymentMethod(final PaymentModel paymentModel, final ActionDispatcher dispatcher, final ViewGroup parent) {
        PaymentMethodComponent paymentMethodComponent = new PaymentMethodComponent(paymentModel, new PaymentMethodComponent.Actions() {
            @Override
            public void onPaymentMethodChangeClicked() {
                dispatcher.dispatch(new ChangePaymentMethodAction());
            }
        });

        View paymentView = paymentMethodComponent.render(parent);
        parent.addView(paymentView);
    }

    private void addReviewItems(@NonNull final ReviewAndConfirmContainer component, final ViewGroup parent) {
        ReviewItems reviewItems = new ReviewItems(
            new ReviewItems.Props(
                component.props.itemsModel,
                component.props.preferences.getCollectorIcon(),
                component.props.preferences.getQuantityLabel(),
                component.props.preferences.getUnitPriceLabel()));
        View view = reviewItems.render(parent);
        parent.addView(view);
    }

    private void addTermsAndConditions(@NonNull final ReviewAndConfirmContainer component,
                                       final ViewGroup parent) {

        TermsAndConditionsComponent termsAndConditionsComponent = new TermsAndConditionsComponent(component.props.mercadoPagoTermsAndConditionsModel);

        View discountTermsAndConditionsView = termsAndConditionsComponent.render(parent);
        parent.addView(discountTermsAndConditionsView);
    }

}
