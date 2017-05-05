package edu.cmu.travelassistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng pittsburgh = new LatLng(40.4435, -79.9435);
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(21.0f);

        String jsonData = readJSONFile();
        try {
            JSONObject obj = new JSONObject(jsonData);
            JSONArray stopsArray = obj.getJSONArray("stops");

            for(int i = 0; i < 50; i++) {
                JSONObject jsonObject = stopsArray.getJSONObject(i);

                String stopName = jsonObject.getString("stop_name");
                Log.i("Stop name : ", stopName);

                float latitude = Float.parseFloat(jsonObject.getString("stop_lat"));
                float longitude = Float.parseFloat(jsonObject.getString("stop_lng"));

                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).
                        title(stopName).icon
                        (BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pittsburgh, 14.0f));

        this.displayMyLocation(mMap);
    }

    /**
     * Displays my location & corrects the layout of mylocation button
     * @param mMap
     */
    public void displayMyLocation(GoogleMap mMap) {
        try {
            mMap.setMyLocationEnabled(true);
            View locationButton = ((View) this.findViewById(1).getParent()).findViewById(2);
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
