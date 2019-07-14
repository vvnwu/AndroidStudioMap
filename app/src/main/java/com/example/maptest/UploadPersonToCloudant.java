package com.example.maptest;

import android.os.AsyncTask;

import com.cloudant.client.api.Database;
import android.util.Log;

public class UploadPersonToCloudant extends AsyncTask<Person, Integer, String>{

    private Database db;

    public UploadPersonToCloudant(Database myDB){
        this.db = myDB;
    }

    @Override
    protected String doInBackground(Person ... params) {
        // get the Person from params, which is an array

        for (int i = 0; i <= params.length; i++) {
            // Call this to update your progress
            try{
            db.post(params[i]);}
            catch(Exception e){
                Log.d("ERROR", e.toString());
            }
        }
        return "finished";
    }

    // This is called from background thread but runs in UI
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.d("DEBUG",values.toString()); //UI elements updated while performing doInBackground
    }

    // This runs in UI when background thread finishes
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("DEBUG",result); // UI elements that run when the doInBackground task is completed.
    }

}
