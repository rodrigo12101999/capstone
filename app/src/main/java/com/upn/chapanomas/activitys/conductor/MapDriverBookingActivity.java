package com.upn.chapanomas.activitys.conductor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.upn.chapanomas.R;
import com.upn.chapanomas.clases.FCMBody;
import com.upn.chapanomas.clases.FCMResponse;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.ClienteProovider;
import com.upn.chapanomas.providers.GeofireProvider;
import com.upn.chapanomas.providers.GoogleAPIProvider;
import com.upn.chapanomas.providers.NotificationProvider;
import com.upn.chapanomas.providers.TokenProvider;
import com.upn.chapanomas.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private AuthProovider authProovider;
    private GeofireProvider geofireProvider;
    private ClienteProovider clienteProovider;
    private ClientBookingProvider clientBookingProvider;
    private TokenProvider tokenProvider;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;

    private LatLng currentLatLng;

    private TextView textViewClientBooking;
    private TextView textViewEmailClientBooking;
    private TextView textViewOriginClientBooking;
    private TextView textViewDestinationClientBooking;

    private String ExtraClientId;

    private LatLng originLatLng;
    private LatLng destinationLatLng;

    private GoogleAPIProvider googleAPIProvider;

    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;

    private boolean isFirstTime = true;
    private boolean isCloseToClient = false;

    private Button buttonStartBooking;
    private Button buttonFinishBooking;

    private NotificationProvider notificationProvider;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if(marker != null){
                        marker.remove();
                    }

                    marker = map.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_moto))
                    );

                    map.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(18f)
                                    .build()
                    ));

                    updateLocation();

                    if(isFirstTime){
                        isFirstTime = false;
                        getClientBooking();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);
        authProovider = new AuthProovider();
        geofireProvider = new GeofireProvider("conductor_trabajando");
        tokenProvider = new TokenProvider();
        clienteProovider = new ClienteProovider();
        clientBookingProvider = new ClientBookingProvider();
        notificationProvider = new NotificationProvider();

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textViewClientBooking = findViewById(R.id.textViewClientBooking);
        textViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        textViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        textViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        buttonStartBooking = findViewById(R.id.btnStartBooking);
        buttonFinishBooking = findViewById(R.id.btnFinishBooking);

        ExtraClientId = getIntent().getStringExtra("idClient");
        googleAPIProvider = new GoogleAPIProvider(MapDriverBookingActivity.this);


        getClient();

        buttonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCloseToClient){
                    startBooking();
                }else{
                    Toast.makeText(MapDriverBookingActivity.this, "Debes estar mas cerca a la posición de recogida", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        buttonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });
    }

    private void finishBooking() {
        clientBookingProvider.updateStatus(ExtraClientId, "finish");
        clientBookingProvider.updateIdHistoryBooking(ExtraClientId);
        sendNotification("Viaje finalizado");
        if(fusedLocation != null){
            fusedLocation.removeLocationUpdates(locationCallback);
        }
        geofireProvider.removeLocation(authProovider.getId());
        Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClientActivity.class);
        intent.putExtra("idClient", ExtraClientId);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        clientBookingProvider.updateStatus(ExtraClientId, "start");
        buttonStartBooking.setVisibility(View.GONE);
        buttonFinishBooking.setVisibility(View.VISIBLE);
        map.clear();
        map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(destinationLatLng);

        sendNotification("Viaje iniciado");
    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng){
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");

        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);

        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);

        distance = clientLocation.distanceTo(driverLocation);

        return distance;
    }

    private void getClientBooking(){
        clientBookingProvider.getClientBooking(ExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());

                    originLatLng = new LatLng(originLat, originLng);
                    destinationLatLng = new LatLng(destinationLat, destinationLng);
                    textViewOriginClientBooking.setText("Recoger en: " + origin);
                    textViewDestinationClientBooking.setText("Destino: " + destination);
                    map.addMarker(new MarkerOptions().position(originLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                    drawRoute(originLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        googleAPIProvider.getDirections(currentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    polylineList = DecodePoints.decodePoly(points);
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.DKGRAY);
                    polylineOptions.width(13f);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(polylineList);
                    map.addPolyline(polylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                }catch (Exception e){
                    Log.d("Error", "Error encontrado: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void getClient(){
        clienteProovider.getClient(ExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String email = snapshot.child("email").getValue().toString();
                    String nombre = snapshot.child("nombre").getValue().toString();
                    textViewClientBooking.setText(nombre);
                    textViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLocation(){
        if(authProovider.existSession() && currentLatLng != null ){
            geofireProvider.saveLocation(authProovider.getId(),currentLatLng);
            if(!isCloseToClient){
                if(originLatLng != null && currentLatLng != null){
                    double distance = getDistanceBetween(originLatLng, currentLatLng); //metros
                    if(distance <= 200){
                        buttonStartBooking.setEnabled(true);
                        isCloseToClient = true;
                        Toast.makeText(this, "Estas cerca a la posición del cliente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

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
                    if(gpsActive()){
                        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
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

    private boolean gpsActive() {
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

        if (requestCode == SETTINGS_REQUEST_CODE && gpsActive()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }else{
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

    private void disconnect() {

        if(fusedLocation != null){
            fusedLocation.removeLocationUpdates(locationCallback);
            if(authProovider.existSession()){
                geofireProvider.removeLocation(authProovider.getId());
            }
        }else{
            Toast.makeText(this,"No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }

    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActive()){
                    fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }else{
                    showAlertDialogNOGPS();
                }
            }else{
                checkLocationPermissions();
            }
        }else{
            if(gpsActive()){
                fusedLocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
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
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }else{
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status){
        tokenProvider.getToken(ExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "Estado de tu viaje");
                    map.put("body","Tu estado del viaje es: " + status);
                    FCMBody fcmBody = new FCMBody(token, "high","4500s", map);
                    notificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación porque el conductor no tiene token de sesión", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}