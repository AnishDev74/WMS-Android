package com.tmotions.wms.api;


import static com.tmotions.wms.common.Constants.BASE_URL;

import android.content.Context;
import android.util.Log;

import com.tmotions.wms.BuildConfig;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final Retrofit retrofit2 = null;

    private static final OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder()
                    .readTimeout(500, TimeUnit.SECONDS)
                    .connectTimeout(500, TimeUnit.SECONDS);
    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    private RetrofitClient(Context context) {
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder().
                        method(originalRequest.method(), originalRequest.body());
                okhttp3.Response response = chain.proceed(builder.build());
                Log.e("server response ---->> ", "" + response);
                return response;
            }
        });

        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            // add logging as last interceptor
            httpClient.addNetworkInterceptor(logging);
        }

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
//                        .client(httpClient.build()).build();
                .client(getUnsafeOkHttpClient().build()).build();

    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null)
            new RetrofitClient(context);
        return retrofit;
    }

    public static Retrofit getClientFile(Context context) {
        if (retrofit2 == null)
            new RetrofitClient(context);
        return retrofit2;
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(500, TimeUnit.SECONDS);
            builder.connectTimeout(500, TimeUnit.SECONDS);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder().
                            method(originalRequest.method(), originalRequest.body());
                    okhttp3.Response response = chain.proceed(builder.build());
                    Log.e("server response ---->> ", "" + response);
                    return response;
                }
            });
            builder.addNetworkInterceptor(logging);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
