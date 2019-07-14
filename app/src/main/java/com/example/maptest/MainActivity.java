package com.example.maptest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.cloudant.client.api.CloudantClient;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.Database;
import android.util.Log;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private Database personDatabase;
    private Database hotSpotDatabase;
    public static List<Hotspot> updatedListOfHotspots = null;
    static final String SETTINGS_CLOUDANT_USER = "c7377986-7eac-49f8-b7cb-96c3558dcf38-bluemix";
    static final String SETTINGS_CLOUDANT_PERSON_DB = "person";
    static final String SETTINGS_CLOUDANT_HOTSPOT_DB = "hotspots";
    static final String SETTINGS_CLOUDANT_API_KEY = "jvQ4jwlpmuQo2-NUjisRcbQPMZYB0jBoGxJdG0-7P_Dn";
    static final String SETTINGS_CLOUDANT_API_SECRET = "bdfcc0f2c073edafe9fbf960a62210e14749b77d540319e4a83e80e09c4637e4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoidnZud3UiLCJhIjoiY2p5MjN0NmdyMGl2bjNibHEydW1kM3R4diJ9.TVwh3UbhnFFQAXiH6_-kWg");
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

// Map is set up and the style has loaded. Now you can add data or make other map adjustments

                    }
                });
            }
        });


        /*
         * Initialize IBM Cloudant
         */
        initIBMCloudant();
        //pushMyLocation(new Person("Vivian", 69.69,69.69));
        //pushMyHotspot(generateHotSpots());
        PullHotSpotInfo();

    }

    /* Method to Initialize IBM Cloudant Database */

    public static void updateHotSpots(List<Hotspot> myHotSpots){
        updatedListOfHotspots = myHotSpots;

        for(Hotspot h: updatedListOfHotspots){

            Log.d("DEBUG", h.getGeoJSON());
            //populate into map
        }
    }
    public void initIBMCloudant(){

        CloudantClient client;
        client = ClientBuilder.account(SETTINGS_CLOUDANT_USER).username(SETTINGS_CLOUDANT_API_KEY).password(SETTINGS_CLOUDANT_API_SECRET).build();
        this.personDatabase = client.database(SETTINGS_CLOUDANT_PERSON_DB,false);
        this.hotSpotDatabase = client.database(SETTINGS_CLOUDANT_HOTSPOT_DB,false);

    }
    /* Method to Post to IBM Cloudant using Android AsyncTask */
    public void pushMyLocation(Person myPerson){

        try{
            if(personDatabase != null){
            UploadPersonToCloudant myUploader = new UploadPersonToCloudant(personDatabase);
            myUploader.execute(myPerson);
            }
        }catch(Exception e){
            Log.d("ERROR", e.toString());
        }

    }

    /* Method to Push Latest Hotspot Information from IBM Cloudant */

    public void pushMyHotspot(Hotspot [] myHotspots){

        try{
            if(hotSpotDatabase != null){
                UploadHotSpotToCloudant myUploader = new UploadHotSpotToCloudant(hotSpotDatabase);
                myUploader.execute(myHotspots);
            }
        }catch(Exception e){
            Log.d("ERROR", e.toString());
        }

    }

    public Hotspot[] generateHotSpots(){
        Hotspot [] listOfHotSpots = new Hotspot[8];
        listOfHotSpots[0] = new Hotspot(100,100, "Spaceneedle", true);
        listOfHotSpots[1] = new Hotspot(100,100, "Pike Place Market", true);
        listOfHotSpots[2] = new Hotspot(100,100, "Chihuly Garden and Glass", true);
        listOfHotSpots[3] = new Hotspot(100,100, "Museum of Pop Culture", true);
        listOfHotSpots[4] = new Hotspot(100,100, "Seattle Center", true);
        listOfHotSpots[5] = new Hotspot(100,100, "Seattle Art Museum", true);
        listOfHotSpots[6] = new Hotspot(100,100, "The Museum of Flight", true);
        listOfHotSpots[7] = new Hotspot(100,100, "Woodland Park Zoo", true);
        return listOfHotSpots;
    }
    /* Method to Pull Latest Hotspot Information from IBM Cloudant */


    public void PullHotSpotInfo(){


        try{
            DownloadHotSpots myDownloader = new DownloadHotSpots();
            myDownloader.execute(hotSpotDatabase);

        }
        catch(Exception e){
            Log.d("ERROR", e.toString());
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}