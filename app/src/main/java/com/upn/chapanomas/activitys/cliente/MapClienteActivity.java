package com.upn.chapanomas.activitys.cliente;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.MainActivity;
import com.upn.chapanomas.providers.AuthProovider;

public class MapClienteActivity extends AppCompatActivity {

    Button btnLogout;
    AuthProovider authProovider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_cliente);

        btnLogout = findViewById(R.id.btnLogout);

        authProovider = new AuthProovider();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProovider.logout();
                Intent intent = new Intent(MapClienteActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}