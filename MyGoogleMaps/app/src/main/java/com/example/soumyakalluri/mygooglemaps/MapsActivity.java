package com.example.soumyakalluri.mygooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final int REQUEST_FINE = 2;
    public static final int REQUEST_COARSE = 2;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;

    private static final long MIN_TIME_BTWN_UPDATES = 1000*15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker where you were born and move the camera
        LatLng saltLakeCity = new LatLng(40.584225, -111.854239);
        mMap.addMarker(new MarkerOptions().position(saltLakeCity).title("Born here: Alta View Hospital, Salt Lake City, UT"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(saltLakeCity));

        // Check permission for getting location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("MyMapsApp", "Failed Fine Permission Check");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("MyMapsApp", "Failed Coarse Permission Check");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
        }
        // Show location
        mMap.setMyLocationEnabled(true);
    }

    public void setNormalView(View v)
    {
        if (mMap.getMapType()!=1) mMap.setMapType(1);
    }

    public void setSatelliteView(View v)
    {
        if (mMap.getMapType()!=2) mMap.setMapType(2);
    }

    public void setTerrainView(View v)
    {
        if (mMap.getMapType()!=3) mMap.setMapType(3);
    }

    public void getLocation(View v)
    {
        try
        {
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            // Get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMapsApp","getLocation: GPS is enabled");

            // Get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation: network is enabled");

            if (!(isGPSenabled && isNetworkEnabled))
            {
                Log.d("MyMapsApp","getLocation: No Provider is enabled");
            }
            else
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("MyMapsApp", "Failed Fine Permission Check");
                    Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
                }

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("MyMapsApp", "Failed Coarse Permission Check");
                    Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)));
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
                }

                canGetLocation = true;
                if (isGPSenabled)
                {
                    Log.d("MyMapsApp", "getLocation: GPS is enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMapsApp","getLocation: Network GPS update request success");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT);
                }

                if (isNetworkEnabled)
                {
                    Log.d("MyMapsApp","getLocation: Network is enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMapsApp","getLocation: Network update request success");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }
                Log.d("MyMapsApp","getLocation: Can get location");
            }

        }
        catch (Exception e)
        {
            Log.d("MyMapsApp", "getLocation: Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    public void dropMarker(double lat, double lng) {
        LatLng location = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    public void trackMe(View v)
    {
        getLocation(v);
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output a message in Log.d and Toast
            Log.d("MyMapsApp","locationListenerGPS: Location has changed!");
            Toast.makeText(getApplicationContext(),"Location has changed!", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAmarker)
            dropMarker(location.getLatitude(), location.getLongitude());

            //disable network updates (see LocationManager to remove updates)
            locationManager.removeUpdates(locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a message in Log.d and Toast

            //setup a switch statement on status
            //case: LocationProvider.AVAILABLE --> output a message to Log.d and/or Toast
            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case: LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            //case: default --> request updates from NETWORK_PROVIDER
            //AVAILABLE==2, TEMPORARILY_UNAVAILABLE==1, OUT_OF_SERVICE==0

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            String statusString;
            switch (status) {
                case 0: statusString = "OUT_OF_SERVICE";
                    Log.d("MyMapsApp","status = OUT_OF_SERVICE");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case 1: statusString = "TEMPORARILY_UNAVAILABLE";
                    Log.d("MyMapsApp","status = TEMPORARILY_UNAVAILABLE");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case 2: statusString = "AVAILABLE";
                    Log.d("MyMapsApp","status = AVAILABLE, location is updating");
                    Toast.makeText(getApplicationContext(),"Location Status = AVAILABLE, updating", Toast.LENGTH_SHORT);
                    break;
                default: statusString = "DEFAULT";
                    Log.d("MyMapsApp","status = DEFAULT");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            //output a message in Log.d and Toast
            Log.d("MyMapsApp","locationListenerNetwork: Location has changed!");
            Toast.makeText(getApplicationContext(),"Location has changed!", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropMarker)
            dropMarker(location.getLatitude(), location.getLongitude());

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a message in Log.d and/or Toast
            Log.d("MyMapsApp","locationListenerNetwork: status has changed");
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}
