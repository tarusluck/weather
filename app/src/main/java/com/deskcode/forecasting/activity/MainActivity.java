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
import android.view.MenuItem;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity implements OnLocationUpdatedListener, OnActivityUpdatedListener, OnGeofencingTransitionListener {

    JSONObject data, UVIndexObject;
    RecyclerView rvWeatherList;
    RecycleViewWeather adapterViewWeather;
    TextView tvTempreture, tvAreaName, tvUVIndex;
    String latitude, longitude, curretAddress, iconName;
    ImageView ivWeather;
    ArrayList<String> todayDataList, todayTimeList;
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
        todayDataList = new ArrayList<>();
        todayTimeList = new ArrayList<>();
        rvWeatherList = findViewById(R.id.rvWeatherList);
        tvTempreture = findViewById(R.id.tvTempreture);
        tvAreaName = findViewById(R.id.tvAreaName);
        tvUVIndex = findViewById(R.id.tvUVIndex);
        ivWeather = findViewById(R.id.ivWeather);
        getPermission();
        setWeatherAdapter();
    }


    private void setWeatherAdapter() {
        adapterViewWeather = new RecycleViewWeather(MainActivity.this, todayDataList, todayTimeList);
        rvWeatherList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvWeatherList.setAdapter(adapterViewWeather);
    }

    /*----------------------get current weather-----------------------*/
    @SuppressLint("StaticFieldLeak")
    public void getCurrentWeather(String tempreture, String tempType) {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();


            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.CURRENT_WEATHER + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID + "&units=" + tempreture);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    Log.e("Connection", connection.toString());

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
                if (data != null) {
                    try {
                        mainJsonObject = new JSONObject(data.get("main").toString());
                        String tempString = mainJsonObject.get("temp").toString() + tempType;
                        tvTempreture.setText(tempString);
                        String weatherData = data.get("weather").toString();
                        JSONArray jsonarrayWeather = new JSONArray(weatherData);
                        for (int i = 0; i < jsonarrayWeather.length(); i++) {
                            JSONObject jsonObj = jsonarrayWeather.getJSONObject(i);
                            iconName = jsonObj.getString("icon");
                        }
                        Log.d("mainJsonObject", mainJsonObject.toString());
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
                        Log.e("UVIndex", UVIndex);
                        float UVIndexFlot = Float.parseFloat(UVIndex);
                        if (UVIndexFlot <= 2.9) {
                            tvUVIndex.setText("UVIndex :Low");
                        } else if (UVIndexFlot == 3.0 && UVIndexFlot <= 5.9) {
                            tvUVIndex.setText("UVIndex :Moderate");
                        } else if (UVIndexFlot == 6.0 && UVIndexFlot <= 7.9) {
                            tvUVIndex.setText("UVIndex : High");
                        } else if (UVIndexFlot == 8.0 && UVIndexFlot <= 10.9) {
                            tvUVIndex.setText("UVIndex : Very High");
                        } else if (UVIndexFlot >= 11) {
                            tvUVIndex.setText("UVIndex : Extreme");
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
    private void todayWeatherList(String tempType) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    URL url = new URL(Constants.TODAY_WEATHER + "lat=" + latitude + "&lon=" + longitude + "&appid=" + Constants.APP_ID + "&units=" + tempType);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    Log.e("Connection", connection.toString());
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
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String CurrentDate = df.format(c);
                if (data != null) {
                    try {
                        todayDataList.clear();
                        todayTimeList.clear();
                        JSONArray jsonarrayListData = new JSONArray(data.get("list").toString());
                        for (int i = 0; i < jsonarrayListData.length(); i++) {
                            JSONObject jsonObj = jsonarrayListData.getJSONObject(i);
                            String dateData = jsonObj.getString("dt");
                            long dateLong = Long.parseLong(dateData);
                            DateTime dateTime = new DateTime(dateLong * 1000L, DateTimeZone.getDefault());
                            Log.e("dateTime", dateTime.toString());
                            String Splitdate[] = dateTime.toString().split("T");
                            String splitCurrentDateList = Splitdate[0];
                            String splitTime = Splitdate[1].substring(0, 5);
                            if (CurrentDate.equals(splitCurrentDateList)) {
                                todayDataList.add(jsonObj.get("weather").toString());
                                todayTimeList.add(splitTime);
                            }
                        }
                        Log.e("ListSize", String.valueOf(todayDataList.size()));
                        setWeatherAdapter();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute();
    }

    /*----------------------option menu for change temp--------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        return super.onOptionsItemSelected(item);

    }

    /*-----------------------------get current location----------------------*/

    private void startLocation() {
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
        showLocation(location);

    }

    private void showLocation(Location location) {
        if (location != null) {
            final String text = String.format("Latitude %.6f, Longitude %.6f",
                    location.getLatitude(),
                    location.getLongitude());


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
                            curretAddress = result.getAdminArea() + " " + result.getCountryName();
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
                            startLocation();
                        } else {
                            getPermission();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
//                        getPermission();
                    }
                }).
                withErrorListener( error -> Toast.makeText(MainActivity.this, "some error", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }
}
