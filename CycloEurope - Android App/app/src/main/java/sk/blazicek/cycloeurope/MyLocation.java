package sk.blazicek.cycloeurope;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Processing GPS
 *
 * @author Jozef Blazicek
 */
public class MyLocation implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private MainActivity activity;
    private Location lastLocation;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private boolean showGPS = false;

    public MyLocation(MainActivity activity) {
        this.activity = activity;

        googleApiClient = new GoogleApiClient.Builder(activity).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Permission check
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

        initLocation();
    }

    /**
     * Initialize GPS functionality
     */
    private void initLocation() {
        // Permission check
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.getMenu().findItem(R.id.locate).setVisible(false);
            showGPS = false;
            return;
        }

        // Setup GPS
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        // Display Location icon on menu
        Menu menu = activity.getMenu();

        if (menu != null) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                menu.findItem(R.id.locate).setVisible(false);
            else
                menu.findItem(R.id.locate).setVisible(true);
        }

        showGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }



    public void onStart() {
        googleApiClient.connect();
    }

    public void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }



    // LocationListener
    /**
     * Updates location
     * */
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }

    // LocationListener
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // LocationListener
    /**
     * Display location icon if GPS has been enabled
     * */
    @Override
    public void onProviderEnabled(String provider) {
        Menu menu = activity.getMenu();
        if (menu != null) {
            menu.findItem(R.id.locate).setVisible(true);
            showGPS = true;
        }
    }

    // LocationListener
    /**
     * Hide location icon if GPS has been disabled
     * */
    @Override
    public void onProviderDisabled(String provider) {
        Menu menu = activity.getMenu();
        if (menu != null) {
            menu.findItem(R.id.locate).setVisible(false);
            showGPS = false;
        }
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Permission check
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // update location
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }



    /**
     * @return true if GPS is enabled / accessible
     * */
    public boolean showGPS() {
        return showGPS;
    }

    /**
     * @return location from GPS
     * */
    public LatLng getLocation() {
        if (lastLocation == null)
            return null;

        double longitude = lastLocation.getLongitude();
        double getLatitude = lastLocation.getLatitude();
        return new LatLng(getLatitude, longitude);
    }

}
