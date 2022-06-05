package com.upn.chapanomas.activitys.cliente;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.upn.chapanomas.R;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView animation;
    private TextView textBuscando;
    private Button btnCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        animation = findViewById(R.id.animation);
        textBuscando = findViewById(R.id.textBuscando);
        btnCancelRequest = findViewById(R.id.btnCancelRequest);

        animation.playAnimation();
    }
}