package com.example.maptest;
import com.cloudant.client.api.model.Document;
public class Person extends Document {
    private String Name;
    private Double Latitude;
    private Double Longitude;

    public Person(String myName, Double myLat, Double myLong){

        this.Name = myName;
        this.Latitude = myLat;
        this.Longitude = myLong;
    }
}
