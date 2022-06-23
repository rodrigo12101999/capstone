package com.upn.chapanomas.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.cliente.RegisterClienteActivity;
import com.upn.chapanomas.activitys.conductor.RegisterConductorActivity;

public class SelectOptionAuthActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Button btnIrLogin;
    Button btnIrRegistro;

    private TextView textViewTitulo;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        pref = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Seleccionar opción");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnIrLogin = findViewById(R.id.btnIrLogin);
        textViewTitulo = findViewById(R.id.TituloOpcion);

        textViewTitulo.setText("Apartado de " + pref.getString("usuario", ""));

        btnIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irLogin();
            }
        });

        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irRegistro();
            }
        });
    }

    private void irRegistro() {
        String usuario = pref.getString("usuario", "");
        if(usuario.equals("cliente")){
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterClienteActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterConductorActivity.class);
            startActivity(intent);
        }
    }

    private void irLogin() {
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}