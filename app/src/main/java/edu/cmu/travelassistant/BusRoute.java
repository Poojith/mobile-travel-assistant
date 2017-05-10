package edu.cmu.travelassistant;

/**
 * Created by TortugaDeVaio on 08-May-17.
 */

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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


public class BusRoute extends AsyncTask<Object, String, String> {
    String queryResult;
    GoogleMap mMap;
    String preUrl = "http://truetime.portauthority.org/bustime/api/v3/getpatterns?key=Gg5eAVrmgNc3U5kC5PcFfcQGz&rtpidatafeed=Port%20Authority%20Bus&format=json&rt=";
    String url;
    String routeDirection;
    String startStop;
    int stopFound = 0;
    String endStop;
    private static List<Polyline> polylines = new ArrayList<Polyline>();

    @Override
    protected String doInBackground(Object... params) {
        try {
            mMap = (GoogleMap) params[0];
            url = preUrl + (String) params[1];
            routeDirection = (String) params[2];
            startStop = (String) params[3];
            endStop = (String) params[4];
            queryResult = query(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryResult;
    }

    @Override
    protected void onPostExecute(String result) {
        List<String> points = parse(result);
        showPoints(points);
    }


    private void showPoints(List<String> points) {

        for (int i = 1; i < points.size(); i++) {
            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(points.get(i - 1));
                LatLng pt1 = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
                jsonObject = new JSONObject(points.get(i));
                LatLng pt2 = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
                polylines.add(mMap.addPolyline(new PolylineOptions()
                        .add(pt1, pt2)
                        .width(10)
                        .color(Color.RED)
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public static void clearBusRoute() {
        for(Polyline line : polylines)
        {
            line.remove();
        }
        polylines.clear();
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

    private List<String> parse(String jsonData) {
        List<String> points = new ArrayList<>();
        try {
            JSONArray jsonArray = null;
            JSONObject jsonObject;
            jsonObject = new JSONObject((String) jsonData);
            jsonObject = jsonObject.getJSONObject("bustime-response");
            jsonArray = jsonObject.getJSONArray("ptr");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("rtdir").equals(routeDirection)) {
                    break;
                }
            }
            jsonArray = jsonObject.getJSONArray("pt");
            int numberOfPlaces = jsonArray.length();
            int i = 0;
            for (; i < numberOfPlaces; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("stpid") && jsonObject.getString("stpid").equals(startStop)) {
                    stopFound = 1;
                    break;
                }
                if (jsonObject.has("stpid") && jsonObject.getString("stpid").equals(endStop)) {
                    stopFound = 2;
                    break;
                }
            }
            for (; i < numberOfPlaces; i++) {
                points.add(jsonArray.getJSONObject(i).toString());
                jsonObject = jsonArray.getJSONObject(i);
                if (stopFound == 1 && jsonObject.has("stpid")&& jsonObject.getString("stpid").equals(endStop)) {
                    break;
                }
                if (stopFound == 2 && jsonObject.has("stpid")&& jsonObject.getString("stpid").equals(startStop)) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return points;
    }
}