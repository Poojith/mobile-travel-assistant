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


//    public static RealTimeBusAPI initBusAPI(final Context context) {
//        final String PAC_BUS_STOPS_URL
//                = "http://truetime.portauthority.org/bustime/api/v3/getstops?key=Gg5eAVrmgNc3U5kC5PcFfcQGz&format=json&rtpidatafeed=Port%20Authority%20Bus";
//
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request.Builder ongoing = chain.request().newBuilder();
//                return chain.proceed(ongoing.build());
//            }
//        });
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(PAC_BUS_STOPS_URL)
//                .client(builder.build())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        return retrofit.create(RealTimeBusAPI.class);
//    }


}
