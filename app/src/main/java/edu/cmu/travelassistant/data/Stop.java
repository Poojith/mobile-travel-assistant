package edu.cmu.travelassistant.data;

/**
 * Created by poojith on 5/6/17.
 */

public class Stop {

    private String stop_id;
    private String stop_lat;
    private String stop_lng;
    private String stop_name;

    public Stop(String stop_id, String stop_lat, String stop_lng, String stop_name) {
        this.stop_id = stop_id;
        this.stop_lat = stop_lat;
        this.stop_lng = stop_lng;
        this.stop_name = stop_name;
    }

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_lat() {
        return stop_lat;
    }

    public void setStop_lat(String stop_lat) {
        this.stop_lat = stop_lat;
    }

    public String getStop_lng() {
        return stop_lng;
    }

    public void setStop_lng(String stop_lng) {
        this.stop_lng = stop_lng;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }
}
