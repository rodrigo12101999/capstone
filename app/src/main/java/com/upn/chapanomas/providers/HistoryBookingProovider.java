package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.clases.HistoryBooking;

import java.util.HashMap;
import java.util.Map;

public class HistoryBookingProovider {

    private DatabaseReference database;

    public HistoryBookingProovider(){
        database = FirebaseDatabase.getInstance().getReference().child("historyBooking");
    }

    public Task<Void> create(HistoryBooking historyBooking){
        return database.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    public Task<Void> updateCalificationClient(String idHistoryBooking, float calificationClient){
        Map<String, Object> map = new HashMap<>();
        map.put("calificationClient", calificationClient);
        return database.child(idHistoryBooking).updateChildren(map);
    }

    public Task<Void> updateCalificationDriver(String idHistoryBooking, float calificationDriver){
        Map<String, Object> map = new HashMap<>();
        map.put("calificationDriver", calificationDriver);
        return database.child(idHistoryBooking).updateChildren(map);
    }

    public DatabaseReference getHistoryBooking(String idHistoryBooking){
        return database.child(idHistoryBooking);
    }
}
