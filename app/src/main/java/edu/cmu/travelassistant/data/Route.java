package edu.cmu.travelassistant.data;

import java.util.HashMap;
import java.util.Map;



public class Route {
    private String routeNumber;
    private String routeName;
    //TODO Map of routeNumber - Stops (change value from String to Stops)
    private Map<String, String> routeMap = new HashMap<>();

    public String getRouteNumber() {
        return routeNumber;
    }

    public String getRouteName() {
        return routeName;
    }

    public Route(String routeNumber, String routeName) {
        this.routeNumber = routeNumber;
        this.routeName = routeName;
        routeMap.put(routeNumber, "");
    }
}

