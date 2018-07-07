package com.mercadopago.core;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.mercadopago.internal.di.Session;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.services.callbacks.Callback;
import com.mercadopago.android.px.services.core.Settings;
import com.mercadopago.android.px.services.exceptions.ApiException;
import com.mercadopago.android.px.services.CheckoutService;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.plugins.DataInitializationTask;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.util.TextUtils;
import java.util.Map;

class PrefetchService {

    private final Handler mainHandler;

    /* default */ final Session session;
    /* default */ final CheckoutLazyBuilder checkoutLazyBuilderCallback;
    /* default */ final MercadoPagoCheckout checkout;
    private Thread currentFetch;

    /* default */ PrefetchService(final MercadoPagoCheckout checkout, final Session session,
        final CheckoutLazyBuilder checkoutLazyBuilderCallback) {
        session.init(checkout);
        this.checkout = checkout;
        this.session = session;
        this.checkoutLazyBuilderCallback = checkoutLazyBuilderCallback;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void prefetch() {

        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        currentFetch = new Thread(new Runnable() {
            @Override
            public void run() {

                final String checkoutPreferenceId =
                    paymentSettings.getCheckoutPreferenceId();
                if (!TextUtils.isEmpty(checkoutPreferenceId)) {
                    fetchPreference();
                } else {
                    fetchDiscounts();
                }
            }
        });
        currentFetch.start();
    }

    /* default */ void fetchPreference() {
        //TODO ADD PREFERENCE SERVICE.
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        session.getRetrofitClient().create(CheckoutService.class)
            .getPreference(Settings.servicesVersion, paymentSettings.getCheckoutPreferenceId(),
                paymentSettings.getPublicKey())
            .execute(
                new Callback<CheckoutPreference>() {
                    @Override
                    public void success(final CheckoutPreference checkoutPreference) {
                        paymentSettings.configure(checkoutPreference);
                        fetchDiscounts();
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        //TODO Track
                        postError();
                    }
                });
    }

    /* default */ void fetchDiscounts() {
        session.getDiscountRepository()
            .configureDiscountAutomatically(session.getAmountRepository().getAmountToPay()).execute(
            new Callback<Boolean>() {
                @Override
                public void success(final Boolean automatic) {
                    initPlugins();
                }

                @Override
                public void failure(final ApiException apiException) {
                    //TODO Track
                    postError();
                }
            });
    }

    /* default */ void initPlugins() {
        //TODO refactor - muy turbio todo.
        final DataInitializationTask dataInitializationTask =
            CheckoutStore.getInstance().getDataInitializationTask();
        if (dataInitializationTask != null) {
            dataInitializationTask.initPlugins(new DataInitializationTask.DataInitializationCallbacks() {
                @Override
                public void onDataInitialized(@NonNull final Map<String, Object> data) {
                    data.put(DataInitializationTask.KEY_INIT_SUCCESS, true);
                    fetchGroups();
                }

                @Override
                public void onFailure(@NonNull final Exception e, @NonNull final Map<String, Object> data) {
                    data.put(DataInitializationTask.KEY_INIT_SUCCESS, false);
                    fetchGroups();
                }
            });
        } else {
            fetchGroups();
        }
    }

    /* default */ void fetchGroups() {
        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                postSuccess();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO Track
                postError();
            }
        });
    }

    /* default */ void postSuccess() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                checkout.prefetch = true;
                checkoutLazyBuilderCallback.success(checkout);
            }
        });
    }

    /* default */ void postError() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                checkoutLazyBuilderCallback.fail();
            }
        });
    }

    public void cancel() {
        if (currentFetch != null && currentFetch.isAlive() && !currentFetch.isInterrupted()) {
            currentFetch.interrupt();
        }
    }
}
