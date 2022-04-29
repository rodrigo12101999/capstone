package com.upn.chapanomas.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProovider {

    FirebaseAuth Auth;

    public AuthProovider(){
        Auth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> registrar(String email, String password){
        return Auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> login(String email, String password){
        return Auth.signInWithEmailAndPassword(email, password);
    }

    public void logout(){
        Auth.signOut();
    }

    public String getId(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public boolean existSession(){
        boolean exist = false;
        if(Auth.getCurrentUser() != null){
            exist = true;
        }
        return exist;
    }
}
