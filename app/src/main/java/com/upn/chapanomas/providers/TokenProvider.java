package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.upn.chapanomas.clases.Token;

public class TokenProvider {

    DatabaseReference database;

    public TokenProvider(){
        database = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(String idUser){
        if(idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                database.child(idUser).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUser){
        return database.child(idUser);
    }

}
