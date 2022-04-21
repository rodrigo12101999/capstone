package com.upn.chapanomas.activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.cliente.MapClienteActivity;
import com.upn.chapanomas.activitys.conductor.MapConductorActivity;

public class MainActivity extends AppCompatActivity {

    Button soyCliente;
    Button soyConductor;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        soyCliente = findViewById(R.id.btnSoyCliente);
        soyConductor = findViewById(R.id.btnSoyConductor);

        soyCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("usuario", "cliente");
                editor.apply();
                goToSelectAuth();
            }
        });

        soyConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("usuario", "conductor");
                editor.apply();
                goToSelectAuth();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String usuario = pref.getString("usuario", "");
            if(usuario.equals("cliente")){
                Intent intent = new Intent(MainActivity.this, MapClienteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this, MapConductorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        startActivity(intent);
    }
}