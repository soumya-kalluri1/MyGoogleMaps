package com.example.soumyakalluri.mygooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
    private Location myLocation;

    private static final long MIN_TIME_BTWN_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private static final float MY_LOC_ZOOM_FACTOR = 15f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MyMapsApp", "onCreate: map has been created");
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
        Log.d("MyMapsApp", "marker for birthplace added!");

        // Check permission for getting location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Fine Permission Check");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Coarse Permission Check");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
        }
        // Show location
        mMap.setMyLocationEnabled(true);
    }

    public void setNormalView(View v) {
        if (mMap.getMapType() != 1) mMap.setMapType(1);
    }

    public void setSatelliteView(View v) {
        if (mMap.getMapType() != 2) mMap.setMapType(2);
    }

    public void setTerrainView(View v) {
        if (mMap.getMapType() != 3) mMap.setMapType(3);
    }

    public void trackMe(View v) {
        if (!(isGPSenabled && isNetworkEnabled)) {
            Log.d("MyMapsApp", "trackMe: calling getLocation");
            getLocation(v);
            Log.d("MyMapsApp", "trackMe: called getLocation");
        } else {
            isGPSenabled = isNetworkEnabled = false;
            locationManager.removeUpdates(locationListenerGPS);
            Log.d("MyMapsApp", "trackMe: removed GPS updates");
            locationManager.removeUpdates(locationListenerNetwork);
            Log.d("MyMapsApp", "trackMe: removed Network updates");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Log.d("MyMapsApp", "trackMe: setMyLocationEnabled = true");
        }
    }

    public void getLocation(View v) {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            // Get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMapsApp", "getLocation: GPS is enabled");
            else Log.d("MyMapsApp", "getLocation: GPS is disabled");

            // Get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled == true) {
                Log.d("MyMapsApp", "getLocation: isNetworkEnabled=" + isNetworkEnabled);
            } else {
                Log.d("MyMapsApp", "getLocation: isNetworkEnabled=" + isNetworkEnabled);
            }

            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "isGPSenabled = " + isGPSenabled + ", isNetworkEnabled = " + isNetworkEnabled);
                Log.d("MyMapsApp", "getLocation: Provider is not enabled");
            } else {
                //isGPSenabled = isNetworkEnabled = true;
                mMap.setMyLocationEnabled(false);
                Log.d("MyMapsApp", "getLocation: setMyLocationEnabled = false");
                this.canGetLocation = true;
                if (isGPSenabled) {
                    Log.d("MyMapsApp", "getLocation: GPS is enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMapsApp", "getLocation: GPS update request success");
                    Toast.makeText(this, "Using GPS", Toast.LENGTH_SHORT).show();
                }

                if (isNetworkEnabled) {
                    Log.d("MyMapsApp", "getLocation: Network is enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMapsApp", "getLocation: Network update request success");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT).show();
                }
                Log.d("MyMapsApp", "getLocation: Can get location");

                //initial location
                dropAMarker("GPS");
            }

        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught an exception in getLocation");
            e.printStackTrace();
        }
    }

    public void dropAMarker(String provider) {
        LatLng userLocation;
        if (locationManager != null) {
            Log.d("MyMapsApp","dropAMarker: locationManager != null");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp","dropAMarker: permissions failed");
                return;
            }
            myLocation = new Location(provider);
        }

        if (myLocation == null) {
            //display a message in Log.d and/or Toast
            Log.d("MyMapsApp", "dropAMarker: Location is null");
            Toast.makeText(getApplicationContext(), "myLocation is invalid",Toast.LENGTH_SHORT).show();
        } else {
            //Add a shape for your marker
            if (provider.equals("Network")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.GREEN));
                Log.d("MyMapsApp", "dropAMarker: Network Marker added!");
            } else {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE));
                Log.d("MyMapsApp", "dropAMarker: GPS Marker added!");
            }

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            mMap.animateCamera(update);
        }
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output a message in Log.d and Toast
            Log.d("MyMapsApp", "locationListenerGPS: Location has changed! GPS is running");
            Toast.makeText(getApplicationContext(), "Location has changed! GPS is running", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAmarker)
            dropAMarker("GPS");

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

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            String statusString;
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString + ", location is updating");
                    Toast.makeText(getApplicationContext(), "Location Status = " + statusString + ", updating", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    statusString = "DEFAULT";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
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
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            //output a message in Log.d and Toast
            Log.d("MyMapsApp", "locationListenerNetwork: Location has changed! Network is running");
            Toast.makeText(getApplicationContext(), "Location has changed! Network is running", Toast.LENGTH_SHORT).show();

            //drop a marker on the map (create a method called dropAMarker)
            dropAMarker("Network");

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a message in Log.d and/or Toast
            Log.d("MyMapsApp", "locationListenerNetwork: status has changed");

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            String statusString;
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString+", location is updating");
                    Toast.makeText(getApplicationContext(),"Location Status = "+statusString+", updating", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    statusString = "DEFAULT";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}
