package edu.cmu.travelassistant.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.travelassistant.data.Route;
import edu.cmu.travelassistant.data.StopMasterData;
import retrofit2.Response;

public class TravelAPITask extends AsyncTask {
    private Context context;
    private TravelAPI api;
    private String coordinates = null;
    private List<FilteredStopResult> filteredStopResults = new ArrayList<>();

    public AsyncResponse asyncResponse = null;

    public TravelAPITask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        api = AppConstants.initAPI(context);
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        coordinates = Double.toString(FilteredStopResult.getCurrentLatitude()) + ", " + Double.toString(FilteredStopResult.getCurrentLongitude());
        try {
            Response<NearestStops> response = api.getNearestStops(coordinates).execute();
            NearestStops stops = response.body();
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

//
//    @Override
//    protected void onPostExecute(Object o) {
//        try {
//            Thread.sleep(18000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        asyncResponse.processNearestStops(filteredStopResults);
//    }
}
