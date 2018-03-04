package com.perezjquim.noallerg;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity
{
    MapView mapView=null;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        //TODO check permissions
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
    }


    @Override
    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView!=null)
            mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (mapView!=null)
            mapView.onPause();
    }
}