package edu.cmu.travelassistant.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.travelassistant.util.Stop;


public class Route {
    private String routeNumber;
    private String routeName;

    //TODO Map of routeNumber - Stops (change value from String to Stops)
    private static Map<Route, List<Stop>> routeToStopsMap = new HashMap<>();

    public static void setRouteToStopsMap(Map<Route, List<Stop>> routeToStopsMap) {
        Route.routeToStopsMap = routeToStopsMap;
    }

    public static void setRouteNameToRouteMap(Map<String, Route> routeNameToRouteMap) {
        Route.routeNameToRouteMap = routeNameToRouteMap;
    }

    // Key = routeNumber
    private static Map<String, Route> routeNameToRouteMap = new HashMap<>();

    public String getRouteNumber() {
        return routeNumber;
    }

    public String getRouteName() {
        return routeName;
    }

    public static Map<Route, List<Stop>> getRouteToStopsMap() {
        return routeToStopsMap;
    }

    public Route(String routeNumber, String routeName) {
        this.routeNumber = routeNumber;
        this.routeName = routeName;
    }

    public static Map<String, Route> getRouteNameToRouteMap() {
        return routeNameToRouteMap;
    }

 }

