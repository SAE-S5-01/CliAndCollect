package fr.iutrodez.sae501.cliandcollect.activites;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;

public class ActiviteParcours extends AppCompatActivity {

    private static final int CODE_REQUETE = 101;
    private MapView map;
    private FusedLocationProviderClient clientDeLocalisation;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Polyline path;
    private List<GeoPoint> pathPoints;
    private IMapController mapController;

    private TextView prochaineDestination;


    private static final float DISTANCE_THRESHOLD = 15.0f; // Distance minimale en mètres
    private Location lastLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_parcours);

        prochaineDestination = findViewById(R.id.prochaineDestination);
        prochaineDestination.setText("Prochain client : " + "Nom de la destination");

        findViewById(R.id.boutonPause).setOnClickListener(v -> mettreEnPauseParcours());
        findViewById(R.id.boutonStop).setOnClickListener(v -> stopperParcours());

        // Initialisation d'OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Configuration de la carte
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Initialisation du marqueur utilisateur
        userMarker = new Marker(map);
        userMarker.setTitle("Ma position");
        userMarker.setIcon(getResources().getDrawable(R.drawable.ic_ma_position));
        map.getOverlays().add(userMarker);

        // Initialisation du tracé
        path = new Polyline();
        path.setWidth(8f);
        path.setColor(Color.BLUE);
        map.getOverlays().add(path);

        pathPoints = new ArrayList<>();

        // Contrôle de la carte
        mapController = map.getController();
        mapController.setZoom(15.0);
        map.setMinZoomLevel(5.0);
        map.setMaxZoomLevel(20.0);  
        // Client de localisation
        clientDeLocalisation = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, CODE_REQUETE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {   
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000) // Toutes les 5 secondes
            .setFastestInterval(2000); // Minimum 3 seconde entre 2 updates

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("loc" , String.valueOf(locationResult.getLastLocation().getLatitude()));
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        updateUserLocation(location);
                    }
                }
            }
        };

        clientDeLocalisation.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateUserLocation(Location location) {
        boolean updateValide = true;

        if (lastLocation != null) {
            float distance = location.distanceTo(lastLocation); // Distance entre l'ancienne et la nouvelle position
            updateValide = distance >= DISTANCE_THRESHOLD; // Met à jour seulement si la distance est suffisante
        }

        if (updateValide) {
            lastLocation = location;
            GeoPoint userPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
            pathPoints.add(userPosition);
            path.setPoints(pathPoints);

            // Mise à jour du marqueur utilisateur
            userMarker.setPosition(userPosition);
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Centrage sur la nouvelle position avec animation
            mapController.animateTo(userPosition);

            // Rafraîchir la carte
            map.invalidate();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (clientDeLocalisation != null) {
            clientDeLocalisation.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }

        if (path != null) {
            path.setPoints(pathPoints);
            map.invalidate(); // Redessiner la carte
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUETE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    private void mettreEnPauseParcours() {
        if (clientDeLocalisation != null) {
            clientDeLocalisation.removeLocationUpdates(locationCallback);
        }
    }

    private void stopperParcours() {
        if (clientDeLocalisation != null) {
            clientDeLocalisation.removeLocationUpdates(locationCallback);
        }
    }
}