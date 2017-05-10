package edu.cmu.travelassistant.util;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TravelAPI {

//    @GET("json?location=40.44,-79.94&radius=500&types=bus_station&sensor=false")
//    Call<NearestStops> getNearestStops();

    @GET("json?key=AIzaSyCH9KLEiSz0eVokA6mNqZ7kErmPloUIU9k&radius=500&types=bus_station&sensor=false")
    Call<NearestStops> getNearestStops(@Query("location") String coordinates);

}

