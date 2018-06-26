package com.mercadopago.utils;

import android.os.Bundle;
import android.support.v4.util.Pair;
import com.mercadopago.SampleTopFragment;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.example.R;
import com.mercadopago.model.Payment;
import com.mercadopago.plugins.MainPaymentProcessor;
import com.mercadopago.plugins.SamplePaymentMethodPlugin;
import com.mercadopago.plugins.model.BusinessPayment;
import com.mercadopago.plugins.model.ExitAction;
import java.util.ArrayList;
import java.util.Collection;

import static com.mercadopago.utils.ExamplesUtils.createBase;

final class BusinessSamples {

    private static final String BUTTON_PRIMARY_NAME = "ButtonPrimaryName";
    private static final String BUTTON_SECONDARY_NAME = "ButtonSecondaryName";

    private BusinessSamples() {
    }

    public static Collection<? extends Pair<String, MercadoPagoCheckout.Builder>> getAll() {
        final Collection<Pair<String, MercadoPagoCheckout.Builder>> options = new ArrayList<>();
        options.add(new Pair<>("Business - Complete - Rejected", startCompleteRejectedBusiness()));
        options.add(new Pair<>("Business - Complete - Approved", startCompleteApprovedBusiness()));
        options.add(new Pair<>("Business - Primary And Help - Pending", startCompletePendingBusiness()));
        options.add(new Pair<>("Business - No help - Pending", startPendingBusinessNoHelp()));
        options.add(
            new Pair<>("Business - NoHelp w/pm - Approved", startCompleteApprovedBusinessWithPaymentMethodNoHelp()));
        return options;
    }

    private static MercadoPagoCheckout.Builder startCompleteRejectedBusiness() {
        final Bundle args = new Bundle();
        args.putParcelable(SampleTopFragment.SOME_PARCELABLE, new SampleTopFragment.ParcelableArgument("SOME_LABEL"));

        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Decorator.REJECTED, Payment.StatusCodes.STATUS_REJECTED,
                Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER,
                R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setReceiptId("#123455")
                .setTopFragment(SampleTopFragment.class, args)
                .setPaymentMethodVisibility(true)
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    static MercadoPagoCheckout.Builder startCompleteApprovedBusiness() {
        final Bundle args = new Bundle();
        args.putParcelable(SampleTopFragment.SOME_PARCELABLE, new SampleTopFragment.ParcelableArgument("SOME_LABEL"));
        final BusinessPayment payment = new BusinessPayment.Builder(BusinessPayment.Decorator.APPROVED,
            Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED,
            "https://www.jqueryscript.net/images/Simplest-Responsive-jQuery-Image-Lightbox-Plugin-simple-lightbox.jpg",
            "Title")
            .setHelp("Help description!")
            .setReceiptId("#123455")
            .setTopFragment(SampleTopFragment.class, args)
            .setStatementDescription("PEDRO")
            .setPaymentMethodVisibility(true)
            .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
            .build();

        return customBusinessPayment(payment);
    }

    private static MercadoPagoCheckout.Builder startCompleteApprovedBusinessWithPaymentMethodNoHelp() {
        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Decorator.APPROVED,
                Payment.StatusCodes.STATUS_APPROVED,
                Payment.StatusDetail.STATUS_DETAIL_ACCREDITED, R.drawable.mpsdk_icon_card, "Title")
                .setReceiptId("#123455")
                .setPaymentMethodVisibility(true)
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static MercadoPagoCheckout.Builder startCompletePendingBusiness() {
        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Decorator.PENDING,
                Payment.StatusCodes.STATUS_PENDING,
                Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT,
                R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .build();

        return customBusinessPayment(payment);
    }

    private static MercadoPagoCheckout.Builder startPendingBusinessNoHelp() {
        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Decorator.PENDING,
                Payment.StatusCodes.STATUS_PENDING,
                Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT,
                R.drawable.mpsdk_icon_card, "Title")
                .setReceiptId("#123455")
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static MercadoPagoCheckout.Builder customBusinessPayment(final BusinessPayment businessPayment) {
        final MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(businessPayment);
        return createBase().setPaymentProcessor(mainPaymentProcessor)
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }
}
