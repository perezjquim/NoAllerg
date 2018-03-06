package com.perezjquim.noallerg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import static com.perezjquim.noallerg.util.UI.toast;

public class MainActivity extends AppCompatActivity
{
    private MapView map;
    private PermissionChecker permissionChecker;
    private static final double MAP_DEFAULT_ZOOM = 10.0;
    private static final double MAP_MIN_ZOOM = 2.0;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_main);

        initMap();
    }

    private void initMap()
    {
        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(MAP_MIN_ZOOM);
        map.getController().setZoom(MAP_DEFAULT_ZOOM);
        map.getController().animateTo(new GeoPoint(39.0,-8.0));
        goCurrent();
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
    public void onResume()
    {
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

    public void goCurrent(View v)
    {
        goCurrent();
    }

    @SuppressLint("MissingPermission")
    public void goCurrent()
    {
        FusedLocationProviderClient location = LocationServices.getFusedLocationProviderClient(this);
        location.getLastLocation()
                .addOnSuccessListener(this, coordinates ->
                {
                    if (coordinates != null)
                    {
                        map.getController().setZoom(10.0);
                        map.getController().animateTo(new GeoPoint(coordinates.getLatitude(),coordinates.getLongitude()));
                    }
                    else
                    { toast(this,"An error ocurred (check if GPS is enabled)."); }
                });
    }

    public void refreshPoints(View v)
    {
        refreshPoints();
    }

    public void refreshPoints()
    {
        toast(this,"refresh");
    }
}