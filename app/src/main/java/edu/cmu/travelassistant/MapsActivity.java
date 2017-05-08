package edu.cmu.travelassistant;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
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
import java.util.Collections;

import edu.cmu.travelassistant.data.Route;
import edu.cmu.travelassistant.data.Stop;
import edu.cmu.travelassistant.util.AsyncResponse;
import edu.cmu.travelassistant.util.FilteredStopResult;
import edu.cmu.travelassistant.util.TravelAPITask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import com.google.android.gms.location.LocationListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        AsyncResponse {

    private GoogleMap mMap;
    private boolean mPermissionDenied = false;

    ArrayList<LatLng> markerPoints;
    private static LatLng user;
    private static LatLng userLastLocation;
    private static LatLng userDestination;
    private static LatLng busStartingPoint;
    private static boolean userLocationFoundFirstTime = false;
    private static boolean busStringPointFound = false;
    Button getNearbyAttractions;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context context = this;
    private static List<Stop> stopList = new ArrayList<>();
    private static List<Route> routeList = new ArrayList<>();

    TravelAPITask travelAPITask;

    private int RADIUS = 1000;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        boolean available = isGooglePlayServicesAvailable();
        if (available == false) {
            finish();
        }
        //@end

        stopList = getAllStops();
        routeList = getAllRoutes();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            public static final String TAG = "place fragment";
            public Marker marker;
            FragmentManager fragmentManager = getFragmentManager();
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
                userDestination = place.getLatLng();
                getNearbyAttractions.performClick();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places", "An error occurred: " + status);
            }
        });


        travelAPITask = new TravelAPITask(this);
        travelAPITask.asyncResponse = this;

        // TODO don't populate these lists every time the app is loaded. Have a flag and load it for the first time.
    }

    public String readJSONFile(String filename) {
        String json = null;
        try {
            InputStream stream = this.getAssets().open(filename);
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

    public List<Route> getAllRoutes() {
        String jsonData = readJSONFile("routes.json");
        List<Route> routes = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONObject routesObject = obj.getJSONObject("bustime-response");
            JSONArray routesArray = routesObject.getJSONArray("routes");

            for(int i = 0; i < routesArray.length(); i++) {
                JSONObject jsonObject = routesArray.getJSONObject(i);

                String routeNumber = jsonObject.getString("rt");
                String routeName = jsonObject.getString("rtnm");

                routes.add(new Route(routeNumber, routeName));
            }

            Log.e("ROUTES", String.valueOf(routes.size()));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;

    }

    public List<Stop> getAllStops() {
        String jsonData = readJSONFile("stops.json");
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

            Log.e("STOPS", String.valueOf(stops.size()));

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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                initializeGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            initializeGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        Button placeButton = (Button) findViewById(R.id.btnPlace);
        getNearbyAttractions = placeButton;
        placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getUrl(userDestination.latitude, userDestination.longitude, "gym");
                Object[] searchData = new Object[2];
                searchData[0] = mMap;
                searchData[1] = url;
                PlaceRequest request = new PlaceRequest();
                request.execute(searchData);
                Toast.makeText(MapsActivity.this, "Nearby gym", Toast.LENGTH_LONG);
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pittsburgh, 14.0f));
        this.displayMyLocation(mMap);
    }

    @Override
    public void processNearestStops(List<FilteredStopResult> results) {
            if(results != null) {
                // Plot all nearby stops within a radius of 500m
                Collections.sort(results, new FilteredStopResult());
                for(FilteredStopResult filteredStopResult : results) {
                    double latitude = filteredStopResult.getLocation().getLat();
                    double longitude = filteredStopResult.getLocation().getLng();
                    String stopName = filteredStopResult.getStop_name();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stopName)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                }

                // Plot the nearest stop from the user's current location

                double latitude = results.get(0).getLocation().getLat();
                double longitude = results.get(0).getLocation().getLng();
                String stopName = results.get(0).getStop_name();
                busStartingPoint = new LatLng(latitude, longitude);
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
                    if (!userLocationFoundFirstTime) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 16.0f));
                        FilteredStopResult.setCurrentLatitude(arg0.getLatitude());
                        FilteredStopResult.setCurrentLongitude(arg0.getLongitude());
                        travelAPITask.execute();
                    }
                    userLocationFoundFirstTime = true;
                    if (user != null && busStartingPoint != null && busStringPointFound) {
                        this.getDirectionsBetweenTwoPoints(user, busStartingPoint);
                    }
                    busStringPointFound = true;
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
                    //mMap.addMarker(options);

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




    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int available = googleApi.isGooglePlayServicesAvailable(this);
        if (available != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(available)) {
                googleApi.getErrorDialog(this, available, 0).show();
            }
            return false;
        }
        return true;
    }


    protected synchronized void initializeGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private String getUrl(double latitude, double longitude, String interestingPlace) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        url.append("location=" + latitude + "," + longitude);
        url.append("&radius=" + RADIUS);
        url.append("&type=" + interestingPlace);
        url.append("&sensor=true");

        url.append("&key=" + "AIzaSyDhdw914cX9akpAvX2aYsfcMwDwAQb5SKw");

        return url.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
