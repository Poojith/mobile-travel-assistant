package edu.cmu.travelassistant;

import android.Manifest;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
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

import java.util.Map;

import edu.cmu.travelassistant.data.Route;
import edu.cmu.travelassistant.util.AsyncResponse;
import edu.cmu.travelassistant.util.BusStopResult;
import edu.cmu.travelassistant.util.RealTimeAPITask;
import edu.cmu.travelassistant.util.Stop;
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

    public static LatLng getUser() {
        return user;
    }

    private static LatLng user;

    private static Route commonRoute;

    private static LatLng userDestination;
    private static LatLng busStartingPoint;
    private static LatLng busEndingPoint;
    private static boolean userLocationFoundFirstTime = false;

    private static String distance = "";
    private static String duration = "";
    private static List<Polyline> polylines = new ArrayList<Polyline>();

    Button getNearbyAttractions;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context context = this;

    private List<Stop> sourceList = new ArrayList<>();

    public static LatLng getUserDestination() {
        return userDestination;
    }

    private static List<Stop> stopsList = new ArrayList<>();
    public static List<Route> routeList = new ArrayList<>();


    private static Map<String, Stop> stopIDToStopMap = Stop.getMapOfStops();


    private static Map<String, Route> routeNameToRouteMap = Route.getRouteNameToRouteMap();

    public static List<Route> getRouteList() {
        return routeList;
    }

    public static List<Stop> getStopsList() {
        return stopsList;
    }


    TravelAPITask travelAPITask;
    RealTimeAPITask realTimeAPITask;

    private int RADIUS = 300;
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

        //TODO populate these lists only the first time the user loads the app. Have a flag to track this.
        stopsList = getAllStops();
        routeList = getAllRoutes();

        Stop.setMapOfStops(stopIDToStopMap);
        Route.setRouteNameToRouteMap(routeNameToRouteMap);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            public static final String TAG = "place fragment";
            public Marker marker;
            FragmentManager fragmentManager = getFragmentManager();

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                // Clear any previos bus stops
                BusRoute.clearBusRoute();
                clearWalkingRoute();

                Log.i(TAG, "Place: " + place.getName());
                if (marker != null) marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).draggable(true));

                marker.setTag("Destination");
                marker.setSnippet(place.getName().toString());
                marker.setTitle(place.getName().toString());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                userDestination = place.getLatLng();
                getNearbyAttractions.performClick();
                showDestinationStops();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Places", "An error occurred: " + status);
            }
        });
    }

    public void showDestinationStops() {
        LatLng userDestination = MapsActivity.getUserDestination();
        double destinationLatitude = userDestination.latitude;
        double destinationLongitude = userDestination.longitude;
        List<Stop> destinationList = new ArrayList<>();
        for(Stop stop : stopsList) {
            double latitude = Double.parseDouble(stop.getLat());
            double longitude = Double.parseDouble(stop.getLon());
            if(Math.abs(latitude - destinationLatitude) <= 0.0025 && Math.abs(longitude - destinationLongitude) <= 0.0025) {
                destinationList.add(stop);
            }
        }

        for (Stop stop : destinationList) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLon()))).title(stop.getStpnm())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            List<Route> routes = stop.getRoutesAtThisStop();
            if (routes != null) {
                List<BusStopResult> results = new ArrayList<>();
                for (Route route : routes) {
                    String routeNumber = route.getRouteNumber();
                    BusStopResult result = new BusStopResult(routeNumber, "INBOUND", stop.getStpid());
                    results.add(result);

                    for(Stop sourceStop : sourceList) {
                        if(sourceStop != null) {
                            List<Route> sourceBuses = sourceStop.getRoutesAtThisStop();
                            if(sourceBuses != null) {
                                for (Route r : sourceBuses) {
                                    if ( r != null && r.getRouteNumber().equals(route.getRouteNumber())) {
                                        commonRoute = route;

                                        busStartingPoint = new LatLng(Double.parseDouble(sourceStop.getLat()), Double.parseDouble(sourceStop.getLon()));
                                        busEndingPoint = new LatLng(Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLon()));
                                        getDirectionsBetweenTwoPoints(user, busStartingPoint);
                                        getDirectionsBetweenTwoPoints(busEndingPoint, userDestination);

                                        BusRoute b = new BusRoute();

                                        Object[] searchData = new Object[5];
                                        Log.e("Common route", route.getRouteNumber());
                                        searchData[0] = mMap;
                                        Log.e("Start stop ID", sourceStop.getStpid());
                                        searchData[1] = route.getRouteNumber();
                                        Log.e("Start stop name", sourceStop.getStpnm());
                                        searchData[2] = "INBOUND";
                                        Log.e("Stop ID", stop.getStpid());
                                        searchData[3] = sourceStop.getStpid();
                                        Log.e("Stop destination name", stop.getStpnm());
                                        searchData[4] = stop.getStpid();
                                        b.execute(searchData);

                                        calculateAndDisplayDistanceAndDurationBetween(user, busStartingPoint);
                                        showNotification("Catch Bus " + searchData[1] + " in 17 minutes", duration + " minute walk to " + sourceStop.getStpnm());
                                        //TODO change icon for the chosen

                                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLon()))).title(stop.getStpnm())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void calculateAndDisplayDistanceAndDurationBetween(LatLng l1, LatLng l2) {
        Location location1 = new Location("");
        location1.setLatitude(l1.latitude);
        location1.setLongitude(l1.longitude);

        Location location2 = new Location("");
        location2.setLatitude(l2.latitude);
        location2.setLongitude(l2.longitude);

        float distanceInMeters = location1.distanceTo(location2);
        int speedIs10MetersPerMinute = 83;
        float estimatedDriveTimeInMinutes = distanceInMeters / speedIs10MetersPerMinute;

        Toast.makeText(this, "Distance:" + (int)distanceInMeters + " Meters\nDuration:" + (int)estimatedDriveTimeInMinutes + "Minutes", Toast.LENGTH_LONG).show();
        duration = Integer.toString((int)estimatedDriveTimeInMinutes);
    }

    public void showNotification(String title, String content) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MapsActivity.class), 0);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("Travel Assistant")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
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

        if (jsonData == null) {
            return routes;
        }

        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONObject routesObject = obj.getJSONObject("bustime-response");
            JSONArray routesArray = routesObject.getJSONArray("routes");

            for (int i = 0; i < routesArray.length(); i++) {
                JSONObject jsonObject = routesArray.getJSONObject(i);

                String routeNumber = jsonObject.getString("rt");
                String routeName = jsonObject.getString("rtnm");
                Route route = new Route(routeNumber, routeName);

                routeNameToRouteMap.put(routeNumber, route);
                routes.add(route);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

    public List<Stop> getAllStops() {
        String jsonData = readJSONFile("stops.json");
        List<Stop> stopMasterData = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONArray stopsArray = obj.getJSONArray("stops");

            for (int i = 0; i < stopsArray.length(); i++) {
                JSONObject jsonObject = stopsArray.getJSONObject(i);
                String stop_id = jsonObject.getString("stop_id");
                String stopName = jsonObject.getString("stop_name");
                String latitude = jsonObject.getString("stop_lat");
                String longitude = jsonObject.getString("stop_lng");

                Stop stop = new Stop(stop_id, stopName, latitude, longitude);

                stopIDToStopMap.put(stop_id, stop);
                stopMasterData.add(stop);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stopMasterData;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                final Context context = getApplicationContext();
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                String title2 = marker.getTitle();
                Log.e("title", title2);
                title.setText(title2);

                String snippet = marker.getSnippet();
                String[] sniInfos = snippet.split(":");
                TextView tv1 = new TextView(context);
                tv1.setText("vicinity: " + sniInfos[0]);
                TextView tv2 = new TextView(context);
                tv2.setText("rating: " + sniInfos[1]);
                TextView tv3 = new TextView(context);
                if (sniInfos[2].equals("true")) {
                    tv3.setText("It is opened now");
                } else {
                    tv3.setText("It is closed now");
                }

                info.addView(title);
                info.addView(tv1);
                info.addView(tv2);
                info.addView(tv3);

                return info;
            }
        });

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
        } else {
            initializeGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        Button placeButton = (Button) findViewById(R.id.btnPlace);
        getNearbyAttractions = placeButton;
        placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getUrl(userDestination.latitude, userDestination.longitude, "restaurant");
                Object[] searchData = new Object[2];
                searchData[0] = mMap;
                searchData[1] = url;
                PlaceRequest request = new PlaceRequest();
                request.execute(searchData);

                Toast.makeText(MapsActivity.this, "Nearby restaurant", Toast.LENGTH_LONG);
            }
        });

        realTimeAPITask = new RealTimeAPITask(context, mMap);
        realTimeAPITask.asyncResponse = this;
        this.displayMyLocation(mMap);

    }

    /**
     * Displays my location & corrects the layout of mylocation button
     *
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
                    user = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    if (!userLocationFoundFirstTime) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 15.0f));
                        realTimeAPITask.execute();
                    }
                    userLocationFoundFirstTime = true;
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

    @Override
    public void processNearestStops(List<Stop> results) {
        sourceList = results;
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

                List l = new ArrayList();
                l.add(new Dot());
                // Adding all the points in the route to LineOptions
                lineOptions.clickable(true);
                lineOptions.pattern(l);
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.DKGRAY);
            }
            // Drawing polyline in the Google Map for the i-th route
            polylines.add(mMap.addPolyline(lineOptions));
        }
    }

    public static void clearWalkingRoute() {
        for(Polyline line : polylines)
        {
            line.remove();
        }
        polylines.clear();
    }
}
