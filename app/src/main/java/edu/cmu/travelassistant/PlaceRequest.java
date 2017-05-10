package edu.cmu.travelassistant;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.CameraUpdateFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Created by zack on 5/7/17.
 */

public class PlaceRequest extends AsyncTask<Object, String, String> {
    String queryResult;
    GoogleMap mMap;
    String url;


    @Override
    protected String doInBackground(Object... params) {
        mMap = (GoogleMap) params[0];
        url = (String) params[1];
        try {
            queryResult = query(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResult;
    }

    @Override
    protected void onPostExecute(String result) {
        List<HashMap<String, String>> placeList = parse(result);
        showPlaces(placeList);
    }


//    private void showPlaces(List<HashMap<String, String>> placeList) {
//        for (int i = 0; i < placeList.size(); i++) {
//            MarkerOptions options = new MarkerOptions();
//            HashMap<String, String> place = placeList.get(i);
//            double latitude = Double.parseDouble(place.get("latitude"));
//            double longitude = Double.parseDouble(place.get("longitude"));
//            String placeName = place.get("placeName");
//            String vicinity = place.get("vicinity");
//            LatLng position = new LatLng(latitude, longitude);
//            options.position(position);
//            options.title(placeName + "&" + vicinity);
//            mMap.addMarker(options);
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
//        }
//    }

    private HashMap<String, String> getPlace(JSONObject jsonPlace) {
        HashMap<String, String> placeMap = new HashMap<>();
        String nameOfPlace = "NULL";
        String vicinity = "NULL";
        String latitude = "";
        String longitude = "";
        String rating = "";
        String opening_hours = "";

        try {
            if (!jsonPlace.isNull("name")) {
                nameOfPlace = jsonPlace.getString("name");
            }
            if (!jsonPlace.isNull("vicinity")) {
                vicinity = jsonPlace.getString("vicinity");
            }
            latitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            Log.d("latitude", latitude);
            longitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            Log.d("longitude", longitude);
            rating = jsonPlace.getString("rating");
            Log.d("rating", rating);
            opening_hours = jsonPlace.getJSONObject("opening_hours").getString("open_now");
            Log.d("opening", opening_hours);

            placeMap.put("placeName", nameOfPlace);
            placeMap.put("vicinity", vicinity);
            placeMap.put("latitude", latitude);
            placeMap.put("longitude", longitude);
            placeMap.put("rating", rating);
            placeMap.put("opening_hours", opening_hours);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeMap;
    }

    private void showPlaces(List<HashMap<String, String>> placeList) {
        for (int i = 0; i < placeList.size(); i++) {
            MarkerOptions options = new MarkerOptions();
            HashMap<String, String> place = placeList.get(i);

            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(place.get("latitude"));
                longitude = Double.parseDouble(place.get("longitude"));
                String placeName = place.get("placeName");
                String vicinity = place.get("vicinity");
                String rating = place.get("rating");
                String opening_now = place.get("opening_hours");

                LatLng position = new LatLng(latitude, longitude);
                options.position(position);
                options.title(placeName);
//                options.snippet(vicinity + ":" + rating + ":" + opening_now);
                options.snippet(vicinity);

                mMap.addMarker(options);

                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            } catch(Exception e) {
                continue;
            }

        }
    }


    public String query(String queryURL) throws IOException {
        Log.d("query URL", queryURL);
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(queryURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("result", data);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            inputStream.close();
        }
        return data;
    }

    private List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject((String) jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int numberOfPlaces = jsonArray.length();
        List<HashMap<String, String>> listOfPlaces = new ArrayList<>();
        HashMap<String, String> placeMap = null;
        for (int i = 0; i < numberOfPlaces; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listOfPlaces.add(placeMap);
        }
        return listOfPlaces;
    }

//    private HashMap<String, String> getPlace(JSONObject jsonPlace) {
//        HashMap<String, String> placeMap = new HashMap<>();
//        String nameOfPlace = "NULL";
//        String vicinity = "NULL";
//        String latitude = "";
//        String longitude = "";
//        String reference = "";
//
//        try {
//            if (!jsonPlace.isNull("name")) {
//                nameOfPlace = jsonPlace.getString("name");
//            }
//            if (!jsonPlace.isNull("vicinity")) {
//                vicinity = jsonPlace.getString("vicinity");
//            }
//            latitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
//            longitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
//            reference = jsonPlace.getString("reference");
//            placeMap.put("placeName", nameOfPlace);
//            placeMap.put("vicinity", vicinity);
//            placeMap.put("latitude", latitude);
//            placeMap.put("longitude", longitude);
//            placeMap.put("reference", reference);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return placeMap;
//    }
}






















