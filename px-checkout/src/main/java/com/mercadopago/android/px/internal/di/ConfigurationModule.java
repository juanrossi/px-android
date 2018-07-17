package com.mercadopago.android.px.internal.di;

import android.content.Context;
import com.mercadopago.android.px.internal.datasource.ChargeService;
import com.mercadopago.android.px.internal.datasource.PaymentSettingService;
import com.mercadopago.android.px.internal.datasource.UserSelectionService;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;

public final class ConfigurationModule extends ApplicationModule implements ConfigurationComponent,
    UserSelectionComponent,
    ChargeSolverComponent {

    //Mem cache
    private UserSelectionRepository userSelectionRepository;
    private PaymentSettingRepository paymentSettingRepository;
    private ChargeRepository chargeRepository;

    public ConfigurationModule(final Context context) {
        super(context);
    }

    @Override
    public UserSelectionRepository getUserSelectionRepository() {
        if (userSelectionRepository == null) {
            userSelectionRepository = new UserSelectionService(getSharedPreferences(), getJsonUtil());
        }
        return userSelectionRepository;
    }

    @Override
    public PaymentSettingRepository getPaymentSettings() {
        if (paymentSettingRepository == null) {
            paymentSettingRepository = new PaymentSettingService(getSharedPreferences(), getJsonUtil());
        }
        return paymentSettingRepository;
    }

    @Override
    public ChargeRepository getChargeSolver() {
        if (chargeRepository == null) {
            chargeRepository = new ChargeService(getUserSelectionRepository(), getPaymentSettings());
        }
        return chargeRepository;
    }

    public void reset() {
        getUserSelectionRepository().reset();
        getPaymentSettings().reset();
    }
}
