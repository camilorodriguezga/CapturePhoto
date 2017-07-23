package source.open.capturephoto.util;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import source.open.capturephoto.CameraActivity;
import source.open.capturephoto.R;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String CONNECTION_SUSPENDED = "Connection suspended";
    public static final String CONNECTION_FAILED = "Connection failed";
    private static final String TAG = CameraActivity.class.getCanonicalName();
    private Context context;
    // Provides the entry point to Google Play services.
    public GoogleApiClient mGoogleApiClient;
    // Represents a geographical location.
    public android.location.Location mLastLocation;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static final int UPDATE_INTERVAL = 10000; // 10 sec
    private static final int FATEST_INTERVAL = 5000; // 5 sec
    private static final int DISPLACEMENT = 10; // 10 meters
    // localization data
    public double latitude;
    public double longitude;

    public LocationHelper(Context context) {
        this.context = context;
        buildGoogleApiClient();
        createLocationRequest();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to reestablish the connection.
        mGoogleApiClient.connect();
        Log.i(TAG, CONNECTION_SUSPENDED);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, CONNECTION_FAILED + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        getLocation();
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        // Create the location client to start receiving updates
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Method to display the location on UI
     */
    private void getLocation() {

        // get last known recent location.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // get las localization
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            // get latitude and longitude
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        } else {
            // update localization
            startLocationUpdates();
        }
    }

    public static void showMessageGPSNoEnabled(final Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.text_alert_tittle_gps_enable))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.accept), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     *  Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Starting the location updates
     */
    public void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
}
