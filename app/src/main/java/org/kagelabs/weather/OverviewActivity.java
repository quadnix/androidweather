package org.kagelabs.weather;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

// google api client
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class OverviewActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String WEATHER_API_KEY = "1e7d7c4776c9da7f51c0126a24a21cfc";
    private static int NUMBER_OF_OVERVIEW_DAYS = 16;

    // location sucks
    private GoogleApiClient gapi;
    private Location lastKnownLocation;


    // http sucks
    private RequestQueue queue;
    private Cache cache;
    private Network network;
    public String rawJSONResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        System.out.println("starting app...");
        this.cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        this.network = new BasicNetwork(new HurlStack());
        this.queue = new RequestQueue(this.cache, this.network);
        this.queue.start();


        this.buildGoogleApiClient();
        this.gapi.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void execHTTPRequest(StringRequest request) {
        this.queue.add(request);
    }

    public static String getAPICallByCoords(String latitude, String longitude, int numberOfDays) {
        return "http://api.openweathermap.com/data/2.5/forecast/daily?lat=" + latitude + "&long=" + longitude + "&cnt=" + numberOfDays + "&APPID=" + OverviewActivity.WEATHER_API_KEY;
    }

    public static String getAPICallByCityAndCountryCode(String city, String country, int numberOfDays) {
        return "http://api.openweathermap.com/data/2.5/forecast/daily?q=" + city + "," + country + "&cnt=" + numberOfDays + "&APPID=" + OverviewActivity.WEATHER_API_KEY;
    }





    public void getWeather(String apiurl) {
        StringRequest request = new StringRequest(Request.Method.GET, apiurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        setRawJSONResponse(response);
                        System.out.println(response);
                        refreshPage();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println("things are broken.");
            }
        });

        this.execHTTPRequest(request);
    }

    public void refreshPage() {
        System.out.println(this.rawJSONResponse);
    }

    public void setRawJSONResponse(String json) {
        this.rawJSONResponse = json;
    }

    private synchronized void buildGoogleApiClient() {
        System.out.println("Starting google api creation");
        this.gapi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        System.out.println("Created the google api.");

    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("connected to google apis");
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(this.gapi);
        if (lastKnownLocation != null) {
            String api = this.getAPICallByCoords(String.valueOf(lastKnownLocation.getLatitude()), String.valueOf(lastKnownLocation.getLongitude()), this.NUMBER_OF_OVERVIEW_DAYS);
            System.out.println("Curling " + api);
            this.getWeather(api);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("connection to google apis suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("failed to connect to google apis");
    }
}
