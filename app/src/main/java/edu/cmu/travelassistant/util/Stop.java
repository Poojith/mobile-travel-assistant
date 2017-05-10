package edu.cmu.travelassistant.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cmu.travelassistant.data.Route;

public class Stop {

    private String stpid;
    private String stpnm;
    private String lat;
    private String lon;

    public List<Route> getRoutesAtThisStop() {
        return routesAtThisStop;
    }

    public void setRoutesAtThisStop(List<Route> routesAtThisStop) {
        this.routesAtThisStop = routesAtThisStop;
    }

    private List<Route> routesAtThisStop;

    // Key = Stop ID, Value = Stop
    private static Map<String, Stop> mapOfStops = new HashMap<>();

    public static Map<String, Stop> getMapOfStops() {
        return mapOfStops;
    }

    public static void setMapOfStops(Map<String, Stop> mapOfStops) {
        Stop.mapOfStops = mapOfStops;
    }

    private static Map<Stop, List<Route>> stopToRoutesMap = new HashMap<>();

//    public static Map<Stop, List<Route>> getStopToRoutesMap() {
//        return stopToRoutesMap;
//    }

    public Stop(String stpid, String stpnm, String lat, String lon) {
        this.stpid = stpid;
        this.stpnm = stpnm;
        this.lat = lat;
        this.lon = lon;
    }

    public String getStpid() {
        return stpid;
    }

    public void setStpid(String stpid) {
        this.stpid = stpid;
    }

    public String getStpnm() {
        return stpnm;
    }

    public void setStpnm(String stpnm) {
        this.stpnm = stpnm;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }


}