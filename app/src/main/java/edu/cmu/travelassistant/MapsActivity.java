package edu.cmu.travelassistant;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.travelassistant.data.Stop;
import edu.cmu.travelassistant.util.AsyncResponse;
import edu.cmu.travelassistant.util.FilteredStopResult;
import edu.cmu.travelassistant.util.TravelAPITask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AsyncResponse {

    private GoogleMap mMap;
    private boolean mPermissionDenied = false;

    ArrayList<LatLng> markerPoints;
    private static LatLng user;
    private static LatLng userLastLocation;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context context = this;
    private static List<Stop> stopList = new ArrayList<>();

    TravelAPITask travelAPITask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            public static final String TAG = "place fragment";
            public Marker marker;
            FragmentManager fragmentManager = getFragmentManager();
//            RoutesFragment routesFragment;
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                if (marker!=null) marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).draggable(true));
//                marker.setIcon(BitmapDescriptorFactory.fromAsset("dest_marker.png"));
                marker.setTag("Destination");
                marker.setSnippet(place.getName().toString());
                marker.setTitle(place.getName().toString());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places", "An error occurred: " + status);
            }
        });

        travelAPITask = new TravelAPITask(this);
        travelAPITask.asyncResponse = this;
        travelAPITask.execute();
        stopList = getAllStops();
    }

    public String readJSONFile() {
        String json = null;
        try {
            InputStream stream = this.getAssets().open("stops.json");
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public List<Stop> getAllStops() {
        String jsonData = readJSONFile();
        List<Stop> stops = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONArray stopsArray = obj.getJSONArray("stops");

            for(int i = 0; i < stopsArray.length(); i++) {
                JSONObject jsonObject = stopsArray.getJSONObject(i);

                String stop_id = jsonObject.getString("stop_id");
                String stopName = jsonObject.getString("stop_name");
                String latitude = jsonObject.getString("stop_lat");
                String longitude = jsonObject.getString("stop_lng");

                stops.add(new Stop(stop_id, latitude, longitude, stopName));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return stops;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng pittsburgh = new LatLng(40.4435, -79.9435);
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(21.0f);

//
//                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).
//                        title(stopName).icon
//                        (BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pittsburgh, 14.0f));
        this.displayMyLocation(mMap);
    }

    @Override
    public void processNearestStops(List<FilteredStopResult> results) {
            if(results != null) {
                // Plot all nearby stops within a radius of 500m

//                for(FilteredStopResult filteredStopResult : results) {
//                    double latitude = filteredStopResult.getLocation().getLat();
//                    double longitude = filteredStopResult.getLocation().getLng();
//                    String stopName = filteredStopResult.getStop_name();
//                }

                // Plot the nearest stop from the user's current location

                Collections.sort(results, new FilteredStopResult());
                double latitude = results.get(0).getLocation().getLat();
                double longitude = results.get(0).getLocation().getLng();
                String stopName = results.get(0).getStop_name();
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stopName)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            }
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
                    double dist;
                    if (user != null && userLastLocation != null) {
                        dist = Math.sqrt(Math.pow(user.latitude - userLastLocation.latitude, 2) + Math.pow(user.longitude - userLastLocation.longitude, 2));
                    } else {
                        dist = 0;
                    }
                    if (dist < 100) {
                        this.getDirectionsBetweenTwoPoints(user, new LatLng(40.454946,-79.9549824));
                    }

                }
                public void getDirectionsBetweenTwoPoints(LatLng point1, LatLng point2) {
                    userLastLocation = point1;
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
                    //mMap.addMarker(options);

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
