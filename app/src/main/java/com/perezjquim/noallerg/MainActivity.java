package com.perezjquim.noallerg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.perezjquim.noallerg.util.PermissionChecker;
import com.perezjquim.noallerg.util.SharedPreferencesHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import static com.perezjquim.noallerg.util.UI.toast;

public class MainActivity extends AppCompatActivity
{
    private MapView map;
    private IMapController mapController;
    private PermissionChecker permissionChecker;
    private SharedPreferencesHelper prefs;
    private static final double MAP_DEFAULT_ZOOM = 10.0;
    private static final double MAP_MIN_ZOOM = 2.0;
    private static final String PREFS_COORDS = "coords";
    private static final String PREFS_COORDS_LAT ="lat";
    private static final String PREFS_COORDS_LONG = "long";

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        initPermissionChecker();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        prefs = new SharedPreferencesHelper(this);
        setContentView(R.layout.activity_main);
        initMap();
    }

    private void initPermissionChecker()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            permissionChecker = new PermissionChecker(this);
            permissionChecker.start();
        }
    }

    private void initMap()
    {
        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(MAP_MIN_ZOOM);
        mapController = map.getController();
        loadPreviousCoordinates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case PermissionChecker.REQUEST_CODE:
                permissionChecker.restart();
        }
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
        saveCoordinates();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        saveCoordinates();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        saveCoordinates();
    }

    @SuppressLint("MissingPermission")
    public void goCurrent(View v)
    {
        FusedLocationProviderClient location = LocationServices.getFusedLocationProviderClient(this);
        location.getLastLocation()
                .addOnSuccessListener(this, coordinates ->
                {
                    if (coordinates != null)
                    {
                        moveTo(coordinates.getLatitude(), coordinates.getLongitude());
                    }
                    else
                    {
                        toast(this, "An error ocurred (check if GPS is enabled).");
                    }
                });
    }

    public void refreshPoints(View v)
    {
        toast(this,"refresh");
    }

    private void moveTo(double latitude, double longitude)
    {
        mapController.setZoom(MAP_DEFAULT_ZOOM);
        mapController.animateTo(new GeoPoint(latitude,longitude));
    }

    private void saveCoordinates()
    {
        prefs.setString(PREFS_COORDS,PREFS_COORDS_LAT,""+map.getMapCenter().getLatitude());
        prefs.setString(PREFS_COORDS,PREFS_COORDS_LONG,""+map.getMapCenter().getLongitude());
    }

    private void loadPreviousCoordinates()
    {
        String sLatitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LAT);
        String sLongitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LONG);

        if(sLatitude != null && sLongitude != null)
        {
            double dLatitude = Double.parseDouble(sLatitude);
            double dLongitude = Double.parseDouble(sLongitude);
            moveTo(dLatitude,dLongitude);
        }
    }
}