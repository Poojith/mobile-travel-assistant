package edu.cmu.travelassistant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

//        this.displayMyLocation(mMap);
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
//    public void displayMyLocation(GoogleMap mMap) {
//        try {
//            mMap.setMyLocationEnabled(true);
//            View locationButton = ((View) this.findViewById(1).getParent()).findViewById(2);
//            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            rlp.setMargins(0, 0, 30, 30);
//
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
}


