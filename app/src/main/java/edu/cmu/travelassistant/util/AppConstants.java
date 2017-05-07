package edu.cmu.travelassistant.util;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by poojith on 5/5/17.
 */

public class AppConstants {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/search/";
    public static TravelAPI initAPI(final Context context) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder ongoing = chain.request().newBuilder();
                return chain.proceed(ongoing.build());
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(TravelAPI.class);
    }
}
