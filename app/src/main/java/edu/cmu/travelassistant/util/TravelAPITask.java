package edu.cmu.travelassistant.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class TravelAPITask extends AsyncTask {
    private Context context;
    private TravelAPI api;
    private String coordinates = null;
    private List<FilteredStopResult> filteredStopResults = new ArrayList<>();
    public AsyncResponse asyncResponse = null;

    public TravelAPITask(Context context) {
        Log.e("API Test", "Inside API task constructor");
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        api = AppConstants.initAPI(context);
        Log.e("API Test", "Inside PreExecute");
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        coordinates = Double.toString(FilteredStopResult.getCurrentLatitude()) + ", " + Double.toString(FilteredStopResult.getCurrentLongitude());

        try {
            Response<NearestStops> response = api.getNearestStops(coordinates).execute();
            NearestStops stops = response.body();
            Log.e("Number of stops: ", String.valueOf(stops.getResults().size()));
            Log.e("Stop name : " , stops.getResults().get(0).getName());

            List<Result> results = stops.getResults();

            for(Result result : results) {
                Location location = result.getGeometry().getLocation();
                String stop_name = result.getName();
                filteredStopResults.add(new FilteredStopResult(location, stop_name));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        asyncResponse.processNearestStops(filteredStopResults);
    }
}
