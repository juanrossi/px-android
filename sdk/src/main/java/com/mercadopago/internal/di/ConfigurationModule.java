package com.mercadopago.internal.di;

import android.content.Context;
import com.mercadopago.internal.datasource.ChargeService;
import com.mercadopago.internal.datasource.PaymentSettingService;
import com.mercadopago.internal.datasource.UserSelectionService;
import com.mercadopago.internal.repository.ChargeRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;

public class ConfigurationModule extends ActivityModule implements ConfigurationComponent,
    UserSelectionComponent,
    ChargeSolverComponent {

    public ConfigurationModule(final Context context) {
        super(context);
    }

    @Override
    public UserSelectionRepository getUserSelectionRepository() {
        return new UserSelectionService(getSharedPreferences(), getJsonUtil());
    }

    @Override
    public PaymentSettingRepository getConfiguration() {
        return new PaymentSettingService(getSharedPreferences(), getJsonUtil());
    }

    @Override
    public ChargeRepository getChargeSolver() {
        return new ChargeService(getUserSelectionRepository(), getConfiguration());
    }
}
