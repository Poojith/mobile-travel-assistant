package edu.cmu.travelassistant.util;

import java.util.List;

import edu.cmu.travelassistant.data.Route;

/**
 * Created by poojith on 5/6/17.
 */

public interface AsyncResponse {
    void processNearestStops(List<Stop> results);
}
