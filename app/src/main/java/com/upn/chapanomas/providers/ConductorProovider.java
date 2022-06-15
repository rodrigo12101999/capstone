package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.clases.Conductor;

import java.util.HashMap;
import java.util.Map;

public class ConductorProovider {

    DatabaseReference dataBase;

    public ConductorProovider(){
        dataBase = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Conductores");
    }

    public Task<Void> crearConductor(Conductor conductor){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", conductor.getNombre());
        map.put("email", conductor.getEmail());
        map.put("tipoMoto", conductor.getTipoMoto());
        map.put("placaMoto", conductor.getPlaca());

        return dataBase.child(conductor.getId()).setValue(map);
    }

    public  DatabaseReference getDriver(String idDriver){
        return dataBase.child(idDriver);
    }
}
