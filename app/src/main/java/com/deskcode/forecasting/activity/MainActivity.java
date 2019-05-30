package com.deskcode.forecasting.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.deskcode.forecasting.R;
import com.deskcode.forecasting.adapter.RecycleViewWeather;
import com.deskcode.forecasting.constants.Constants;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class MainActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnActivityUpdatedListener, OnGeofencingTransitionListener {

    JSONObject data;
    RecyclerView rvWeatherList;
    RecycleViewWeather adapterViewWeather;
    TextView tvTempreture,tvAreaName;
    String latitude, longitude,curretAddress;
    private LocationGooglePlayServicesProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rvWeatherList = findViewById(R.id.rvWeatherList);
        tvTempreture = findViewById(R.id.tvTempreture);
        tvAreaName = findViewById(R.id.tvAreaName);
        getCurrentLocation();
        getJSON();
        setWeatherAdapter();
//        startLocation();
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.getProviders(true);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            startLocation();
        } else {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.e("Location", String.valueOf(location.getLatitude()));

        }

//       GPSTracker gpsTracker=new GPSTracker(this);
//       if (gpsTracker.getIsGPSTrackingEnabled())
//       {
//           Log.e("Locationss", String.valueOf(gpsTracker.getLatitude()));
//       }

    }


    private void setWeatherAdapter() {
        adapterViewWeather = new RecycleViewWeather(MainActivity.this);
        rvWeatherList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvWeatherList.setAdapter(adapterViewWeather);
    }

    @SuppressLint("StaticFieldLeak")
    public void getJSON() {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.TEST + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID + "&units=" + Constants.TEMP_TYPE);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";
                    while ((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();
                    data = new JSONObject(json.toString());
                    if (data.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }

                } catch (Exception e) {

                    System.out.println("Exception " + e.getMessage());
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void Void) {
                JSONObject mainJsonObject;
                if (data != null) {
                    try {
                        mainJsonObject = new JSONObject(data.get("main").toString());
                        tvTempreture.setText(mainJsonObject.get("temp").toString());
                        Log.d("mainJsonObject", mainJsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.d("my weather received", data.toString());

//                    tvTempreture.setText(data.get("main"));
                }

            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemTempF:

        }
        return super.onOptionsItemSelected(item);

    }

    private void startLocation() {

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(provider).start(MainActivity.this);
        smartLocation.activity().start(this);

        // Create some geofences
        GeofenceModel mestalla = new GeofenceModel.Builder("1").setTransition(Geofence.GEOFENCE_TRANSITION_ENTER).setLatitude(39.47453120000001).setLongitude(-0.358065799999963).setRadius(500).build();
        smartLocation.geofencing().add(mestalla).start(this);
    }

    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
    }

    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

    }

    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location);

    }

    private void showLocation(Location location) {
        if (location != null) {
            final String text = String.format("Latitude %.6f, Longitude %.6f",
                    location.
            (),
                    location.getLongitude());
            Log.e("Locations2", text);

            // We are going to get the address for the current position
            SmartLocation.with(this).geocoding().reverse(location, new OnReverseGeocodingListener() {
                @Override
                public void onAddressResolved(Location original, List<Address> results) {
                    if (results.size() > 0) {
                        Address result = results.get(0);
                        StringBuilder builder = new StringBuilder(text);
                        builder.append("\n[Reverse Geocoding] ");
                        List<String> addressElements = new ArrayList<>();
                        for (int i = 0; i <= result.getMaxAddressLineIndex(); i++) {
                            addressElements.add(result.getAddressLine(i));
                            curretAddress=result.getAdminArea();
                        }
                        builder.append(TextUtils.join(", ", addressElements));
                        tvAreaName.setText(curretAddress);
                        Log.e("Locations3", builder.toString());
                    }
                }
            });
        } else {
            Log.e("Locations4", "Null location");
        }
    }
}
