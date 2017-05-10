package edu.cmu.travelassistant.util;

/**
 * Created by poojith on 5/10/17.
 */

public class TimePrediction {
    private String predictedArrivalTime;
    private String predictionType;
    private String distanceFromStop;
    private String finalDestination;
    private String stopID;
    private String routeNumber;

    public TimePrediction(String predictedArrivalTime, String predictionType, String distanceFromStop, String finalDestination, String stopID, String routeNumber) {
        this.predictedArrivalTime = predictedArrivalTime;
        this.predictionType = predictionType;
        this.distanceFromStop = distanceFromStop;
        this.finalDestination = finalDestination;
        this.stopID = stopID;
        this.routeNumber = routeNumber;

    }

    public String getPredictedArrivalTime() {
        return predictedArrivalTime;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public String getDistanceFromStop() {
        return distanceFromStop;
    }

    public String getFinalDestination() {
        return finalDestination;
    }


    public String getStopID() {
        return stopID;
    }

    public String getRouteNumber() {
        return routeNumber;
    }
}
