package com.upn.chapanomas.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {
    private DatabaseReference database;
    private GeoFire geoFire;

    public GeofireProvider(String reference){
        database = FirebaseDatabase.getInstance().getReference().child(reference);
        geoFire = new GeoFire(database);
    }

    public void saveLocation(String idConductor, LatLng latLng){
        geoFire.setLocation(idConductor, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String idConductor){
        geoFire.removeLocation(idConductor);
    }

    public GeoQuery getActiveDrivers(LatLng latLng, double radius){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference getDriverLocation(String idDriver){
        return database.child(idDriver).child("l");
    }

    public DatabaseReference isDriverWorking(String idDriver){
        return FirebaseDatabase.getInstance().getReference().child("conductor_trabajando").child(idDriver);
    }
}

