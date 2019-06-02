package com.deskcode.forecasting.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.deskcode.forecasting.R;
import com.deskcode.forecasting.adapter.RecycleViewWeather;
import com.deskcode.forecasting.constants.Constants;
import com.google.android.gms.location.DetectedActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class MainActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnActivityUpdatedListener, OnGeofencingTransitionListener, PopupMenu.OnMenuItemClickListener {

    JSONObject UVIndexObject, currentWeatherObject, todayWeatherObject;
    RecyclerView rvWeatherList;
    RecycleViewWeather adapterViewWeather;
    TextView tvTempreture, tvAreaName, tvUVIndex, tvDesc;
    ImageButton btnSettings;
    String latitude, longitude, currentAddress, iconName;
    ImageView ivWeather;
    ArrayList<String> todayrvDataList, todayTimeList;
    ProgressDialog progressDialog;
    int counter = 3;
    private LocationGooglePlayServicesProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JodaTimeAndroid.init(this);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("loading....");
        todayrvDataList = new ArrayList<>();
        todayTimeList = new ArrayList<>();
        rvWeatherList = findViewById(R.id.rvWeatherList);
        tvTempreture = findViewById(R.id.tvTempreture);
        tvDesc = findViewById(R.id.tvDesc);
        tvAreaName = findViewById(R.id.tvAreaName);
        tvUVIndex = findViewById(R.id.tvUVIndex);
        ivWeather = findViewById(R.id.ivWeather);
        btnSettings = findViewById(R.id.btnSettings);

        getPermission();
        setWeatherAdapter();

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_main, popup.getMenu());
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.show();
            }
        });
    }


    private void setWeatherAdapter() {
        adapterViewWeather = new RecycleViewWeather(MainActivity.this, todayrvDataList, todayTimeList);
        rvWeatherList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvWeatherList.setAdapter(adapterViewWeather);
    }

    /*----------------------get current weather-----------------------*/
    @SuppressLint("StaticFieldLeak")
    public void getCurrentWeather(String temperature, String temperatureType) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.CURRENT_WEATHER + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID + "&units=" + temperature);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    Log.e("Connection", connection.toString());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";
                    while ((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();
                    currentWeatherObject = new JSONObject(json.toString());
                    if (currentWeatherObject.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }

                } catch (Exception e) {
                    System.out.println("Exception " + e.getMessage());
                    return null;
                }
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(Void Void) {
                try {
                    counter--;
                    if (counter == 0) {
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONObject mainJsonObject;
                if (currentWeatherObject != null) {
                    try {
                        mainJsonObject = new JSONObject(currentWeatherObject.get("main").toString());
                        String tempString = mainJsonObject.get("temp").toString() + temperatureType;
                        JSONArray jsonArray = new JSONArray(currentWeatherObject.get("weather").toString());
                        String weatherDesc = jsonArray.getJSONObject(0).getString("description");
                        tvTempreture.setText(tempString);
                        tvDesc.setText(weatherDesc);
                        String weatherData = currentWeatherObject.get("weather").toString();
                        JSONArray jsonarrayWeather = new JSONArray(weatherData);
                        for (int i = 0; i < jsonarrayWeather.length(); i++) {
                            JSONObject jsonObj = jsonarrayWeather.getJSONObject(i);
                            iconName = jsonObj.getString("icon");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Glide.with(MainActivity.this).load(Constants.IMAGE_URL + iconName + Constants.IMAGE_TYPE).into(ivWeather);
                }
            }
        }.execute();

    }

    /*-----get UV Index--------------*/
    @SuppressLint("StaticFieldLeak")
    private void getUVIndex() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(Constants.UVINDEX_URL + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    Log.e("Connection", connection.toString());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";
                    while ((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();
                    UVIndexObject = new JSONObject(json.toString());
                } catch (Exception e) {
                    System.out.println("Exception " + e.getMessage());
                    return null;
                }
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    counter--;
                    if (counter == 0) {
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (UVIndexObject != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(UVIndexObject.toString());
                        String UVIndex = jsonObject.get("value").toString();
                        float UVIndexFlot = Float.parseFloat(UVIndex);
                        if (UVIndexFlot <= 2.9) {
                            tvUVIndex.setText(getResources().getString(R.string.low));
                        } else if (UVIndexFlot == 3.0 && UVIndexFlot <= 5.9) {
                            tvUVIndex.setText(getResources().getString(R.string.moderate));
                        } else if (UVIndexFlot == 6.0 && UVIndexFlot <= 7.9) {
                            tvUVIndex.setText(getResources().getString(R.string.high));
                        } else if (UVIndexFlot == 8.0 && UVIndexFlot <= 10.9) {
                            tvUVIndex.setText(getResources().getString(R.string.very_high));
                        } else if (UVIndexFlot >= 11) {
                            tvUVIndex.setText(getResources().getString(R.string.extreme));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    /*------today weather List-----*/
    @SuppressLint("StaticFieldLeak")
    private void todayWeatherList(String temperature) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.TODAY_WEATHER + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID + "&units=" + temperature);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    Log.e("Connection", connection.toString());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";
                    while ((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();
                    todayWeatherObject = new JSONObject(json.toString());
                    if (todayWeatherObject.getInt("cod") != 200) {
                        System.out.println("Cancelled");
                        return null;
                    }

                } catch (Exception e) {
                    System.out.println("Exception " + e.getMessage());
                    return null;
                }
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(Void Void) {
                try {
                    counter--;
                    if (counter == 0) {
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Date currentLocalDate = Calendar.getInstance().getTime();

                //Check current time is more than 11PM or not
                SimpleDateFormat localDateFormat = new SimpleDateFormat("HH");
                int time = Integer.parseInt(localDateFormat.format(currentLocalDate));
                if (time >= 21) {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    currentLocalDate = c.getTime();
                }

                SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String CurrentDate = currentDateFormat.format(currentLocalDate);
                if (todayWeatherObject != null) {
                    try {
                        todayrvDataList.clear();
                        todayTimeList.clear();
                        JSONArray jsonArrayDataList = new JSONArray(todayWeatherObject.get("list").toString());
                        for (int i = 0; i < jsonArrayDataList.length(); i++) {
                            JSONObject jsonObjDataList = jsonArrayDataList.getJSONObject(i);
                            String dateDataString = jsonObjDataList.getString("dt");
                            long dateLong = Long.parseLong(dateDataString);
                            DateTime dateTime = new DateTime(dateLong * 1000L, DateTimeZone.getDefault());
                            String[] splitDate = dateTime.toString().split("T");
                            String splitCurrentDateList = splitDate[0];
                            String splitTime = splitDate[1].substring(0, 5);
                            if (CurrentDate.equals(splitCurrentDateList)) {
                                todayrvDataList.add(jsonObjDataList.get("weather").toString());
                                todayTimeList.add(splitTime);
                            }
                        }
                        adapterViewWeather.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    /*----------------------settings menu for change temp--------------------------*/

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemTempF:
                counter = 1;
                getCurrentWeather(Constants.TEMP_TYPE_F, "°F");
                break;
            case R.id.itemTempC:
                counter = 1;
                getCurrentWeather(Constants.TEMP_TYPE_C, "°C");
                break;
        }
        return true;
    }

    /*-----------------------------get current location----------------------*/

    private void requestLocation() {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.location(provider).start(MainActivity.this);
        smartLocation.activity().start(this);
    }

    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
    }

    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

    }

    @Override
    public void onLocationUpdated(Location location) {
        getData(location);

    }

    private void getData(Location location) {
        if (location != null) {
            final String text = String.format("Latitude %.6f, Longitude %.6f", location.getLatitude(), location.getLongitude());
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            getCurrentWeather(Constants.TEMP_TYPE_C, "°C");
            todayWeatherList(Constants.TEMP_TYPE_C);
            getUVIndex();
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
                            currentAddress = result.getLocality() + " " + result.getCountryName();
                        }
                        builder.append(TextUtils.join(", ", addressElements));
                        tvAreaName.setText(currentAddress);
                    }
                }
            });
        } else {
            Log.e("Locations", "Null location");
        }
    }

    /*---Location Permission----*/
    private void getPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            progressDialog.show();
                            requestLocation();
                        } else {
                            getPermission();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(MainActivity.this, "some error", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }
}
