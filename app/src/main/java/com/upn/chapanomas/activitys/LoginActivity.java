package com.upn.chapanomas.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.cliente.MapClientActivity;
import com.upn.chapanomas.activitys.conductor.MapConductorActivity;
import com.upn.chapanomas.includes.MyToolbar;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences pref;

    TextInputEditText textInputEmail;
    TextInputEditText textInputPassword;
    Button btnLogin;

    FirebaseAuth auth;
    DatabaseReference dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        MyToolbar.show(this, "Login de " + pref.getString("usuario", ""), true);

        textInputEmail = findViewById(R.id.textInputEmail);
        textInputPassword = findViewById(R.id.textInputPassword);
        btnLogin = findViewById(R.id.btnIngresar);

        auth = FirebaseAuth.getInstance();
        dataBase = FirebaseDatabase.getInstance().getReference();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingresar();
            }
        });
    }

    private void ingresar() {
        String email = textInputEmail.getText().toString();
        String password = textInputPassword.getText().toString();

        if(!email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String usuario = pref.getString("usuario", "");
                            if(usuario.equals("cliente")){
                                Intent intent = new Intent(LoginActivity.this, MapClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(LoginActivity.this, MapConductorActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "El correo o la contraseña son incorrectas", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Toast.makeText(LoginActivity.this, "La contraseña debe tener mas de 6 dígitos", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this, "La email y contraseña obligatorias", Toast.LENGTH_SHORT).show();
        }
    }
}