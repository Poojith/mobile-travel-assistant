package edu.cmu.travelassistant;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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

    }
}
