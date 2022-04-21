package com.upn.chapanomas.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.upn.chapanomas.R;

public class MyToolbar {

    public static void show(AppCompatActivity activity, String title, boolean boton){
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(boton);
    }
}
