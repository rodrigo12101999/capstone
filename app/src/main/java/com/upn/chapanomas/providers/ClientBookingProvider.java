package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.clases.ClientBooking;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {
    private DatabaseReference database;

    public ClientBookingProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("clientBooking");
    }

    public Task<Void> create(ClientBooking clientBooking){
        return database.child(clientBooking.getIdClient()).setValue(clientBooking);
    }

    public Task<Void> updateStatus(String idClientBooking, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return database.child(idClientBooking).updateChildren(map);
    }

    public Task<Void> updateIdHistoryBooking(String idClientBooking){
        String idPush = database.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return database.child(idClientBooking).updateChildren(map);
    }

    public DatabaseReference getStatus(String idClientBooking){
        return database.child(idClientBooking).child("status");
    }

    public DatabaseReference getClientBooking(String idClientBooking){
        return database.child(idClientBooking);
    }

    public Task<Void> delete(String idClientBooking){
        return database.child(idClientBooking).removeValue();
    }
}
