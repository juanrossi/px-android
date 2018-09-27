package com.mercadopago.android.px.internal.core;

public final class Settings {

    private Settings() {
        //Do nothing
    }

    public static String servicesVersion = "v1";

    @Deprecated
    public static void enableBetaServices() {
        servicesVersion = "beta";
    }
}