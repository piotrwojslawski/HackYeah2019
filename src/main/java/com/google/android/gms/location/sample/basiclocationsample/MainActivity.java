/*
  Copyright 2017 Google Inc. All Rights Reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.google.android.gms.location.sample.basiclocationsample;

import 	java.util.logging.Logger;
import  java.io.ByteArrayOutputStream;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.os.Handler;
import android.widget.Toast;
import android.os.AsyncTask;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import java.io.IOException;

/**
 * Location sample.
 * <p>
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 */


public class MainActivity extends AppCompatActivity {
    public class MyClass {

    }

    public static double distance(String lat1, String lat2, String lon1,
                                  String lon2) {

        final double R = 6371; // Radius of the earth

        double LAT1=Double.parseDouble(lat1);
        double LAT2=Double.parseDouble(lat2);
        double LON1=Double.parseDouble(lon1);
        double LON2=Double.parseDouble(lon2);

        double latDistance = Math.toRadians(LAT2 - LAT1);
        double lonDistance = Math.toRadians(LON2 - LON1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(LAT1)) * Math.cos(Math.toRadians(LAT2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * R * c *c * 1000000; // convert to meters
        return Math.sqrt(distance);
    }

    public String readFullyAsString(InputStream inputStream, String encoding)
            throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public byte[] readFullyAsBytes(InputStream inputStream)
            throws IOException {
        return readFully(inputStream).toByteArray();
    }

    private ByteArrayOutputStream readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    private class MYRequest extends AsyncTask<String, Void, Integer>{

        public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {


            Log.d("LINK", url);

            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();

            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Accept",
                    "application/json");

            httpCon.setRequestProperty("Accept-Language",
                    "en");

            httpCon.setRequestProperty("apikey",
                    "3mK2bklJOrMnUvKtQvOyg6mtsSeOejPW");
            String jsonText = readFullyAsString(httpCon.getInputStream(), "UTF-8");

            Log.e("MyActivityTEXT", jsonText);

            Log.e("MyActivity", new Integer(jsonText.length()).toString());

            JSONObject json = new JSONObject(jsonText);

            return json;
        }

        Integer getAirValue(String Latitude, String Longitude)  {

            JSONObject json;
            try {
                json = readJsonFromUrl("https://airapi.airly.eu/v2/measurements/point?indexType=AIRLY_CAQI&lat=" + Latitude +"&lng=" + Longitude);


                try {

                    Integer x =json.getJSONObject("Value").getInt("value");
                    value += x;

                    ((TextView)findViewById((R.id.value_text))).setText("Air summery value: "+new Float(value).toString());

                    return x;
                }
                catch (Exception e)
                {

                    Integer x = new Integer(53); //averege value if not found starion nearby
                    value += x;

                    ((TextView)findViewById((R.id.value_text))).setText("Air summary value: "+new Float(value).toString());

                    return x;

                }
            }
            catch(Exception e) {
                Log.e("MyActivityException", e.toString());
            }
            return null;
        }




        @Override
        protected Integer doInBackground(String... url) {

            // TODO Auto-generated method stub
            return getAirValue(url[0],url[1]);
        }

    }
    private final int interval = 1000; // 1 Second
    static MainActivity main;

    static String actualLong;
    static String actualLatt;

    static String lastLong = "20.775062";
    static String lastLatt = "52.306306";

    private int number;
    private float value;
    private float dist;

    boolean stateActive;

    private Button awesomeButton;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private TextView mLatitudeText;
    private TextView mLongitudeText;


    private String mDistLabel;
    private String mValueLabel;
    private TextView mValueText;
    private TextView mDistText;


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }



    private Handler handler = new Handler();
    private Runnable runnable = new Runnable(){
        public void run() {
            if (stateActive) {

                lastLatt = actualLatt;
                lastLong = actualLong;

                getLastLocation();


                dist+=distance(lastLatt, actualLatt,lastLong , actualLong);

                Log.d("DistanceLastLatt", lastLatt);
                Log.d("DistancelastLong", lastLong);
                Log.d("DistanceactualLatt", actualLatt);
                Log.d("DistanceactualLong", actualLong);

                Log.d("Distance", new Double(distance(lastLatt, actualLatt,lastLong , actualLong)).toString());

                ((TextView)findViewById((R.id.dist_text))).setText("Distance: "+new Float(dist).toString());

                MYRequest myR = new MYRequest();


                myR.execute(actualLatt,actualLong);

                number++;
            }

            handler.postAtTime(runnable, System.currentTimeMillis()+ interval);
            handler.postDelayed(runnable, interval);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));


        mDistLabel = "Dist:";
        mValueLabel = "Value:";
        mDistText = (TextView) findViewById((R.id.dist_text));
        mValueText = (TextView) findViewById((R.id.value_text));



        stateActive = false;
        number = 0;
        value = 0;
        dist = 0;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        awesomeButton = findViewById(R.id.button);
        awesomeButton.setOnClickListener(new AwesomeButtonClick());
        main = this;

        handler.postAtTime(runnable, System.currentTimeMillis()+ interval);
        handler.postDelayed(runnable, interval);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            actualLatt = String.format(Locale.ENGLISH, "%f",
                                    mLastLocation.getLatitude());

                            actualLong = String.format(Locale.ENGLISH, "%f",
                                    mLastLocation.getLongitude());

                            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    mLatitudeLabel,
                                    mLastLocation.getLatitude()));
                            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
                                    mLongitudeLabel,
                                    mLastLocation.getLongitude()));
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }



    private void awesomeButtonClicked() {

        if (stateActive) {



            ((TextView)findViewById((R.id.dist_text))).setText("Distance: "+new Float(dist).toString());
            ((TextView)findViewById((R.id.value_text))).setText("Average air state: "+new Float(value/number).toString());



            stateActive = false;
            awesomeButton.setText("START!");
        }
        else {


            stateActive = true;
            dist = 0;
            value = 0;
            awesomeButton.setText("STOP!");
        }
    }

    class AwesomeButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            awesomeButtonClicked();
        }
    }
    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}
