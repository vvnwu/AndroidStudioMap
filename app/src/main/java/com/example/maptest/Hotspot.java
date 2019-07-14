package com.example.maptest;

public class Hotspot {

    private double locLongitude;
    private double locLatitude;
    private String locName;
    private boolean isActive;

    public Hotspot(double myLong, double myLat, String myName, boolean myActive){
        locLongitude = myLong;
        locLatitude = myLat;
        locName = myName;
        isActive = myActive;
    }

    public String getGeoJSON(){

        String firstPart = "{\"type\": \"Feature\", \"geometry\": { \"type\": \"Point\", \"coordinates\": [";
        String middlePart = ", ";
        String lastPart = "]},\"properties\": {\"name\":\"" + locName + "\"}}";
        StringBuilder myStringBuilder = new StringBuilder(firstPart).append(locLatitude).append(middlePart).append(locLongitude).append(lastPart);
        return myStringBuilder.toString();
    }
}
