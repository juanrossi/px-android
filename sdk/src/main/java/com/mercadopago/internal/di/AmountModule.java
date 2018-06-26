package com.mercadopago.internal.di;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.internal.datasource.AmountService;
import com.mercadopago.internal.datasource.InstallmentService;
import com.mercadopago.internal.repository.AmountRepository;
import com.mercadopago.internal.repository.PaymentSettingRepository;
import com.mercadopago.internal.repository.UserSelectionRepository;

public class AmountModule extends ActivityModule implements AmountComponent {

    @NonNull
    private final ConfigurationModule configurationModule;

    public AmountModule(final Context context) {
        super(context);
        configurationModule = new ConfigurationModule(context);
    }

    @Override
    public AmountRepository getAmountRepository() {
        final PaymentSettingRepository configuration = configurationModule.getConfiguration();
        final UserSelectionRepository userSelectionRepository = configurationModule.getUserSelectionRepository();
        return new AmountService(configuration,
            configurationModule.getChargeSolver(),
            new InstallmentService(userSelectionRepository));
    }

    @NonNull
    public ConfigurationModule getConfigurationModule() {
        return configurationModule;
    }
}
