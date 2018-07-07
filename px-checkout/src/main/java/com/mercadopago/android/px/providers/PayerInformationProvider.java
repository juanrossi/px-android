package com.mercadopago.android.px.providers;

import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.mvp.ResourcesProvider;
import com.mercadopago.android.px.mvp.TaggedCallback;
import java.util.List;

/**
 * Created by mromar on 22/09/17.
 */

public interface PayerInformationProvider extends ResourcesProvider {

    void getIdentificationTypesAsync(final TaggedCallback<List<IdentificationType>> taggedCallback);

    String getInvalidIdentificationNumberErrorMessage();

    String getInvalidIdentificationNameErrorMessage();

    String getInvalidIdentificationLastNameErrorMessage();

    String getInvalidIdentificationBusinessNameErrorMessage();

    String getMissingPublicKeyErrorMessage();

    String getMissingIdentificationTypesErrorMessage();
}
