package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.clases.Cliente;

import java.util.HashMap;
import java.util.Map;

public class ClienteProovider {

    DatabaseReference dataBase;

    public ClienteProovider(){
        dataBase = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Clientes");
    }

    public Task<Void> crearCliente(Cliente cliente){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", cliente.getNombre());
        map.put("email", cliente.getEmail());

        return dataBase.child(cliente.getId()).setValue(map);
    }

    public DatabaseReference getClient(String idClient) {
        return dataBase.child(idClient);
    }
}
