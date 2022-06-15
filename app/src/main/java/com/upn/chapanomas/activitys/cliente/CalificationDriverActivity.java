package com.upn.chapanomas.activitys.cliente;

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
import com.upn.chapanomas.clases.ClientBooking;
import com.upn.chapanomas.clases.HistoryBooking;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClientBookingProvider;
import com.upn.chapanomas.providers.HistoryBookingProovider;

import java.util.Date;

public class CalificationDriverActivity extends AppCompatActivity {

    private TextView textViewOrigin;
    private TextView textViewDestination;
    private RatingBar ratingBar;
    private Button buttonCalifitacion;

    private ClientBookingProvider clientBookingProvider;
    private AuthProovider authProovider;

    private HistoryBooking historyBooking;
    private HistoryBookingProovider historyBookingProovider;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_driver);

        textViewOrigin = findViewById(R.id.textViewOriginCalification);
        textViewDestination = findViewById(R.id.textViewDestinationCalification);
        ratingBar = findViewById(R.id.ratingbarCalification);
        buttonCalifitacion = findViewById(R.id.btnCalification);

        clientBookingProvider = new ClientBookingProvider();
        historyBookingProovider = new HistoryBookingProovider();
        authProovider = new AuthProovider();

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
        clientBookingProvider.getClientBooking(authProovider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
            historyBooking.setCalificationDriver(mCalification);
            historyBooking.setTimestamp(new Date().getTime());
            historyBookingProovider.getHistoryBooking(historyBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        historyBookingProovider.updateCalificationDriver(historyBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationDriverActivity.this, "Calificación guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationDriverActivity.this, MapClientActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }else{
                        historyBookingProovider.create(historyBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationDriverActivity.this, "Calificación guardada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationDriverActivity.this, MapClientActivity.class);
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