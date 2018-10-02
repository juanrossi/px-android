package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.core.DynamicFragmentCreator;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class DynamicFragmentConfiguration {

    private final Map<FragmentLocation, DynamicFragmentCreator> creators;

    public enum FragmentLocation {
        TOP_PAYMENT_METHOD_REVIEW_AND_CONFIRM,
        BOTTOM_PAYMENT_METHOD_REVIEW_AND_CONFIRM
    }

    /* default */ DynamicFragmentConfiguration(@NonNull final Builder builder) {
        creators = builder.creators;
    }

    @Nullable
    public DynamicFragmentCreator getCreatorFor(@NonNull final FragmentLocation fragmentLocation) {
        return creators.get(fragmentLocation);
    }

    public boolean hasCreatorFor(@NonNull final FragmentLocation fragmentLocation) {
        return creators.containsKey(fragmentLocation);
    }

    public static final class Builder {

        /* default */ Map<FragmentLocation, DynamicFragmentCreator> creators;

        public Builder() {
            creators = new HashMap<>();
        }

        /**
         * @param location where dynamic fragment will be placed.
         * @param dynamicFragmentCreator your creator.
         */
        public Builder addDynamicCreator(@NonNull final FragmentLocation location,
            @NonNull final DynamicFragmentCreator dynamicFragmentCreator) {
            creators.put(location, dynamicFragmentCreator);
            return this;
        }

        public DynamicFragmentConfiguration build() {
            return new DynamicFragmentConfiguration(this);
        }
    }
}
