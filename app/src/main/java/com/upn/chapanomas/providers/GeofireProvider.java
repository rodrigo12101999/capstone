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

    public GeofireProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("conductor_activo");
        geoFire = new GeoFire(database);
    }

    public void saveLocation(String idConductor, LatLng latLng){
        geoFire.setLocation(idConductor, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String idConductor){
        geoFire.removeLocation(idConductor);
    }

    public GeoQuery getActiveDrivers(LatLng latLng){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 30);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

}

