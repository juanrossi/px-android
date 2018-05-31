package com.mercadopago.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;
import com.mercadopago.components.CustomComponent;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoCheckout.Builder;
import com.mercadopago.example.R;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Item;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentTypes;
import com.mercadopago.model.Sites;
import com.mercadopago.plugins.DataInitializationTask;
import com.mercadopago.plugins.MainPaymentProcessor;
import com.mercadopago.plugins.SamplePaymentMethodPlugin;
import com.mercadopago.plugins.components.SampleCustomComponent;
import com.mercadopago.plugins.model.BusinessPayment;
import com.mercadopago.plugins.model.ExitAction;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.review_and_confirm.models.ReviewAndConfirmPreferences;
import com.mercadopago.tracking.listeners.TracksListener;
import com.mercadopago.tracking.tracker.MPTracker;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;

public class ExamplesUtils {

    private static final String REQUESTED_CODE_MESSAGE = "Requested code: ";
    private static final String PAYMENT_WITH_STATUS_MESSAGE = "Payment with status: ";
    private static final String BUTTON_PRIMARY_NAME = "ButtonPrimaryName";
    private static final String BUTTON_SECONDARY_NAME = "ButtonSecondaryName";
    private static final String RESULT_CODE_MESSAGE = " Result code: ";
    private static final String DUMMY_PREFERENCE_ID = "243962506-0bb62e22-5c7b-425e-a0a6-c22d0f4758a9";
    private static final String DUMMY_PREFERENCE_ID_WITH_TWO_ITEMS = "243962506-b6476e8b-a1a4-40cb-bfec-9954bff4a143";
    private static final String DUMMY_PREFERENCE_ID_WITH_ITEM_LONG_TITLE = "243962506-4ddac80d-af86-4a4f-80e3-c4e4735ba200";
    private static final String DUMMY_PREFERENCE_ID_WITH_DECIMALS = "243962506-ad5df092-f5a2-4b99-bcc4-7578d6e71849";
    private static final String DUMMY_PREFERENCE_ID_WITH_NO_DECIMALS = "243966003-3db6717c-371a-4660-8d01-ebf63f588fd8";
    private static final String DUMMY_PREFERENCE_ID_MLM = "253812950-1f347959-4ed9-4fee-9189-0eb84fe8f049";
    private static final String DUMMY_MERCHANT_PUBLIC_KEY = "TEST-c6d9b1f9-71ff-4e05-9327-3c62468a23ee";
    private static final String DUMMY_MERCHANT_PUBLIC_KEY_MLM = "TEST-0f375857-0881-447c-9b2b-23e97b93f947";

    public static void resolveCheckoutResult(final Activity context, final int requestCode, final int resultCode,
        final Intent data) {
        LayoutUtil.showRegularLayout(context);

        if (requestCode == MercadoPagoCheckout.CHECKOUT_REQUEST_CODE) {
            if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
                Payment payment = JsonUtil.getInstance().fromJson(data.getStringExtra("payment"), Payment.class);
                Toast.makeText(context, new StringBuilder()
                    .append(PAYMENT_WITH_STATUS_MESSAGE)
                    .append(payment.getStatus()), Toast.LENGTH_LONG)
                    .show();
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null && data.getStringExtra("mercadoPagoError") != null) {
                    MercadoPagoError mercadoPagoError = JsonUtil.getInstance()
                        .fromJson(data.getStringExtra("mercadoPagoError"), MercadoPagoError.class);
                    Toast.makeText(context, "Error: " + mercadoPagoError.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, new StringBuilder()
                        .append("Cancel - ")
                        .append(REQUESTED_CODE_MESSAGE)
                        .append(requestCode)
                        .append(RESULT_CODE_MESSAGE)
                        .append(resultCode), Toast.LENGTH_LONG)
                        .show();
                }
            } else {

                Toast.makeText(context, new StringBuilder()
                    .append(REQUESTED_CODE_MESSAGE)
                    .append(requestCode)
                    .append(RESULT_CODE_MESSAGE)
                    .append(resultCode), Toast.LENGTH_LONG)
                    .show();
            }
        }
    }

    public static List<Pair<String, Builder>> getOptions() {
        List<Pair<String, Builder>> options = new ArrayList<>();
        options.add(new Pair<>("Discount", discountSample()));
        options.add(new Pair<>("Review and Confirm - Custom exit", customExitReviewAndConfirm()));
        options.add(new Pair<>("Business - Complete - Rejected", startCompleteRejectedBusiness()));
        options.add(new Pair<>("Business - Secondary And Help - Approved", startCompleteApprovedBusiness()));
        options.add(new Pair<>("Business - Primary And Help - Pending", startCompletePendingBusiness()));
        options.add(new Pair<>("Business - No help - Pending", startPendingBusinessNoHelp()));
        options
            .add(new Pair<>("Business - Complete w/pm - Approved", startCompleteApprovedBusinessWithPaymentMethod()));
        options.add(
            new Pair<>("Business - NoHelp w/pm - Approved", startCompleteApprovedBusinessWithPaymentMethodNoHelp()));
        options.add(new Pair<>("Base flow - Tracks with listener", startBaseFlowWithTrackListener()));
        options.add(new Pair<>("All but debit card", allButDebitCard()));
        options.add(new Pair<>("Two items", createBaseWithTwoItems()));
        options.add(new Pair<>("Two items - Collector icon", createBaseWithTwoItemsAndCollectorIcon()));
        options.add(new Pair<>("One item - Long title", createBaseWithOneItemLongTitle()));
        options.add(new Pair<>("Account money plugin - Decimals", createBaseWithAccountMoneyAndDecimals()));
        options.add(new Pair<>("Account money plugin - No decimals", createBaseWithAccountMoneyAndNoDecimals()));
        options.add(new Pair<>("Account money plugin - Discount percent off", createBaseWithAccountMoneyAndDiscountPercentOff()));
        options.add(new Pair<>("Account money plugin - Discount amount off", createBaseWithAccountMoneyAndDiscountAmountOff()));
        return options;
    }

    private static Builder allButDebitCard() {
        Item item = new Item("Aaaa", 1, new BigDecimal(10));
        item.setId("123");
        item.setCurrencyId("ARS");

        CheckoutPreference.Builder builder = new CheckoutPreference.Builder(Sites.ARGENTINA, "a@a.a",
            Collections.singletonList(item));

        builder.enableAccountMoney(); // to not exclude double account money.
        for (String type : PaymentTypes.getAllPaymentTypes()) {
            if (!PaymentTypes.DEBIT_CARD.equals(type)) {
                builder.addExcludedPaymentType(type);
            }
        }

        return createBase(builder.build())
            .setFlowPreference(new FlowPreference.Builder().exitOnPaymentMethodChange().build());
    }

    private static Builder startCompleteRejectedBusiness() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.REJECTED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setReceiptId("#123455")
                .setPaymentMethodVisibility(true)
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static Builder startCompleteApprovedBusinessWithPaymentMethod() {
        BusinessPayment payment = new BusinessPayment.Builder(BusinessPayment.Status.APPROVED,
            "https://www.jqueryscript.net/images/Simplest-Responsive-jQuery-Image-Lightbox-Plugin-simple-lightbox.jpg",
            "Title")
            .setHelp("Help description!")
            .setReceiptId("#123455")
            .setStatementDescription("PEDRO")
            .setPaymentMethodVisibility(true)
            .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
            .build();

        return customBusinessPayment(payment);
    }

    private static Builder startCompleteApprovedBusinessWithPaymentMethodNoHelp() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setReceiptId("#123455")
                .setPaymentMethodVisibility(true)
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static Builder startCompleteApprovedBusiness() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static Builder startCompletePendingBusiness() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.PENDING, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .build();

        return customBusinessPayment(payment);
    }

    private static Builder startPendingBusinessNoHelp() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.PENDING, R.drawable.mpsdk_icon_card, "Title")
                .setReceiptId("#123455")
                .setPrimaryButton(new ExitAction(BUTTON_PRIMARY_NAME, 23))
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();

        return customBusinessPayment(payment);
    }

    private static Builder customBusinessPayment(final BusinessPayment businessPayment) {
        MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(businessPayment);
        return createBase().setPaymentProcessor(mainPaymentProcessor)
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }

    private static Builder customExitReviewAndConfirm() {
        CustomComponent.Props props = new CustomComponent.Props(new HashMap<String, Object>(), null);
        ReviewAndConfirmPreferences preferences = new ReviewAndConfirmPreferences.Builder()
            .setTopComponent(new SampleCustomComponent(props)).build();
        return createBaseWithDecimals().setReviewAndConfirmPreferences(preferences);
    }

    private static Builder discountSample() {
        Discount.Builder discountBuilder = new Discount.Builder("77123", "ARS", new BigDecimal(20));
        discountBuilder.setPercentOff(new BigDecimal(20));

        Campaign.Builder campaignBuilder = new Campaign.Builder("77123");
        campaignBuilder.setMaxCouponAmount(new BigDecimal(200));

        return createBase().setDiscount(discountBuilder.build(), campaignBuilder.build());
    }

    private static Builder startBaseFlowWithTrackListener() {
        MPTracker.getInstance().setTracksListener(new TracksListener<HashMap<String, String>>() {

            @Override
            public void onScreenLaunched(@NonNull final String screenName,
                @NonNull final Map<String, String> extraParams) {
                Log.d("Screen track: ", screenName + " " + extraParams);
            }

            @Override
            public void onEvent(@NonNull final HashMap<String, String> event) {
                Log.d("Event track: ", event.toString());
            }
        });
        return createBase();
    }

    private static Builder createBase(@NonNull final CheckoutPreference checkoutPreference) {
        final Map<String, Object> defaultData = new HashMap<>();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, checkoutPreference)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    public static Builder createBase() {
        final Map<String, Object> defaultData = new HashMap<>();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    public static Builder createBaseWithMLM() {
        final Map<String, Object> defaultData = new HashMap<>();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY_MLM, DUMMY_PREFERENCE_ID_MLM)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithDecimals() {
        final Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("amount", 120f);

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID_WITH_DECIMALS)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithTwoItems() {
        final Map<String, Object> defaultData = new HashMap<>();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID_WITH_TWO_ITEMS)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithTwoItemsAndCollectorIcon() {
        final Map<String, Object> defaultData = new HashMap<>();

        final ReviewAndConfirmPreferences preferences = new ReviewAndConfirmPreferences.Builder()
            .setCollectorIcon(R.drawable.mpsdk_collector_icon)
            .build();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID_WITH_TWO_ITEMS)
            .setReviewAndConfirmPreferences(preferences)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithOneItemLongTitle() {
        final Map<String, Object> defaultData = new HashMap<>();

        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID_WITH_ITEM_LONG_TITLE)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithNoDecimals() {
        final Map<String, Object> defaultData = new HashMap<>();
        return new Builder(DUMMY_MERCHANT_PUBLIC_KEY, DUMMY_PREFERENCE_ID_WITH_NO_DECIMALS)
            .setDataInitializationTask(getDataInitializationTask(defaultData));
    }

    private static Builder createBaseWithAccountMoneyAndDecimals() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();
        MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(payment);
        return createBaseWithDecimals().setPaymentProcessor(mainPaymentProcessor)
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }

    private static Builder createBaseWithAccountMoneyAndNoDecimals() {
        BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();
        MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(payment);
        return createBaseWithNoDecimals().setPaymentProcessor(mainPaymentProcessor)
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }

    private static Builder createBaseWithAccountMoneyAndDiscountPercentOff() {
        final Discount.Builder discount = new Discount.Builder("77123", "ARS", new BigDecimal("245.76"))
            .setPercentOff(new BigDecimal(20));
        final Campaign.Builder campaignBuilder = new Campaign.Builder("77123");
        campaignBuilder.setMaxCouponAmount(new BigDecimal(200));
        // The payer pays $ 983,04

        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();
        final MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(payment);
        return createBase(new CheckoutPreference.Builder(Sites.ARGENTINA, "a@a.a", getItems()).build())
            .setPaymentProcessor(mainPaymentProcessor)
            .setDiscount(discount.build(), campaignBuilder.build())
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }

    private static Builder createBaseWithAccountMoneyAndDiscountAmountOff() {
        final Discount.Builder discount = new Discount.Builder("77123", "ARS", new BigDecimal("245.76"))
            .setAmountOff(new BigDecimal("245.76"));
        final Campaign.Builder campaignBuilder = new Campaign.Builder("77123");
        campaignBuilder.setMaxCouponAmount(new BigDecimal(200));
        // The payer pays $ 983,04

        final BusinessPayment payment =
            new BusinessPayment.Builder(BusinessPayment.Status.APPROVED, R.drawable.mpsdk_icon_card, "Title")
                .setHelp("Help description!")
                .setSecondaryButton(new ExitAction(BUTTON_SECONDARY_NAME, 34))
                .build();
        final MainPaymentProcessor mainPaymentProcessor = new MainPaymentProcessor(payment);
        return createBase(new CheckoutPreference.Builder(Sites.ARGENTINA, "a@a.a", getItems()).build())
            .setPaymentProcessor(mainPaymentProcessor)
            .setDiscount(discount.build(), campaignBuilder.build())
            .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor);
    }

    private static List<Item> getItems() {
        final Item itemSarasa = new Item("Item desc", new BigDecimal("1228.8"));
        itemSarasa.setId(Sites.ARGENTINA.getId());
        itemSarasa.setId("12345");
        itemSarasa.setTitle("Some title");
        itemSarasa.setCurrencyId(Sites.ARGENTINA.getCurrencyId());
        final List<Item> items = new ArrayList<>();
        items.add(itemSarasa);
        return items;
    }

    @NonNull
    private static DataInitializationTask getDataInitializationTask(final Map<String, Object> defaultData) {
        return new DataInitializationTask(defaultData) {
            @Override
            public void onLoadData(@NonNull final Map<String, Object> data) {
                data.put("user", "Nico");
            }
        };
    }
}