package com.sensorberg.sdk.internal;

import android.net.Uri;
import android.os.Build;

import com.sensorberg.sdk.BuildConfig;

import java.net.URL;

public class URLFactory {

    private static final String PRODUCTION_BASE_URL     = "connect.sensorberg.com";
    private static final String STAGING_BASE_URL        = "staging-connect.sensorberg.com";
    private static final String TEST_BASE_URL           = "test-connect.sensorberg.com";
    private static final String BIGBANG_BASE_URL        = "172.17.12.103:8091";

    private static final String PRODUCTION_RESOLVER_URL = "https://resolver.sensorberg.com/layout";
    private static final String STAGING_RESOLVER_URL    = "https://staging-resolver.sensorberg.com/layout";
    private static final String TEST_RESOLVER_URL       = "https://test-resolver.sensorberg.com/layout";
    private static final String BIGBANG_RESOLVER_URL    = "http://172.17.12.103:8093/layout";



    private static String SCHEME = "https";

    private static String BASE_URL = PRODUCTION_BASE_URL;
    private static String customResolverURL = PRODUCTION_RESOLVER_URL;

    public static void switchToProductionEnvironment(){
        BASE_URL = PRODUCTION_BASE_URL;
        SCHEME = "https";
        customResolverURL = PRODUCTION_RESOLVER_URL;
    }

    public static void switchToStagingEnvironment(){
        BASE_URL = STAGING_BASE_URL;
        SCHEME = "https";
        customResolverURL = STAGING_RESOLVER_URL;
    }

    public static void switchToTestEnvironment(){
        BASE_URL = TEST_BASE_URL;
        SCHEME = "https";
        customResolverURL = TEST_RESOLVER_URL;
    }

    public static void switchToBigbangEnvironment(){
        BASE_URL = BIGBANG_BASE_URL;
        SCHEME = "http";
        customResolverURL = BIGBANG_RESOLVER_URL;
    }

    public static void switchToMockEnvironment(URL url) {
        BASE_URL = url.getHost() + ":" +url.getPort();
        SCHEME = url.getProtocol();
        customResolverURL = null;
    }

    private static Uri.Builder BaseUri() {
        return new Uri.Builder()
                .scheme(SCHEME)
                .encodedAuthority(BASE_URL)
                .appendPath("api");
    }

    public static String getPingURL() {
        return BaseUri().appendEncodedPath("status/version").build().toString();
    }

    public static String getSettingsURLString(String apiKey) {
        return getSettingsURLString(null, apiKey);
    }

    public static String getSettingsURLString(Long revision, String apiKey){
        Uri.Builder builder = BaseUri()
                .appendEncodedPath("applications/")
                .appendPath(apiKey)
                .appendPath("settings")
                .appendPath("android")
                .appendPath(BuildConfig.SDK_VERSION).appendPath(Build.VERSION.RELEASE).appendPath(Build.MANUFACTURER)
                .appendPath(android.os.Build.MODEL + ":" + android.os.Build.PRODUCT);

        if (revision != null) {
            builder.appendQueryParameter("revision", revision.toString());
        }
        return builder.toString();
    }

    public static String getResolveURLString() {
        if (customResolverURL != null){
            return customResolverURL;
        }
        return BaseUri().appendPath("layout").toString();
    }

    public static void setLayoutURL(String newResolverURL) {
        if (newResolverURL == null) {
            customResolverURL = PRODUCTION_RESOLVER_URL;
            return;
        }
        customResolverURL = newResolverURL;
    }
}
