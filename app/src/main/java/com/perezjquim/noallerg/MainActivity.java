package com.perezjquim.noallerg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.perezjquim.noallerg.db.DatabaseManager;
import com.perezjquim.noallerg.util.Http;
import com.perezjquim.noallerg.util.PermissionChecker;
import com.perezjquim.noallerg.util.SharedPreferencesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import static com.perezjquim.noallerg.util.UI.toast;

public class MainActivity extends AppCompatActivity
{
    private MapView map;
    private IMapController mapController;
    private PermissionChecker permissionChecker;
    private SharedPreferencesHelper prefs;
    private static final double MAP_DEFAULT_LAT = 39;
    private static final double MAP_DEFAULT_LONG = -8;
    private static final double MAP_DEFAULT_ZOOM = 15.0;
    private static final double MAP_MIN_ZOOM = 4.0;
    private static final String PREFS_COORDS = "coords";
    private static final String PREFS_COORDS_LAT ="lat";
    private static final String PREFS_COORDS_LONG = "long";
    private static final String PREFS_COORDS_ZOOM = "zoom";
    private RequestQueue queue;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        initPermissionChecker();
        DatabaseManager.initDatabase();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        prefs = new SharedPreferencesHelper(this);
        setContentView(R.layout.activity_main);
        initMap();
        queue = Volley.newRequestQueue(this);
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
        mapController.stopPanning();
        loadPreviousCoordinates();
        loadPreviousMarkers();
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
        new Thread(()->
        {
            FusedLocationProviderClient location = LocationServices.getFusedLocationProviderClient(this);
            location.getLastLocation()
                    .addOnSuccessListener(this, coordinates ->
                    {
                        if (coordinates != null)
                        {
                            moveTo(coordinates.getLatitude(), coordinates.getLongitude(),MAP_DEFAULT_ZOOM);
                        }
                        else
                        {
                            toast(this, "An error occured (make sure GPS is enabled)");
                        }
                    });
        }).start();
    }

    public void refreshPoints(View v)
    {
        new Thread(()->
        {
            toast(this,"Refreshing data..");
            Http.doGetRequest("http://www.noallerg.x10host.com/markers.php/",
                    response ->
                    {
                        DatabaseManager.clearDatabase();
                        placeMarkers(response);
                        mapController.setZoom(map.getZoomLevelDouble());
                        toast(this,"Done refreshing!");
                    },
                    error ->
                    {
                        System.err.println(error.toString());
                        toast(this,"An error occured (make sure you have an internet connection)");
                    },
                    queue
            );
        }).start();
    }

    private void moveTo(double latitude, double longitude, double zoom)
    {
        mapController.setZoom(zoom);
        moveTo(latitude,longitude);
    }

    private void moveTo(double latitude, double longitude)
    {
        mapController.animateTo(new GeoPoint(latitude,longitude));
    }

    private void saveCoordinates()
    {
        prefs.setString(PREFS_COORDS,PREFS_COORDS_LAT,""+map.getMapCenter().getLatitude());
        prefs.setString(PREFS_COORDS,PREFS_COORDS_LONG,""+map.getMapCenter().getLongitude());
        prefs.setString(PREFS_COORDS,PREFS_COORDS_ZOOM,""+map.getZoomLevelDouble());
    }

    private void loadPreviousCoordinates()
    {
        String sLatitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LAT);
        String sLongitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LONG);
        String sZoom = prefs.getString(PREFS_COORDS,PREFS_COORDS_ZOOM);

        if(sLatitude != null && sLongitude != null && sZoom != null)
        {
            double dLatitude = Double.parseDouble(sLatitude);
            double dLongitude = Double.parseDouble(sLongitude);
            double dZoom = Double.parseDouble(sZoom);
            moveTo(dLatitude,dLongitude,dZoom);
        }
        else
        {
            moveTo(MAP_DEFAULT_LAT,MAP_DEFAULT_LONG,MAP_DEFAULT_ZOOM);
        }
    }

    private void loadPreviousMarkers()
    {
        ArrayList<OverlayItem> items = new ArrayList<>();
        Cursor markers = DatabaseManager.getMarkers();
        while(markers.moveToNext())
        {
            String title = markers.getString(0);
            String subtitle = markers.getString(1);
            double latitude = markers.getDouble(2);
            double longitude = markers.getDouble(3);

            items.add(new OverlayItem(title,subtitle,
                    new GeoPoint(latitude,longitude)));
        }
        ItemizedIconOverlay<OverlayItem> overlay
                = new ItemizedIconOverlay<>(
                this, items, null);
        map.getOverlays().add(overlay);
    }

    private void placeMarkers(JSONArray markers)
    {
        map.getOverlays().clear();
        ArrayList<OverlayItem> items = new ArrayList<>();

        for(int i = 0; i < markers.length(); i++)
        {
            try
            {
                JSONObject marker = markers.getJSONObject(i);

                String title = marker.getString("title");
                String subtitle = marker.getString("subtitle");
                double latitude = marker.getDouble("latitude");
                double longitude = marker.getDouble("longitude");

                DatabaseManager.insertMarker(title,subtitle,latitude,longitude);

                items.add(new OverlayItem(title,subtitle,
                        new GeoPoint(latitude,longitude)));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        ItemizedIconOverlay<OverlayItem> overlay
                = new ItemizedIconOverlay<>(
                this, items, null);
        map.getOverlays().add(overlay);
    }
}