package com.upn.chapanomas.activitys.cliente;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.upn.chapanomas.R;
import com.upn.chapanomas.clases.Cliente;
import com.upn.chapanomas.includes.MyToolbar;
import com.upn.chapanomas.providers.AuthProovider;
import com.upn.chapanomas.providers.ClienteProovider;
import com.upn.chapanomas.utils.CompressorBitmapImage;
import com.upn.chapanomas.utils.FileUtil;

import java.io.File;

public class UpdateProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private Button btnUpdate;
    private TextView textViewName;

    private ClienteProovider clientProovider;
    private AuthProovider authProovider;

    private String name;

    private File imageFile;
    private String image;

    ActivityResultLauncher<String> getContent;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        MyToolbar.show(this, "Actualizar perfil", true);

        imageViewProfile = findViewById(R.id.imageViewProfile);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        textViewName = findViewById(R.id.textInputNombre);

        imageViewProfile.setClickable(true);

        clientProovider = new ClienteProovider();
        authProovider = new AuthProovider();

        progressDialog = new ProgressDialog(this);

        getClientInfo();

       /* imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UpdateProfileActivity.this, "Probando si funciona", Toast.LENGTH_SHORT).show();
                openGallery();
            }
        });*/

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    private void onClickImage(View v){
        openGallery();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                imageFile = FileUtil.from(this, data.getData());
                imageViewProfile.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Mensaje: " + e.getMessage());
            }
        }
    }

    private void getClientInfo(){
        clientProovider.getClient(authProovider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("nombre").getValue().toString();
                    textViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
        name = textViewName.getText().toString();
        if(name.equals("") && imageFile != null){
            progressDialog.setMessage("Espere un momento...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            saveImage();
        }else{
            Toast.makeText(this, "Ingresa una imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        byte[] imageByte = CompressorBitmapImage.getImage(this, imageFile.getPath(), 500, 500);
        StorageReference storage = FirebaseStorage.getInstance().getReference().child("client_images").child(authProovider.getId() + ".jpg");
        UploadTask uploadTask = storage.putBytes(imageByte);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Cliente client = new Cliente();
                            client.setImage(image);
                            client.setNombre(name);
                            client.setId(authProovider.getId());
                            clientProovider.update(client).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UpdateProfileActivity.this, "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}