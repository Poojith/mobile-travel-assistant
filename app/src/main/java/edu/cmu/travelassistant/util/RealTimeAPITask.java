package edu.cmu.travelassistant.util;

/**
 * Created by poojith on 5/8/17.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.cmu.travelassistant.MapsActivity;
import edu.cmu.travelassistant.R;
import edu.cmu.travelassistant.data.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;


public class RealTimeAPITask extends AsyncTask {
    String queryResult;
    String baseUrl = "http://truetime.portauthority.org/bustime/api/v3/getstops?key=QzLiAG6tuHzPqqii3ETFuwTsZ&format=json&rtpidatafeed=Port%20Authority%20Bus&dir=INBOUND&rt=";

    GoogleMap mMap;
    private Context context;

    //    Map<Stop, List<Route>> stopToRoutesMap = Stop.getStopToRoutesMap();
    Map<String, Stop> mapOfStops = Stop.getMapOfStops();
    Map<Route, List<Stop>> routesToStopMap = Route.getRouteToStopsMap();

    public AsyncResponse asyncResponse = null;

    public RealTimeAPITask(Context context, GoogleMap map) {
        this.context = context;
        this.mMap = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            List<Route> routes = MapsActivity.getRouteList();
            for (Route route : routes) {
                String routeNumber = route.getRouteNumber();
                String url = baseUrl + routeNumber;
                queryResult = query(url);
                List<Stop> stopList = parse(queryResult, route);
                routesToStopMap.put(route, stopList);
                Route.setRouteToStopsMap(routesToStopMap);
                Stop.setMapOfStops(mapOfStops);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<Stop> parse(String jsonData, Route route) {
        List<Stop> stops = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonObject = jsonObject.getJSONObject("bustime-response");
            JSONArray jsonArray = jsonObject.getJSONArray("stops");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String stopID = jsonObject.getString("stpid");
                Stop stop = mapOfStops.get(stopID);
                List<Route> routes = stop.getRoutesAtThisStop();

                if (routes == null) {
                    routes = new ArrayList<>();
                }

                routes.add(route);
                stop.setRoutesAtThisStop(routes);
                mapOfStops.put(stopID, stop);
//                stopToRoutesMap.put(stop, routes);
                stops.add(stop);
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            ;
        }

        return stops;
    }

    public String query(String queryURL) throws IOException {
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
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            inputStream.close();
        }
        return data;
    }

    @Override
    protected void onPostExecute(Object o) {
        double currentLatitude = MapsActivity.getUser().latitude;
        double currentLongitude = MapsActivity.getUser().longitude;

        List<Stop> stopsList = MapsActivity.getStopsList();
        List<Stop> sourceList = new ArrayList<>();

        for (Stop stop : stopsList) {
            double latitude = Double.parseDouble(stop.getLat());
            double longitude = Double.parseDouble(stop.getLon());

            if (Math.abs(latitude - currentLatitude) <= 0.0025 && Math.abs(longitude - currentLongitude) <= 0.0025) {
                sourceList.add(stop);
            }
        }

        for (Stop stop : sourceList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLon()))).title(stop.getStpnm())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            List<Route> routes = stop.getRoutesAtThisStop();
            if (routes != null) {
                List<BusStopResult> results = new ArrayList<>();
                for (Route route : routes) {
                    String routeNumber = route.getRouteNumber();
                    BusStopResult result = new BusStopResult(routeNumber, "INBOUND", stop.getStpid());
                    results.add(result);
                }
            }
        }
        asyncResponse.processNearestStops(sourceList);
    }
}