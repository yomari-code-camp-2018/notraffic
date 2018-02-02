package com.example.rosha.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng loc, loc_origin;
    String sendData, response;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    double end_latitude, end_longitude;


    public MapsActivity() throws IOException {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
            Toast.makeText(MapsActivity.this, "Please install Google Play Services", Toast.LENGTH_LONG).show();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(false);
        }

        // mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onClick(View v) throws IOException {
        final String location;
        String origin;
        final String locationData;
//        dataSender d = new dataSender();
        final Object[][] dataTransfer = {new Object[2]};
        final Object[][] dataTransfer_origin = {new Object[2]};
        // GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();


        switch (v.getId()) {
            case R.id.B_search: {
                EditText tf_location = (EditText) findViewById(R.id.destination);
                EditText from_location = (EditText) findViewById(R.id.origin);
                location = tf_location.getText().toString();
                locationData = from_location.getText().toString();
                sendData = locationData + " = " + location;
                class TimeconsumingAsynctask extends AsyncTask<String, String, String> {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }


                    @Override
                    protected String doInBackground(String... strings) {
                        sendd(sendData);
                        return "Success";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        List<Address> addressList = null;
                        List<Address> addressList_origin = null;
                        MarkerOptions markerOptions = new MarkerOptions();
                        MarkerOptions markerOptions_origin = new MarkerOptions();
                        Log.d("location = ", location);

                        if (!locationData.equals("")) {
                            Geocoder geocoder1 = new Geocoder(MapsActivity.this);
                            try {
                                addressList_origin = geocoder1.getFromLocationName(locationData, 1);
                                Log.d("MapsActivity", "ADDRESS LIST DATA :" + addressList_origin.toString());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!location.equals("")) {
                            Geocoder geocoder = new Geocoder(MapsActivity.this);
                            try {
                                addressList = geocoder.getFromLocationName(location, 1);
                                Log.d("MapsActivity", "ADDRESS LIST DATA :" + addressList.toString());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (addressList != null) {
                                for (int i = 0; i < addressList.size(); i++) {
                                    mMap.clear();
                                    Address myAddress_origin = addressList_origin.get(i);//added
                                    Address myAddress = addressList.get(i);
                                    LatLng latLng_origin = new LatLng(myAddress_origin.getLatitude(), myAddress_origin.getLongitude());//added
                                    LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                                    loc = latLng;
                                    loc_origin = latLng_origin;//added
                                    dataTransfer[0] = new Object[3];
                                    dataTransfer_origin[0] = new Object[3];//added
                                    String url = getDirectionsUrl(loc, loc_origin);//function changed
                                    GetDirectionsData getDirectionsData = new GetDirectionsData();
                                    //GetDirectionsData getDirectionsData_origin = new GetDirectionsData();//added
                                    dataTransfer[0][0] = mMap;
                                    dataTransfer[0][1] = url;
                                    dataTransfer[0][2] = latLng;
                            /*dataTransfer_origin[0] = mMap;//added
                            dataTransfer_origin[1] = url;//added
                            dataTransfer_origin[2] = latLng_origin;//added*/
                                    getDirectionsData.execute(dataTransfer[0]);
                                    //getDirectionsData_origin.execute(dataTransfer_origin);//added
                                    markerOptions.position(latLng);
                                    markerOptions_origin.position(latLng_origin);
                                    mMap.addMarker(markerOptions);
                                    mMap.addMarker(markerOptions_origin);
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                }

                            }

                        }

                    }


                }
                TimeconsumingAsynctask asynctask = new TimeconsumingAsynctask();
                asynctask.execute();
            }
        }
    }




    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?&alternatives=true");
        googleDirectionsUrl.append("&origin=" + origin.latitude + "," + origin.longitude);
        googleDirectionsUrl.append("&destination=" + destination.latitude + "," + destination.longitude);
        googleDirectionsUrl.append("&key=" + "AIzaSyAI5RjkaHpSmiRNjrnzsQZImfzAXYLyEO8");

        return googleDirectionsUrl.toString();
    }

   /* private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBj-cnmMUY21M0vnIKz0k3tD3bRdyZea-Y");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }*/


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f));


        Toast.makeText(MapsActivity.this, "Your Current Location", Toast.LENGTH_LONG).show();


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        //marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;

        Log.d("end_lat", "" + end_latitude);
        Log.d("end_lng", "" + end_longitude);
    }

    protected String sendd(String latlngdata) {

        Log.i("myLog", "Wating to send");
        int port = 1234;
        Socket dataSocket = null;
        String ipString = "192.168.43.42";
        try {
            dataSocket = new Socket(ipString, port);
            Log.i("myLog", "initialized");
            DataOutputStream dos = null;
            dos = new DataOutputStream(dataSocket.getOutputStream());
            dos.write(Integer.parseInt(latlngdata));
            Log.i("myLog", "Sent");
            dos.flush();
            dos.close();
            dataSocket.close();
        } catch (Exception e) {
            Log.i("myLog", e.toString());
        }
        Log.i("myLog", "Wating to receive");
        port = 123;
        try {
            dataSocket = new Socket(ipString, port);
            Log.i("myLog", "initialized");
            BufferedReader in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            Log.i("myLog", "Received");
            Log.i("myLog", in.toString());
            response = in.readLine();
            Log.i("myLog", response);
            dataSocket.close();
        } catch (Exception e) {
            Log.i("myLog", e.toString());
        }
        Log.i("myLog", "all complete");

        return "Success";
    }
}
