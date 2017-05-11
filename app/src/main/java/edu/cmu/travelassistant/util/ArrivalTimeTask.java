package edu.cmu.travelassistant.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.travelassistant.data.Route;

/**
 * Created by poojith on 5/10/17.
 */

public class ArrivalTimeTask extends AsyncTask {

    GoogleMap map;
    Context context;
    Stop finalStop;
    Route finalRoute;

    String queryResult;
    String baseUrl = "http://truetime.portauthority.org/bustime/api/v3/getpredictions?key=2dpGCNXPHBD4ZpkJh5wzaghhn&format=json&rtpidatafeed=Port%20Authority%20Bus&dir=INBOUND&rt=";

    Map<Stop, Map<Route, List<TimePrediction>>> stopsRoutesAndPredictionsMap = new HashMap<>();
    Map<Stop, Marker> stopToMarkerMap;

    Marker stopMarker ;

    public ArrivalTimeTask(Context context, GoogleMap map, Stop stop, Route route) {
        this.map = map;
        this.context = context;
        this.finalStop = stop;
        this.finalRoute = route;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] params) {

//        for (Stop stop : stopsList) {
//            try {
//                String stopID = stop.getStpid();

        stopMarker = (Marker) params[0];

        try {
                List<Route> routesAtThisStop = finalStop.getRoutesAtThisStop();

                Map<Route, List<TimePrediction>> routeToPredictionsMap = new HashMap<>();

                if(routesAtThisStop != null) {
                    for(Route route : routesAtThisStop) {
                        String routeNumber = route.getRouteNumber();
                        String routeUrl = baseUrl + routeNumber;
                        String url = routeUrl + "&stpid=" + finalStop.getStpid();
                        queryResult = query(url);
                        List<TimePrediction> predictionList = parse(queryResult);
                        routeToPredictionsMap.put(route, predictionList);
                    }

                    stopsRoutesAndPredictionsMap.put(finalStop, routeToPredictionsMap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        return null;
    }

    private List<TimePrediction> parse(String jsonData) {
        List<TimePrediction> timePredictionsList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonObject = jsonObject.getJSONObject("bustime-response");
            if (jsonObject.has("prd")) {
                JSONArray jsonArray = jsonObject.getJSONArray("prd");
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    String predictedArrivalTime = jsonObject.getString("prdctdn");
                    String predictionType = jsonObject.getString("typ");
                    String distanceFromStop = jsonObject.getString("dstp");
                    String finalDestination = jsonObject.getString("des");
                    String stopID = jsonObject.getString("stpid");
                    String routeNumber = jsonObject.getString("rt");

                    TimePrediction timePrediction = new TimePrediction(predictedArrivalTime, predictionType, distanceFromStop, finalDestination, stopID, routeNumber);
                    timePredictionsList.add(timePrediction);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return timePredictionsList;
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
        List<List<TimePrediction>> allPredictions = new ArrayList<>();

//        for(Stop stop : stopsList) {
            Map<Route, List<TimePrediction>> routesToPredictionsMap = stopsRoutesAndPredictionsMap.get(finalStop);

            for(Route route : routesToPredictionsMap.keySet()) {
                List<TimePrediction> timePredictionsList = routesToPredictionsMap.get(route);
                allPredictions.add(timePredictionsList);
            }
//        }

//        for(Stop stop : stopsList) {
//            int minimumTime = Integer.MAX_VALUE;
            StringBuilder sb = new StringBuilder();

            for(List<TimePrediction> timePredictions : allPredictions) {

                for(TimePrediction timePrediction : timePredictions) {

                    String routeNumber = timePrediction.getRouteNumber();
                    String arrivalTime = timePrediction.getPredictedArrivalTime();
//                    String type = timePrediction.getPredictionType();

                        sb.append("Route number : " + routeNumber + " | Arrival time : " + arrivalTime);
                        sb.append("\n");


                    //TODO sort the arrival time according to route
//                    if(type != null && type.equals("A") && arrivalTime != null) {
//                        int time = Integer.parseInt(arrivalTime);
//                        if(time < minimumTime) {
//                            minimumTime = time;
//                        }
                    }
                }


//            if(sb.length() != 0) {
//                stopMarker.setSnippet(sb.toString());
//            }
//            else {
//                stopMarker.setSnippet("No information for this route at this moment.");
//            }

            if (sb.length() == 0) {
                sb.append("No bus arrivals available at this time");
            }
            Log.e("Prediction" , sb.toString());

//            Marker marker = stopToMarkerMap.get(finalStop);
            stopMarker.setSnippet("#||#" + sb.toString());

            }
        }
//    }

