package com.example.maptest;

import android.os.AsyncTask;
import com.cloudant.client.api.Database;
import android.util.Log;
import java.util.List;

public class DownloadHotSpots extends AsyncTask<Database, Void, List<Hotspot>>{

    private List<Hotspot> hotspots;
    public DownloadHotSpots(){
        super();
    }
    @Override
    protected List<Hotspot> doInBackground(Database... db) {
            try{
                List<Hotspot> myHotspots = db[0].getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(Hotspot.class);
                return myHotspots;
               }
            catch(Exception e){
                Log.d("ERROR", e.toString());
                return null;
            }

    }


    @Override
    protected void onPostExecute(List<Hotspot> result) {
        //do stuff
        MainActivity.updateHotSpots(result);

    }

}



