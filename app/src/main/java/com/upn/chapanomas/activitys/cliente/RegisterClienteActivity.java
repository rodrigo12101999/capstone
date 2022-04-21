package com.upn.chapanomas.activitys.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.upn.chapanomas.R;
import com.upn.chapanomas.activitys.conductor.MapConductorActivity;
import com.upn.chapanomas.activitys.conductor.RegisterConductorActivity;
import com.upn.chapanomas.clases.Cliente;
import com.upn.chapanomas.includes.MyToolbar;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClienteProovider;

public class RegisterClienteActivity extends AppCompatActivity {

    AuthProovider authProovider;
    ClienteProovider clienteProovider;

    Button btnRegistrar;
    TextInputEditText textInputNombre;
    TextInputEditText textInputEmail;
    TextInputEditText textInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MyToolbar.show(this, "Registro de Cliente", true);

        authProovider = new AuthProovider();
        clienteProovider = new ClienteProovider();

        btnRegistrar = findViewById(R.id.btnRegistrar);

        textInputNombre = findViewById(R.id.textInputNombre);
        textInputEmail = findViewById(R.id.textInputEmail);
        textInputPassword = findViewById(R.id.textInputPassword);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegistrar();
            }
        });

    }

    void clickRegistrar() {
        String nombre = textInputNombre.getText().toString();
        String email = textInputEmail.getText().toString();
        String password = textInputPassword.getText().toString();

        if(!nombre.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                registrar(nombre, email, password);
            }else{
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void registrar(String nombre, String email, String password) {
        authProovider.registrar(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Cliente c = new Cliente(id, nombre, email);
                    crear(c);
                }else{
                    Toast.makeText(RegisterClienteActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void crear(Cliente cliente) {
        clienteProovider.crearCliente(cliente).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(RegisterClienteActivity.this, MapClienteActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterClienteActivity.this, "No se pudo registrar el cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    private void guardarUsuario(String id, String nombre, String email) {
        String usuario = pref.getString("usuario", "");
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setNombre(nombre);

        if(usuario.equals("conductor")){
            dataBase.child("Usuarios").child("Conductores").child(id).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            dataBase.child("Usuarios").child("Clientes").child(id).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    */
}
