package com.example.maptest;//package com.example.maptest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class Map extends AppCompatActivity {

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

        /*
         * Initialize IBM Cloudant
         */
        initIBMCloudant();
        //pushMyLocation(new Person("Vivian", 69.69,69.69)); //for testing
        //pushMyHotspot(generateHotSpots()); //for testing
        PullHotSpotInfo();

        Mapbox.getInstance(this, "pk.eyJ1IjoidnZud3UiLCJhIjoiY2p5MjN0NmdyMGl2bjNibHEydW1kM3R4diJ9.TVwh3UbhnFFQAXiH6_-kWg");
        setContentView(R.layout.activity_main);
        ImageButton wifiButton = (ImageButton) findViewById(R.id.wifiButton);
        wifiButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                startActivity(new Intent(Map.this, wifi.class));
            }
        });
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style style) {
                        new CountDownTimer(5000, 1000) {
                            public void onFinish() {
                                addClusteredGeoJsonSource(style);
                            }

                            public void onTick(long millisUntilFinished) {
                                // millisUntilFinished    The amount of time until finished.
                            }
                        }.start();
                    }
                });

            }
    });}

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

    public static void updateHotSpots(List<Hotspot> myHotSpots) {
        updatedListOfHotspots = myHotSpots;
        for (Hotspot h : updatedListOfHotspots) {
            final int[][] layers = new int[][]{
                    new int[]{150, Color.parseColor("#E55E5E")}, //light red
                    new int[]{20, Color.parseColor("#F9886C")}, //darker orange
                    new int[]{0, Color.parseColor("#FBB03B")} //orange
            };
            CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
            unclustered.setProperties(
                    circleColor(Color.parseColor("#FBB03B")), //orange
                    circleRadius(20f),
                    circleBlur(1f));
            unclustered.setFilter(Expression.neq(get("cluster"), literal(true)));
            //loadedMapStyle.addLayerBelow(unclustered, "building");
            for (int i = 0; i < layers.length; i++) {
                CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
                circles.setProperties(
                        circleColor(layers[i][1]),
                        circleRadius(70f),
                        circleBlur(1f)
                );
                Expression pointCount = toNumber(get("point_count"));
                circles.setFilter(
                        i == 0
                                ? Expression.gte(pointCount, literal(layers[i][0])) :
                                Expression.all(
                                        Expression.gte(pointCount, literal(layers[i][0])),
                                        Expression.lt(pointCount, literal(layers[i - 1][0]))
                                )
                );
                //loadedMapStyle.addLayerBelow(circles, "building");
            }
        }
    }

    /* Initialize IBM Cloudant DB */
    public void initIBMCloudant() {
        CloudantClient client;
        client = ClientBuilder.account(SETTINGS_CLOUDANT_USER).username(SETTINGS_CLOUDANT_API_KEY).password(SETTINGS_CLOUDANT_API_SECRET).build();
        this.personDatabase = client.database(SETTINGS_CLOUDANT_PERSON_DB, false);
        this.hotSpotDatabase = client.database(SETTINGS_CLOUDANT_HOTSPOT_DB, false);
    }
    /* Method to Post to IBM Cloudant using Android AsyncTask */
    public void pushMyLocation(Person myPerson) {

        try {
            if (personDatabase != null) {
                UploadPersonToCloudant myUploader = new UploadPersonToCloudant(personDatabase);
                myUploader.execute(myPerson);
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
    }

    /* Method that helps us generate a smaple list of hotspots */
    public Hotspot[] generateHotSpots() {
        Hotspot[] listOfHotSpots = new Hotspot[11];
        listOfHotSpots[0] = new Hotspot(47.6205, -122.3493, "Spaceneedle", true);
        listOfHotSpots[1] = new Hotspot(47.6084, -122.3405, "Pike Place Market", true);
        listOfHotSpots[2] = new Hotspot(47.6206, -122.3505, "Chihuly Garden and Glass", true);
        listOfHotSpots[3] = new Hotspot(47.6215, -122.3481, "Museum of Pop Culture", true);
        listOfHotSpots[4] = new Hotspot(47.6219, -122.3517, "Seattle Center", true);
        listOfHotSpots[5] = new Hotspot(47.6073, -122.3381, "Seattle Art Museum", true);
        listOfHotSpots[6] = new Hotspot(47.5180, -122.2964, "The Museum of Flight", true);
        listOfHotSpots[7] = new Hotspot(47.6685, -122.3543, "Woodland Park Zoo", true);
        listOfHotSpots[8] = new Hotspot(47.3623, -122.1953, "Madison Centre", true);
        listOfHotSpots[9] = new Hotspot(47.6019066, -122.3385206, "King Street Station", true);
        listOfHotSpots[10] = new Hotspot(47.5982618, -122.3312084, "Getty Images", true);
        return listOfHotSpots;
    }

    /* Method to Pull Latest Hotspot Information from IBM Cloudant */
    public void PullHotSpotInfo(){
        try {
            DownloadHotSpots myDownloader = new DownloadHotSpots();
            myDownloader.execute(hotSpotDatabase);

        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }

    }

    public static String createGeoJSONList(List<Hotspot> listOfHotspots){

        //Parses the list of updated hotspots into individual strings
        List<String> myStrings = new ArrayList<String>();
        for(Hotspot h: listOfHotspots){
            myStrings.add(h.getGeoJSON());
        }
        //Create a final GeoJSON list of coordinates for app's map.
        if(!myStrings.isEmpty()){
            String firstPart = "{\n" + "\"type\": \"FeatureCollection\",\n"
                    //+ "\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },\n"
                    + "\"features\": [";
            StringBuilder myFinalString = new StringBuilder(firstPart);
            String lastPart = "]\n" + "}";
            for(int i = 0; myStrings.size() > i; i++){

                if(i == myStrings.size() - 1 ){
                    myFinalString.append(",");
                    myFinalString.append(myStrings.get(i));
                    myFinalString.append(lastPart);
                    //the last element
                }
                else if(i == 0){
                    myFinalString.append(myStrings.get(i));
                    //the first element

                }
                else{
                    myFinalString.append(",");
                    myFinalString.append(myStrings.get(i));
                    //every other string in the list
                }
            }
            return(myFinalString.toString());
        }
        else{
            Log.d("DEBUG","didnt work");
            //Do nothing if no hotspots in the list.
            return null;
        }


    }

public static void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle){
        try{
            Log.d("DEBUG",createGeoJSONList(updatedListOfHotspots));
            loadedMapStyle.addSource(
            new GeoJsonSource("earthquakes",
            createGeoJSONList(updatedListOfHotspots),
            new GeoJsonOptions()
            .withCluster(true)
            .withClusterMaxZoom(30) // Max zoom to cluster points on
            .withClusterRadius(50) // Use small cluster radius for the hotspots look
            )
            );
        }
        catch(Exception e){
        Log.d("ERROR",e.toString());

        }
    }
}
