package com.upn.chapanomas.activitys.conductor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.cliente.CalificationDriverActivity;
import com.upn.chapanomas.activitys.cliente.MapClientActivity;
import com.upn.chapanomas.clases.ClientBooking;
import com.upn.chapanomas.clases.HistoryBooking;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.HistoryBookingProovider;

import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private TextView textViewOrigin;
    private TextView textViewDestination;
    private RatingBar ratingBar;
    private Button buttonCalifitacion;

    private ClientBookingProvider clientBookingProvider;

    private String ExtraClientId;

    private HistoryBooking historyBooking;
    private HistoryBookingProovider historyBookingProovider;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);

        textViewOrigin = findViewById(R.id.textViewOriginCalification);
        textViewDestination = findViewById(R.id.textViewDestinationCalification);
        ratingBar = findViewById(R.id.ratingbarCalification);
        buttonCalifitacion = findViewById(R.id.btnCalification);

        clientBookingProvider = new ClientBookingProvider();
        historyBookingProovider = new HistoryBookingProovider();

        ExtraClientId = getIntent().getStringExtra("idClient");

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {
                mCalification = calification;
            }
        });

        buttonCalifitacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });

        getClientBooking();
    }

    private void getClientBooking(){
        clientBookingProvider.getClientBooking(ExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    textViewOrigin.setText(clientBooking.getOrigin());
                    textViewDestination.setText(clientBooking.getDestination());
                    historyBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificate() {
        if(mCalification > 0){
            historyBooking.setCalificationClient(mCalification);
            historyBooking.setTimestamp(new Date().getTime());
            historyBookingProovider.getHistoryBooking(historyBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        historyBookingProovider.updateCalificationClient(historyBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationClientActivity.this, "Calificación guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapClientActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }else{
                        historyBookingProovider.create(historyBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationClientActivity.this, "Calificación guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapConductorActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            Toast.makeText(this, "Debe ingresar una calificación", Toast.LENGTH_SHORT).show();
        }
    }
}