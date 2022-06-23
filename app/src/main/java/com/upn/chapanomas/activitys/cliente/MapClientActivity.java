package com.upn.chapanomas.activitys.cliente;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.MainActivity;
import com.upn.chapanomas.includes.MyToolbar;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.GeofireProvider;
import com.upn.chapanomas.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private AuthProovider authProovider;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocation;

    private GeofireProvider geofireProvider;
    private TokenProvider tokenProvider;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;

    private LatLng currentLatLng;

    private List<Marker> driversMarkers = new ArrayList<>();

    private boolean primeraVez = true;

    private PlacesClient PlacesC;
    private AutocompleteSupportFragment AutoComplete;
    private AutocompleteSupportFragment AutoCompleteDestination;

    private String mOrigin;
    private LatLng mOriginLatLng;

    private String mDestination;
    private LatLng mLatLngDestination;

    private GoogleMap.OnCameraIdleListener CameraListener;

    private Button btnRequestDirver;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    /*
                    if(marker != null){
                        marker.remove();
                    }

                    marker = map.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_ubicacion))
                    );*/

                    map.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));

                    if (primeraVez) {
                        primeraVez = false;
                        getActiveDrivers();
                        limitSearch();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_cliente);

        MyToolbar.show(this, "Cliente", false);

        authProovider = new AuthProovider();
        geofireProvider = new GeofireProvider("conductor_activo");
        tokenProvider = new TokenProvider();
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRequestDirver = findViewById(R.id.btnRequestDirver);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        PlacesC = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();

        btnRequestDirver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDriver();
            }
        });

        generateToken();
    }

    private void requestDriver(){
        if(mOriginLatLng != null && mLatLngDestination != null){
            Intent intent = new Intent(MapClientActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat", mOriginLatLng.latitude);
            intent.putExtra("origin_lng", mOriginLatLng.longitude);
            intent.putExtra("destination_lat", mLatLngDestination.latitude);
            intent.putExtra("destination_lng", mLatLngDestination.longitude);
            intent.putExtra("origin", mOrigin);
            intent.putExtra("destination", mDestination);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void limitSearch(){
        LatLng northSide = SphericalUtil.computeOffset(currentLatLng, 5000,0);
        LatLng southSide = SphericalUtil.computeOffset(currentLatLng, 5000,180);
        AutoComplete.setCountry("PER");
        AutoComplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        AutoCompleteDestination.setCountry("PER");
        AutoCompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void onCameraMove(){
        CameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try{
                    Geocoder geocoder = new Geocoder(MapClientActivity.this);
                    mOriginLatLng = map.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude, mOriginLatLng.longitude, 1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    AutoComplete.setText(address + " " + city+ " " + country);
                }catch (Exception e){
                    Log.d("Error", "Mensaje error: " + e.getMessage());
                }
            }
        };
    }

    private void instanceAutocompleteOrigin(){
        AutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeaAutocompleteOrigin);
        AutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        AutoComplete.setHint("Lugar de recogida");
        AutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {

            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLng = place.getLatLng();

                Log.d("PLACE", "Name: " + mOrigin);
                Log.d("PLACE", "Lat: " + mOriginLatLng.latitude);
                Log.d("PLACE", "Lng: " + mOriginLatLng.longitude);
            }
        });
    }

    private void instanceAutocompleteDestination(){
        AutoCompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeaAutocompleteDestination);
        AutoCompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        AutoCompleteDestination.setHint("Destino");
        AutoCompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {

            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mLatLngDestination = place.getLatLng();

                Log.d("PLACE", "Name: " + mDestination);
                Log.d("PLACE", "Lat: " + mLatLngDestination.latitude);
                Log.d("PLACE", "Lng: " + mLatLngDestination.longitude);
            }
        });
    }

    private void getActiveDrivers() {
        geofireProvider.getActiveDrivers(currentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for (Marker marker : driversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = map.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor Disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_moto)));
                marker.setTag(key);
                driversMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : driversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            driversMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : driversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnCameraIdleListener(CameraListener);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActived()){
                        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        map.setMyLocationEnabled(true);
                    }else{
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            map.setMyLocationEnabled(true);
        }else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor active su ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                })
                .create()
                .show();
    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActived()){
                    fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    map.setMyLocationEnabled(true);
                }else{
                    showAlertDialogNOGPS();
                }
            }else{
                checkLocationPermissions();
            }
        }else{
            if(gpsActived()){
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(true);
            }else{
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permismos de ubicación para poder ser usada")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.client_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.action_logout){
            logout();
        }

        /*if(item.getItemId() == R.id.action_update){
            Intent intent = new Intent(MapClientActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }*/

        return super.onOptionsItemSelected(item);
    }

    void logout(){
        authProovider.logout();
        Intent intent = new Intent(MapClientActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generateToken(){
        tokenProvider.create(authProovider.getId());
    }
}