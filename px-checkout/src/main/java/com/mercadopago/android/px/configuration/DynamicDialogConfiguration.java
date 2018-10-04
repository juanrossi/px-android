package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import java.util.HashMap;
import java.util.Map;

// Used by single player to inform charges and other payment special information.
// Single player usecase depends on amount and payment method.
@SuppressWarnings("unused")
public final class DynamicDialogConfiguration {

    private final Map<DialogLocation, DynamicDialogCreator> creators;

    public enum DialogLocation {
        ENTER_REVIEW_AND_CONFIRM,
    }

    /* default */ DynamicDialogConfiguration(@NonNull final Builder builder) {
        creators = builder.creators;
    }

    @Nullable
    public DynamicDialogCreator getCreatorFor(@NonNull final DialogLocation dialogLocation) {
        return creators.get(dialogLocation);
    }

    public boolean hasCreatorFor(@NonNull final DialogLocation fragmentLocation) {
        return creators.containsKey(fragmentLocation);
    }

    public static final class Builder {

        /* default */ Map<DialogLocation, DynamicDialogCreator> creators;

        public Builder() {
            creators = new HashMap<>();
        }

        /**
         * @param location where dynamic dialog will be placed.
         * @param dynamicDialogCreator your creator.
         */
        public Builder addDynamicCreator(@NonNull final DialogLocation location,
            @NonNull final DynamicDialogCreator dynamicDialogCreator) {
            creators.put(location, dynamicDialogCreator);
            return this;
        }

        public DynamicDialogConfiguration build() {
            return new DynamicDialogConfiguration(this);
        }
    }
}
