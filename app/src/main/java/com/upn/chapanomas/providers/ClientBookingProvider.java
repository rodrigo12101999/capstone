package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.clases.ClientBooking;

public class ClientBookingProvider {
    private DatabaseReference database;

    public ClientBookingProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("clientBooking");
    }

    public Task<Void> create(ClientBooking clientBooking){
        return database.child(clientBooking.getIdClient()).setValue(clientBooking);
    }
}
