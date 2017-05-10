package edu.cmu.travelassistant.util;

/**
 * Created by poojith on 5/9/17.
 */

public class BusStopResult {
    private String routeNumber;
    private String routeDirection;
    private String startStopID;
    private String endStopId;

    // TODO Add endStopID to constructor
    public BusStopResult(String routeNumber, String routeDirection, String startStopID) {
        this.routeNumber = routeNumber;
        this.routeDirection = routeDirection;
        this.startStopID = startStopID;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getRouteDirection() {
        return routeDirection;
    }

    public void setRouteDirection(String routeDirection) {
        this.routeDirection = routeDirection;
    }

    public String getStartStopID() {
        return startStopID;
    }

    public void setStartStopID(String startStopID) {
        this.startStopID = startStopID;
    }

    public String getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(String endStopId) {
        this.endStopId = endStopId;
    }
}
