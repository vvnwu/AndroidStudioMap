package com.example.maptest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.cloudant.client.api.CloudantClient;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.Database;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProviders;
import java.util.List;



    public class MainActivity extends AppCompatActivity {
        private LoginViewModel loginViewModel;
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
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());

                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful

                startActivity(new Intent(MainActivity.this, Map.class));
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                startActivity(new Intent(MainActivity.this, Map.class));
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());

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

    public static void updateHotSpots(List<Hotspot> myHotSpots) {
        updatedListOfHotspots = myHotSpots;

        for (Hotspot h : updatedListOfHotspots) {

            Log.d("DEBUG", h.getGeoJSON());
            //populate into map
        }
    }

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

    /* Method to Push Latest Hotspot Information from IBM Cloudant */

    public void pushMyHotspot(Hotspot[] myHotspots) {

        try {
            if (hotSpotDatabase != null) {
                UploadHotSpotToCloudant myUploader = new UploadHotSpotToCloudant(hotSpotDatabase);
                myUploader.execute(myHotspots);
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }

    }

    public Hotspot[] generateHotSpots() {
        Hotspot[] listOfHotSpots = new Hotspot[8];
        listOfHotSpots[0] = new Hotspot(100, 100, "Spaceneedle", true);
        listOfHotSpots[1] = new Hotspot(100, 100, "Pike Place Market", true);
        listOfHotSpots[2] = new Hotspot(100, 100, "Chihuly Garden and Glass", true);
        listOfHotSpots[3] = new Hotspot(100, 100, "Museum of Pop Culture", true);
        listOfHotSpots[4] = new Hotspot(100, 100, "Seattle Center", true);
        listOfHotSpots[5] = new Hotspot(100, 100, "Seattle Art Museum", true);
        listOfHotSpots[6] = new Hotspot(100, 100, "The Museum of Flight", true);
        listOfHotSpots[7] = new Hotspot(100, 100, "Woodland Park Zoo", true);
        return listOfHotSpots;
    }
    /* Method to Pull Latest Hotspot Information from IBM Cloudant */


    public void PullHotSpotInfo() {


        try {
            DownloadHotSpots myDownloader = new DownloadHotSpots();
            myDownloader.execute(hotSpotDatabase);

        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }

    }


    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = "Welcome!";
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}