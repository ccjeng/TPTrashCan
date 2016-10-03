package com.ccjeng.tptrashcan.presenter;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ccjeng.tptrashcan.view.base.TPTrashCan;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by andycheng on 2016/9/24.
 */

public class LocationService implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationService.class.getSimpleName();

    // A request to connect to Location Services
    private LocationRequest locationRequest;
    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;
    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;

    /*
   * Define a request code to send to Google Play services This code is returned in
   * Activity.onActivityResult
   */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;

    private Context context;

    private LocationConnectedListener listener;

    public LocationService(Context context) {
        this.context = context;

        // 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

        connect();
    }

    public Location getCurrentLocation() {
        return (currentLocation == null) ? lastLocation : currentLocation;
    }

    public void connect(){
        if (!locationClient.isConnected()) {
            locationClient.connect();
        }
    }

    public void disconnect(){
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
        }
    }


    public void pause(){
        if (locationClient != null) {
            if (locationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        locationClient, this);
            }
        }
    }

    public void setLocationConnectedListener(LocationConnectedListener listener) {
        this.listener = listener;
        Log.d(TAG, "setLocationConnectedListener");
    }

    private Location getLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(locationClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (TPTrashCan.APPDEBUG)
            Log.d(TAG, "onConnected - Connected to location services");

        currentLocation = getLocation();

        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);

        if (TPTrashCan.APPDEBUG)
            Log.d(TAG, "onConnected - isConnected =" + locationClient.isConnected());

        //pass location data to presenter
        listener.onLocationServiceConnected(currentLocation);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        int errorCode = result.getErrorCode();
        Log.i(TAG, "GoogleApiClient connection failed");

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
         //   Utils.showSnackBar(coordinatorlayout, getString(R.string.google_play_service_missing), Utils.Mode.ERROR);

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void configLocationRequest() {
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use low power
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }
}
