package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;

public class Sites {

    public static final Site ARGENTINA =
        new Site("MLA", "ARS", "https://www.mercadopago.com.ar/ayuda/terminos-y-condiciones_299");
    public static final Site BRASIL =
        new Site("MLB", "BRL", "https://www.mercadopago.com.br/ajuda/termos-e-condicoes_300");
    public static final Site CHILE =
        new Site("MLC", "CLP", "https://www.mercadopago.cl/ayuda/terminos-y-condiciones_299");
    public static final Site MEXICO =
        new Site("MLM", "MXN", "https://www.mercadopago.com.mx/ayuda/terminos-y-condiciones_715");
    public static final Site COLOMBIA =
        new Site("MCO", "COP", "https://www.mercadopago.com.co/ayuda/terminos-y-condiciones_299");
    public static final Site VENEZUELA =
        new Site("MLV", "VES", "https://www.mercadopago.com.ve/ayuda/terminos-y-condiciones_299");
    public static final Site USA = new Site("USA", "USD", "");
    public static final Site PERU =
        new Site("MPE", "PEN", "https://www.mercadopago.com.pe/ayuda/terminos-condiciones-uso_2483");

    private Sites() {
    }

    @NonNull
    public static Site getById(@NonNull final String siteId) {
        Site site;
        if (Sites.ARGENTINA.getId().equals(siteId)) {
            site = Sites.ARGENTINA;
        } else if (Sites.BRASIL.getId().equals(siteId)) {
            site = Sites.BRASIL;
        } else if (Sites.CHILE.getId().equals(siteId)) {
            site = Sites.CHILE;
        } else if (Sites.MEXICO.getId().equals(siteId)) {
            site = Sites.MEXICO;
        } else if (Sites.COLOMBIA.getId().equals(siteId)) {
            site = Sites.COLOMBIA;
        } else if (Sites.VENEZUELA.getId().equals(siteId)) {
            site = Sites.VENEZUELA;
        } else if (Sites.USA.getId().equals(siteId)) {
            site = Sites.USA;
        } else if (Sites.PERU.getId().equals(siteId)) {
            site = Sites.PERU;
        } else {
            throw new IllegalArgumentException("There is no site for that id");
        }
        return site;
    }
}
