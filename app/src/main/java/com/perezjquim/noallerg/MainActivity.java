package com.perezjquim.noallerg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.perezjquim.PermissionChecker;
import com.perezjquim.SharedPreferencesHelper;
import com.perezjquim.noallerg.db.DatabaseManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import static com.perezjquim.UIHelper.closeProgressDialog;
import static com.perezjquim.UIHelper.openProgressDialog;
import static com.perezjquim.UIHelper.toast;
import static com.perezjquim.noallerg.ToastMessages.GPS_ERROR;
import static com.perezjquim.noallerg.ToastMessages.GPS_INIT;
import static com.perezjquim.noallerg.ToastMessages.GPS_SUCCESS;
import static com.perezjquim.noallerg.ToastMessages.UPDATE_MARKERS_INIT;
import static com.perezjquim.noallerg.ToastMessages.UPDATE_MARKERS_SUCCESS;

public class MainActivity extends AppCompatActivity
{
    private MapView map;
    private IMapController mapController;
    private SharedPreferencesHelper prefs;

    private static final String GET_MARKERS_URL = "http://noallerg.herokuapp.com/markers";

    // Constantes (Mapa)
    private static final double MAP_DEFAULT_LAT = 39;
    private static final double MAP_DEFAULT_LONG = -8;
    private static final double MAP_DEFAULT_ZOOM = 15.0;
    private static final double MAP_MIN_ZOOM = 4.0;

    // Constantes (SharedPreferences)
    private static final String PREFS_COORDS = "coords";
    private static final String PREFS_COORDS_LAT ="lat";
    private static final String PREFS_COORDS_LONG = "long";
    private static final String PREFS_COORDS_ZOOM = "zoom";

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        PermissionChecker.init(this);
        DatabaseManager.initDatabase();
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        AndroidNetworking.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initMap();

        super.setTheme(R.style.Theme_AppCompat);
    }

    private void initMap()
    {
        // Inicialização do mapa e as suas configurações
        prefs = new SharedPreferencesHelper(this);     
        map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(MAP_MIN_ZOOM);
        mapController = map.getController();

        // O mapa é posicionado nas coordenadas da sessão anterior
        loadPreviousCoordinates();

        // Os marcadores são recarregados para o mapa
        loadPreviousMarkers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case PermissionChecker.REQUEST_CODE:
                PermissionChecker.restart();
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
        // As coordenadas são guardadas
        saveCoordinates();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // As coordenadas são guardadas        
        saveCoordinates();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        // As coordenadas são guardadas   
        saveCoordinates();
    }

    @SuppressLint("MissingPermission")
    public void goCurrent(View v)
    {
        new Thread(()->
        {
            openProgressDialog(this, GPS_INIT.message);

            // Busca a localização atual e move o mapa para tal
            FusedLocationProviderClient location = LocationServices.getFusedLocationProviderClient(this);
            location
                    .getLastLocation()
                    .addOnSuccessListener(this, coordinates ->
                    {
                        if (coordinates != null)
                        {
                            moveTo(coordinates.getLatitude(), coordinates.getLongitude(),MAP_DEFAULT_ZOOM);
                            closeProgressDialog();
                            toast(this,GPS_SUCCESS.message);
                        }
                        else
                        {
                            closeProgressDialog();
                            toast(this, GPS_ERROR.message);
                        }
                    });
            location
                    .getLastLocation()
                    .addOnFailureListener(this, coordinates ->
                    {
                        closeProgressDialog();
                        toast(this, GPS_ERROR.message);
                    });
        }).start();
    }

    public void refreshPoints(View v)
    {
        new Thread(()->
        {
            // toast(this,UPDATE_MARKERS_INIT.message);
            openProgressDialog(this,UPDATE_MARKERS_INIT.message);

            // Atualiza os marcadores
            String credentials = "perezjquim" + ":" + "1234";
            String eCredencials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            AndroidNetworking.get(GET_MARKERS_URL)
                    .addHeaders("Authorization", "Basic " + eCredencials)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @Override
                        public void onResponse(JSONArray response)
                        {
                            DatabaseManager.clearDatabase();
                            placeMarkers(response);
                            map.invalidate();
                            closeProgressDialog();
                            toast(getApplicationContext(),UPDATE_MARKERS_SUCCESS.message);
                        }
                        @Override
                        public void onError(ANError error)
                        {
                            closeProgressDialog();
                            toast(getApplicationContext(),error.getErrorDetail());
                        }
                    });
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
        // Busca as coordenadas armazenadas no SharedPreferences
        String sLatitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LAT);
        String sLongitude = prefs.getString(PREFS_COORDS,PREFS_COORDS_LONG);
        String sZoom = prefs.getString(PREFS_COORDS,PREFS_COORDS_ZOOM);

        // Caso existam coordenadas anteriores
        if(sLatitude != null && sLongitude != null && sZoom != null)
        {
            // O mapa é movido para tais coordenadas
            double dLatitude = Double.parseDouble(sLatitude);
            double dLongitude = Double.parseDouble(sLongitude);
            double dZoom = Double.parseDouble(sZoom);
            moveTo(dLatitude,dLongitude,dZoom);
        }

        // Caso não existam coordenadas anteriores
        // (ex.: a primeira vez em que a aplicação é executada)
        else
        {
            // O mapa é movido para umas coordenadas predefinidas
            moveTo(MAP_DEFAULT_LAT,MAP_DEFAULT_LONG,MAP_DEFAULT_ZOOM);
        }
    }

    private void loadPreviousMarkers()
    {
        // Busca os markers à base de dados (local)
        Cursor markers = DatabaseManager.getMarkers();

        // Percorre todos os markers
        while(markers.moveToNext())
        {
            String title = markers.getString(0);
            String subtitle = markers.getString(1);
            double latitude = markers.getDouble(2);
            double longitude = markers.getDouble(3);

            // Posiciona o marker no mapa
            placeMarker(title,subtitle,latitude,longitude);
        }
    }

    private void placeMarker(String title, String subtitle, double latitude, double longitude)
    {
        // É criado um marker
        Marker m = new Marker(map);

        // As propriedades são atribuídas ao marker
        m.setPosition(new GeoPoint(latitude,longitude));
        m.setTitle(title);
        m.setSubDescription(subtitle);

        // Posiciona o marker no mapa
        map.getOverlays().add(m);
    }

    private void placeMarkers(JSONArray markers)
    {
        // O mapa é limpo
        map.getOverlays().clear();

        // Percorre todos os markers
        for(int i = 0; i < markers.length(); i++)
        {
            try
            {
                // Obtém um marker
                JSONObject marker = markers.getJSONObject(i);

                // Obtém as propriedades desse marker
                String title = marker.getString("title");
                String subtitle = marker.getString("subtitle");
                double latitude = marker.getDouble("latitude");
                double longitude = marker.getDouble("longitude");

                // Insere o marker na base de dados (local)
                DatabaseManager.insertMarker(title,subtitle,latitude,longitude);

                // O marker é colocado no mapa
                placeMarker(title,subtitle,latitude,longitude);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}