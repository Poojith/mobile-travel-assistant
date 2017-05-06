package edu.cmu.travelassistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<LatLng> markerPoints;
    private static LatLng user;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng pittsburgh = new LatLng(40.4435, -79.9435);
        mMap.addMarker(new MarkerOptions().position(pittsburgh).title("Pittsburgh, PA"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pittsburgh));
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(21.0f);
        this.displayMyLocation(mMap);
    }

    /**
     * Displays my location & corrects the layout of mylocation button
     * @param mMap
     */
    public void displayMyLocation(final GoogleMap mMap) {
        try {
            mMap.setMyLocationEnabled(true);
            View locationButton = ((View) this.findViewById(1).getParent()).findViewById(2);
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub
                    user = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    this.getDirectionsBetweenTwoPoints(user, new LatLng(40.454946,-79.9549824));
                }
                public void getDirectionsBetweenTwoPoints(LatLng point1, LatLng point2) {
                    // Initializing
                    markerPoints = new ArrayList<LatLng>();
                    // Adding new item to the ArrayList
                    markerPoints.add(point1);
                    markerPoints.add(point2);
                    // Creating MarkerOptions

                    // Setting the position of the marker

                    /**
                     * For the start location, the color of marker is GREEN and
                     * for the end location, the color of marker is RED.
                     */
                    MarkerOptions options = new MarkerOptions();
                    options.position(point1);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.addMarker(options);

                    options.position(point2);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    mMap.addMarker(options);

                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

                private String getDirectionsUrl(LatLng origin, LatLng dest) {
                    // Origin of route
                    String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
                    // Destination of route
                    String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
                    // Sensor enabled
                    String sensor = "sensor=false";
                    // Building the parameters to the web service
                    String parameters = str_origin + "&" + str_dest + "&" + sensor;
                    // Output format
                    String output = "json";
                    // Building the url to the web service
                    String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
                    return url;
                }

                /**
                 * A method to download json data from url
                 */
                private String downloadUrl(String strUrl) throws IOException {
                    String data = "";
                    InputStream iStream = null;
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL(strUrl);
                        // Creating an http connection to communicate with url
                        urlConnection = (HttpURLConnection) url.openConnection();
                        // Connecting to url
                        urlConnection.connect();
                        // Reading data from url
                        iStream = urlConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                        StringBuffer sb = new StringBuffer();

                        String line = "";
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        data = sb.toString();
                        br.close();
                    } catch (Exception e) {
                        //Log.d("Exception while downloading url", e.toString());
                    } finally {
                        iStream.close();
                        urlConnection.disconnect();
                    }
                    return data;
                }

                // Fetches data from url passed
                class DownloadTask extends AsyncTask<String, Void, String> {

                    // Downloading data in non-ui thread
                    @Override
                    protected String doInBackground(String... url) {
                        // For storing data from web service
                        String data = "";
                        try {
                            // Fetching the data from web service
                            data = downloadUrl(url[0]);
                        } catch (Exception e) {
                            Log.d("Background Task", e.toString());
                        }
                        return data;
                    }

                    // Executes in UI thread, after the execution of
                    // doInBackground()
                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        ParserTask parserTask = new ParserTask();
                        // Invokes the thread for parsing the JSON data
                        parserTask.execute(result);
                    }
                }

                /**
                 * A class to parse the Google Places in JSON format
                 */
                class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

                    // Parsing the data in non-ui thread
                    @Override
                    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                        JSONObject jObject;
                        List<List<HashMap<String, String>>> routes = null;
                        try {
                            jObject = new JSONObject(jsonData[0]);
                            DirectionsJSONParser parser = new DirectionsJSONParser();

                            // Starts parsing data
                            routes = parser.parse(jObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return routes;
                    }

                    // Executes in UI thread, after the parsing process
                    @Override
                    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                        ArrayList<LatLng> points = null;
                        PolylineOptions lineOptions = null;
                        MarkerOptions markerOptions = new MarkerOptions();
                        String distance = "";
                        String duration = "";

                        if (result.size() < 1) {
                            Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Traversing through all the routes
                        for (int i = 0; i < result.size(); i++) {
                            points = new ArrayList<LatLng>();
                            lineOptions = new PolylineOptions();
                            // Fetching i-th route
                            List<HashMap<String, String>> path = result.get(i);
                            // Fetching all the points in i-th route
                            for (int j = 0; j < path.size(); j++) {
                                HashMap<String, String> point = path.get(j);
                                if (j == 0) {    // Get distance from the list
                                    distance = (String) point.get("distance");
                                    continue;
                                } else if (j == 1) { // Get duration from the list
                                    duration = (String) point.get("duration");
                                    continue;
                                }

                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }

                            // Adding all the points in the route to LineOptions
                            lineOptions.addAll(points);
                            lineOptions.width(10);
                            lineOptions.color(Color.DKGRAY);
                        }
                        //tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);
                        // Drawing polyline in the Google Map for the i-th route
                        mMap.addPolyline(lineOptions);
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
