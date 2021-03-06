package com.upn.chapanomas.activitys.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.upn.chapanomas.R;
import com.upn.chapanomas.clases.ClientBooking;
import com.upn.chapanomas.clases.FCMBody;
import com.upn.chapanomas.clases.FCMResponse;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.GeofireProvider;
import com.upn.chapanomas.providers.GoogleAPIProvider;
import com.upn.chapanomas.providers.NotificationProvider;
import com.upn.chapanomas.providers.TokenProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView animation;
    private TextView textBuscando;
    private Button btnCancelRequest;
    private GeofireProvider geofireProvider;

    private String ExtraOrigin;
    private String ExtraDestination;
    private double ExtraOriginLat;
    private double ExtraOriginLng;
    private double ExtraDestinationLat;
    private double ExtraDestinationLng;
    private LatLng OriginLatLng;
    private LatLng DestinationLatLng;

    private GoogleAPIProvider googleAPIProvider;

    private double radius = 0.1;
    private boolean DriverFound = false;
    private String idDriverFound = "";
    private LatLng DriverFoundLatLng;

    private NotificationProvider notificationProvider;
    private TokenProvider tokenProvider;
    private ClientBookingProvider clientBookingProvider;
    private AuthProovider authProovider;

    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        animation = findViewById(R.id.animation);
        textBuscando = findViewById(R.id.textBuscando);
        btnCancelRequest = findViewById(R.id.btnCancelRequest);

        animation.playAnimation();

        ExtraOrigin = getIntent().getStringExtra("origin");
        ExtraDestination = getIntent().getStringExtra("destination");
        ExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        ExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        ExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        ExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        OriginLatLng = new LatLng(ExtraOriginLat, ExtraOriginLng);
        DestinationLatLng = new LatLng(ExtraDestinationLat, ExtraDestinationLng);

        geofireProvider = new GeofireProvider("conductor_activo");

        notificationProvider = new NotificationProvider();
        tokenProvider = new TokenProvider();
        clientBookingProvider = new ClientBookingProvider();
        authProovider = new AuthProovider();

        googleAPIProvider = new GoogleAPIProvider(RequestDriverActivity.this);

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });

        getClosesDriver();
    }

    private void cancelRequest() {
        clientBookingProvider.delete(authProovider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                sendNotificationCancel();
            }
        });
    }

    private void getClosesDriver(){
        geofireProvider.getActiveDrivers(OriginLatLng, radius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!DriverFound){
                    DriverFound = true;
                    idDriverFound = key;
                    DriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    textBuscando.setText("Conductor encontrado\nEsperando confirmaci??n");
                    createClientBooking();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //CUANDO TERMINA LA BUSQUEDA DE 0.1 KM
                if(!DriverFound){
                    radius = radius + 0.1f;
                    
                    //NO ENCONTR?? CONDUCTOR DISPONIBLE
                    if(radius > 5){
                        textBuscando.setText("No se encontr?? un conductor disponible, intentelo de nuevo m??s tarde");
                        Toast.makeText(RequestDriverActivity.this, "No se encontr?? un conductor disponible, intentelo de nuevo m??s tarde", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        getClosesDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createClientBooking(){
        googleAPIProvider.getDirections(OriginLatLng, DriverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    sendNotification(durationText, distanceText);

                }catch (Exception e){
                    Log.d("Error", "Error encontrado: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void sendNotificationCancel(){
        tokenProvider.getToken(idDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "Viaje cancelado");
                    map.put("body","El cliente cancelo la solicitud");
                    FCMBody fcmBody = new FCMBody(token, "high","4500s", map);
                    notificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1){
                                    Toast.makeText(RequestDriverActivity.this, "La solicitud se cancelo correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n porque el conductor no tiene token de sesi??n", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String time, final String km){
        tokenProvider.getToken(idDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "Solicitud de servicio a " + time + " de tu posici??n");
                    map.put("body","Un cliente esta solicitando un servicio a una distancia de " + km + "\n" + "Recoger en: " + ExtraOrigin + "\n" + "Destino: " + ExtraDestination);
                    map.put("idClient", authProovider.getId());
                    map.put("origin", ExtraOrigin);
                    map.put("destination", ExtraDestination);
                    map.put("min", time);
                    map.put("distance", km);
                    FCMBody fcmBody = new FCMBody(token, "high","4500s", map);
                    notificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1){
                                    ClientBooking clientBooking = new ClientBooking(authProovider.getId(), idDriverFound, ExtraDestination, ExtraOrigin, time, km, "create", ExtraOriginLat, ExtraOriginLng, ExtraDestinationLat, ExtraDestinationLng);

                                    clientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            checkStatusClientBooking();
                                        }
                                    });
                                }else{
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificaci??n porque el conductor no tiene token de sesi??n", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatusClientBooking(){
        listener = clientBookingProvider.getStatus(authProovider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue().toString();
                    if(status.equals("accept")){
                        Intent intent = new Intent(RequestDriverActivity.this, MapClientBookingActivity.class);
                        startActivity(intent);
                        finish();
                    }else if(status.equals("cancel")){
                        Toast.makeText(RequestDriverActivity.this, "Conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null){
            clientBookingProvider.getStatus(authProovider.getId()).removeEventListener(listener);
        }
    }
}