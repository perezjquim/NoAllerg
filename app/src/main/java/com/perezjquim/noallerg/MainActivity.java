package com.perezjquim.noallerg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {
    private MapView map = null;
    private PermissionChecker permissionChecker;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(2.0);
        map.getController().setZoom(5.0);
        map.getController().animateTo(50,10);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 23)
        {
            permissionChecker = new PermissionChecker(this);
            permissionChecker.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionChecker.start();
    }


    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null)
            map.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Configuration.getInstance().save(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        if (map != null)
            map.onPause();
    }

    @SuppressLint("MissingPermission")
    public void goCurrent(View v)
    {
            FusedLocationProviderClient location = LocationServices.getFusedLocationProviderClient(this);
            location.getLastLocation()
                    .addOnSuccessListener(this, coordinates ->
                    {
                        if (coordinates != null)
                        { map.getController().animateTo((int) coordinates.getLatitude(),(int) coordinates.getLatitude()); }
                    });
    }
}