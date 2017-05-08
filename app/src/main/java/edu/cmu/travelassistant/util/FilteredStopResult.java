package edu.cmu.travelassistant.util;

import java.util.Comparator;

/**
 * Created by poojith on 5/6/17.
 */

public class FilteredStopResult implements Comparator<FilteredStopResult> {
    private Location location;
    private String stop_name;
    private static double currentLatitude = 40.444521;
    private static double currentLongitude = -79.9486052;

    public static double getCurrentLatitude() {
        return currentLatitude;
    }

    public static void setCurrentLatitude(double latitude) {
        currentLatitude = latitude;
    }

    public static double getCurrentLongitude() {
        return currentLongitude;
    }

    public static void setCurrentLongitude(double longitude) {
        currentLongitude = longitude;
    }

    public FilteredStopResult() {
    }

    public FilteredStopResult(Location location, String stop_name) {
        this.location = location;
        this.stop_name = stop_name;
    }

    public Location getLocation() {
        return location;
    }

    public String getStop_name() {
        return stop_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilteredStopResult that = (FilteredStopResult) o;

        return location != null ? location.equals(that.location) : that.location == null;
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    @Override
    public int compare(FilteredStopResult stop1, FilteredStopResult stop2) {
            if (stop1.equals(stop2)) {
                return 0;
            }

        double lat1 = stop1.getLocation().getLat();
        double lon1 = stop1.getLocation().getLng();
        double lat2 = stop2.getLocation().getLat();
        double lon2 = stop2.getLocation().getLng();

        double distanceToPlace1 = distance(getCurrentLatitude(), getCurrentLongitude(), lat1, lon1);
        double distanceToPlace2 = distance(getCurrentLatitude(), getCurrentLongitude(), lat2, lon2);
        return (int) (distanceToPlace1 - distanceToPlace2);

    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }
}


