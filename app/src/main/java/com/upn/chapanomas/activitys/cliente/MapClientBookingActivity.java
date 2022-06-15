package com.upn.chapanomas.activitys.cliente;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.ConductorProovider;
import com.upn.chapanomas.providers.GeofireProvider;
import com.upn.chapanomas.providers.GoogleAPIProvider;
import com.upn.chapanomas.providers.TokenProvider;
import com.upn.chapanomas.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity  implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private AuthProovider authProovider;

    private GeofireProvider geofireProvider;
    private ClientBookingProvider clientBookingProvider;
    private ConductorProovider conductorProovider;
    private TokenProvider tokenProvider;

    private Marker markerDriver;

    private boolean primeraVez = true;

    private String origin;
    private LatLng originLatLng;

    private String destination;
    private LatLng destinationLatLng;
    private LatLng driverLatLng;

    private GoogleAPIProvider googleAPIProvider;

    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;

    private TextView textViewDriverBooking;
    private TextView textViewEmailDriverBooking;
    private TextView textViewOriginDriverBooking;
    private TextView textViewDestinationDriverBooking;
    private TextView textViewStatusBooking;

    private ValueEventListener listener;
    private String mIdDriver;
    private ValueEventListener listenerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        authProovider = new AuthProovider();
        geofireProvider = new GeofireProvider("conductor_trabajando");
        clientBookingProvider = new ClientBookingProvider();
        conductorProovider = new ConductorProovider();
        tokenProvider = new TokenProvider();

        textViewDriverBooking = findViewById(R.id.textViewDriverBooking);
        textViewEmailDriverBooking = findViewById(R.id.textViewEmailDriverBooking);
        textViewOriginDriverBooking = findViewById(R.id.textViewOriginDriverBooking);
        textViewDestinationDriverBooking = findViewById(R.id.textViewDestinationDriverBooking);
        textViewStatusBooking = findViewById(R.id.textViewStatusBooking);

        googleAPIProvider = new GoogleAPIProvider(MapClientBookingActivity.this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getStatus();
        getClientBooking();
    }

    private void getStatus() {
         listenerStatus = clientBookingProvider.getStatus(authProovider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue().toString();
                    textViewStatusBooking.setText(status);
                    if(status.equals("accept")){
                        textViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if(status.equals("start")){
                        textViewStatusBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    }else if(status.equals("finish")){
                        textViewStatusBooking.setText("Estado: Viaje Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        map.clear();
        map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(destinationLatLng);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null){
            geofireProvider.getDriverLocation(mIdDriver).removeEventListener(listener);
        }
        if(listenerStatus != null){
            clientBookingProvider.getStatus(authProovider.getId()).removeEventListener(listenerStatus);
        }
    }

    private void getClientBooking(){
        clientBookingProvider.getClientBooking(authProovider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    String idDriver = snapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());

                    originLatLng = new LatLng(originLat, originLng);
                    destinationLatLng = new LatLng(destinationLat, destinationLng);
                    textViewOriginDriverBooking.setText("Recoger en: " + origin);
                    textViewDestinationDriverBooking.setText("Destino: " + destination);
                    map.addMarker(new MarkerOptions().position(originLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriver(String idDriver) {
        conductorProovider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();

                    textViewDriverBooking.setText(nombre);
                    textViewEmailDriverBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriverLocation(String idDriver){
        listener =  geofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                    driverLatLng = new LatLng(lat, lng);
                    if(markerDriver != null){
                        markerDriver.remove();
                    }
                    markerDriver = map.addMarker(new MarkerOptions().position(
                            new LatLng(lat, lng)
                            )
                                    .title("Tu conductor")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_moto))
                    );
                    if(primeraVez){
                        primeraVez = false;
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(driverLatLng)
                                        .zoom(14f)
                                        .build()
                        ));
                        drawRoute(originLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        googleAPIProvider.getDirections(driverLatLng, latLng).enqueue(new Callback<String>() {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
    }
}