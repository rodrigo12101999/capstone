package com.upn.chapanomas.activitys.conductor;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.upn.chapanomas.R;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.GeofireProvider;

public class NotificationBookingActivity extends AppCompatActivity {

    private ClientBookingProvider clientBookingProvider;
    private GeofireProvider geofireProvider;
    private AuthProovider authProovider;

    private TextView textViewDestination;
    private TextView textViewOrigin;
    private TextView textViewMin;
    private TextView textViewDistance;
    private TextView textViewCounter;
    private Button btnAccept;
    private Button btnCancel;

    private String ExtraIdClient;
    private String ExtraOrigin;
    private String ExtraDestination;
    private String ExtraMin;
    private String ExtraDistance;

    private MediaPlayer mediaPlayer;

    private int counter = 20;
    private Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            counter = counter - 1;
            textViewCounter.setText(String.valueOf(counter));
            if(counter > 0){
                initTimer();
            }else{
                cancelBooking();
            }
        }
    };
    private ValueEventListener listener;

    private void initTimer() {
        handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        clientBookingProvider = new ClientBookingProvider();

        textViewDestination = findViewById(R.id.textViewDestination);
        textViewOrigin = findViewById(R.id.textViewOrigin);
        textViewMin = findViewById(R.id.textViewMin);
        textViewDistance = findViewById(R.id.textViewDistance);
        btnAccept = findViewById(R.id.btnAcceptBooking);
        btnCancel = findViewById(R.id.btnCancelBooking);
        textViewCounter = findViewById(R.id.textViewCounter);

        ExtraIdClient = getIntent().getStringExtra("idClient");
        ExtraOrigin = getIntent().getStringExtra("origin");
        ExtraDestination = getIntent().getStringExtra("destination");
        ExtraMin = getIntent().getStringExtra("min");
        ExtraDistance = getIntent().getStringExtra("distance");

        textViewDestination.setText(ExtraDestination);
        textViewOrigin.setText(ExtraOrigin);
        textViewMin.setText(ExtraMin);
        textViewDistance.setText(ExtraDistance);

        mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer.setLooping(true);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();

        checkIfClientCancelBooking();

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });
    }

    private void checkIfClientCancelBooking(){
        listener = clientBookingProvider.getClientBooking(ExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(NotificationBookingActivity.this, "El cliente cancelo el viaje", Toast.LENGTH_LONG).show();
                    if(handler != null) handler.removeCallbacks(runnable);
                    Intent intent = new Intent(NotificationBookingActivity.this, MapConductorActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelBooking() {
        if(handler != null) handler.removeCallbacks(runnable);
        clientBookingProvider.updateStatus(ExtraIdClient,"cancel");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
        Intent intent = new Intent(NotificationBookingActivity.this, MapConductorActivity.class);
        startActivity(intent);
        finish();
    }

    private void acceptBooking() {
        if(handler != null) handler.removeCallbacks(runnable);
        authProovider = new AuthProovider();

        geofireProvider = new GeofireProvider("conductor_activo");
        geofireProvider.removeLocation(authProovider.getId());

        clientBookingProvider = new ClientBookingProvider();
        clientBookingProvider.updateStatus(ExtraIdClient,"accept");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(NotificationBookingActivity.this, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", ExtraIdClient);
        startActivity(intent1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.release();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer != null){
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null) handler.removeCallbacks(runnable);
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
        if(listener != null){
            clientBookingProvider.getClientBooking(ExtraIdClient).removeEventListener(listener);
        }
    }
}