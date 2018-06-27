package com.mercadopago.lite.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import com.mercadopago.lite.core.ConnectivityStateInterceptor;
import com.mercadopago.lite.core.Settings;
import com.mercadopago.lite.core.TLSSocketFactory;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class HttpClientUtil {

    private static OkHttpClient client;
    private static OkHttpClient customClient;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String CACHE_DIR_NAME = "PX_OKHTTP_CACHE_SERVICES";

    private HttpClientUtil() {
    }

    public static synchronized OkHttpClient getClient(@NonNull final Context context,
        final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {

        if (customClientSet()) {
            return customClient;
        } else {
            if (client == null) {
                client = createClient(context, connectTimeout, readTimeout, writeTimeout);
            }
            return client;
        }
    }

    @NonNull
    private static OkHttpClient createClient(@NonNull final Context context, final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {
        // Set log info
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(Settings.OKHTTP_LOGGING);

        // Set cache size
        Cache cache = null;
        try {
            cache =
                new okhttp3.Cache(new File(String.format("%s%s", context.getCacheDir().getPath(), CACHE_DIR_NAME)),
                    CACHE_SIZE);
        } catch (final Exception e) {
            // do nothing
        }

        // Set client
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(interceptor);

        okHttpClientBuilder.addInterceptor(getConnectionInterceptor(context));

        OkHttpClient client = okHttpClientBuilder.build();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            try {
                client = TLSSocketFactory.enforceTls(client);
            } catch (final Exception e) {
                // Do nothing
            }
        }

        return client;
    }

    @NonNull
    private static Interceptor getConnectionInterceptor(@NonNull final Context context) {
        return new ConnectivityStateInterceptor(
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    /**
     * Intended for testing proposes.
     *
     * @param client custom client
     */
    static void setCustomClient(final OkHttpClient client) {
        customClient = TLSSocketFactory.enforceTls(client);
    }

    /**
     * Intended for testing proposes.
     */
    static void removeCustomClient() {
        customClient = null;
    }

    private static boolean customClientSet() {
        return customClient != null;
    }
}
