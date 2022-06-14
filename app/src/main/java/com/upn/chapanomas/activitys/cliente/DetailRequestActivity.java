package com.upn.chapanomas.activitys.cliente;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.upn.chapanomas.R;
import com.upn.chapanomas.includes.MyToolbar;
import com.upn.chapanomas.providers.GoogleAPIProvider;
import com.upn.chapanomas.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private double ExtraOriginLat;
    private double ExtraOriginLng;

    private double ExtraDestinationLat;
    private double ExtraDestinationLng;

    private String ExtraOrigin;
    private String ExtraDestination;

    private LatLng originLatLng;
    private LatLng destinationLatLng;

    private GoogleAPIProvider googleAPIProvider;

    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;

    private TextView textOrigin;
    private TextView textDestination;
    private TextView textTime;
    private TextView textDistance;

    private Button buttonRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        MyToolbar.show(this, "Tus datos", true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        ExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);

        ExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        ExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);

        ExtraOrigin = getIntent().getStringExtra("origin");
        ExtraDestination = getIntent().getStringExtra("destination");

        originLatLng = new LatLng(ExtraOriginLat, ExtraOriginLng);
        destinationLatLng = new LatLng(ExtraDestinationLat, ExtraDestinationLng);

        googleAPIProvider = new GoogleAPIProvider(DetailRequestActivity.this);

        textOrigin = findViewById(R.id.textOrigin);
        textDestination = findViewById(R.id.textDestination);
        textTime = findViewById(R.id.textTime);
        textDistance = findViewById(R.id.textDistance);
        buttonRequest = findViewById(R.id.btnRequestNow);

        textOrigin.setText(ExtraOrigin);
        textDestination.setText(ExtraDestination);

        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRequestDriver();
            }
        });
    }

    private void goToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_lat", originLatLng.latitude);
        intent.putExtra("origin_lng", originLatLng.longitude);
        intent.putExtra("origin", ExtraOrigin);
        intent.putExtra("destination", ExtraDestination);
        intent.putExtra("destination_lat", destinationLatLng.latitude);
        intent.putExtra("destination_lng", destinationLatLng.longitude);
        startActivity(intent);
        finish();
    }

    private void drawRoute(){
        googleAPIProvider.getDirections(originLatLng, destinationLatLng).enqueue(new Callback<String>() {
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
                    textTime.setText(durationText);
                    textDistance.setText(distanceText);
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.addMarker(new MarkerOptions().position(originLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
        map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(originLatLng)
                        .zoom(14f)
                        .build()
        ));

        drawRoute();
    }
}