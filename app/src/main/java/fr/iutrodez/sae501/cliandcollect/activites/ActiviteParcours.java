package fr.iutrodez.sae501.cliandcollect.activites;

import android.Manifest;
import android.content.Context;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
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
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.Parcours;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.SingletonListeParcours;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

public class ActiviteParcours extends AppCompatActivity {

    private static final int CODE_REQUETE = 101;
    private MapView map;
    private FusedLocationProviderClient clientDeLocalisation;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Polyline path;
    private List<GeoPoint> pathPoints;
    private IMapController mapController;

    private TextView nomProchainClient;
    private TextView prochaineDestination;
    private TextView numeroProchainClient;

    private Parcours parcoursCourant;

    private static final float DISTANCE_THRESHOLD = 15.0f; // Distance minimale en mètres
    private Location lastLocation = null;

    private Itineraire itineraireCourant = null;
    private Handler networkLocationHandler = new Handler(Looper.getMainLooper());
    private Runnable networkLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isNetworkAvailable()) {
                afficherAlerte("Perte de connexion réseau", "Veuillez vérifier votre connexion internet.");
            } else if (!isLocationEnabled()) {
                afficherAlerte("Localisation désactivée", "Veuillez activer la localisation.");
            }
            // Vérifier toutes les 5 secondes
            networkLocationHandler.postDelayed(this, 5000);
        }
    };

    private AlertDialog currentAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String itineraireId = getIntent().getStringExtra("SELECTED_ITINERAIRE_ID");
        Long idParcoursCourant = getIntent().getLongExtra("PARCOURS_ID", -1);

        parcoursCourant = SingletonListeParcours.getInstance().getParcours(idParcoursCourant);

        if (itineraireId != null && !itineraireId.isEmpty()) {
            // Charger l'itinéraire correspondant
            itineraireCourant = SingletonListeItineraire.getItineraire(itineraireId);
        }
        checkLocationPermission();
    }

    private void initialiserUI() {
        setContentView(R.layout.activite_parcours);

        nomProchainClient = findViewById(R.id.prochainClient);
        prochaineDestination = findViewById(R.id.prochaineDestination);
        numeroProchainClient = findViewById(R.id.numeroProchainClient);

        Client prochainClient = getProchainClient();
        nomProchainClient.setText(prochainClient.getEntreprise());
        prochaineDestination.setText(prochainClient.getAdresse());
        numeroProchainClient.setText("N° " + getNumeroClient(prochainClient) + "/"
                                     + itineraireCourant.getOrdreClients().size());

        findViewById(R.id.boutonPause).setOnClickListener(v ->
            SnackbarCustom.show(this, R.string.clic_long_pour_action, SnackbarCustom.STYLE_INFORMATION));
        findViewById(R.id.boutonPause).setOnLongClickListener(v -> mettreEnPauseParcours());

        findViewById(R.id.boutonPasser).setOnClickListener(v -> passerClient());

        findViewById(R.id.boutonStop).setOnClickListener(v ->
            SnackbarCustom.show(this, R.string.clic_long_pour_action, SnackbarCustom.STYLE_INFORMATION));
        findViewById(R.id.boutonStop).setOnLongClickListener(v -> modifierStatutParcours("ARRETE"));

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        userMarker = new Marker(map);
        userMarker.setTitle("Ma position");
        userMarker.setIcon(getResources().getDrawable(R.drawable.ic_ma_position));
        map.getOverlays().add(userMarker);

        path = new Polyline();
        path.setWidth(8f);
        path.setColor(Color.BLUE);
        map.getOverlays().add(path);

        pathPoints = new ArrayList<>();

        mapController = map.getController();
        mapController.setZoom(15.0);
        map.setMinZoomLevel(5.0);
        map.setMaxZoomLevel(20.0);

        if (itineraireCourant != null) {
            placerPoint(itineraireCourant);
        }
        clientDeLocalisation = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODE_REQUETE);
        } else {
            initialiserUI();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)
            .setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLastLocation() != null) {
                Location location = locationResult.getLastLocation();
                Log.d("loc", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                updateUserLocation(location);
            } else {
                if (!isNetworkAvailable()) {
                    afficherAlerte("Perte de connexion réseau", "Veuillez vérifier votre connexion internet.");
                } else if (!isLocationEnabled()) {
                    afficherAlerte("Localisation désactivée", "Veuillez activer la localisation.");
                }
            }
            }
        };

        clientDeLocalisation.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void afficherAlerte(String titre, String message) {
        if (currentAlertDialog != null && currentAlertDialog.isShowing()) {
            return;
        }
        currentAlertDialog = new AlertDialog.Builder(this)
            .setTitle(titre)
            .setMessage(message)
            .setCancelable(false) // pour forcer l'utilisateur à choisir une action
            .setPositiveButton("Ok", (dialog, which) -> {
                dialog.dismiss();
                currentAlertDialog = null;
            })
            .show();
    }


    private void updateUserLocation(Location location) {
        if (lastLocation != null && location.distanceTo(lastLocation) < DISTANCE_THRESHOLD) {
            return;
        }

        lastLocation = location;
        GeoPoint userPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
        pathPoints.add(userPosition);
        path.setPoints(pathPoints);

        userMarker.setPosition(userPosition);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapController.animateTo(userPosition);
        map.invalidate();
    }

    private boolean mettreEnPauseParcours() {
        modifierStatutParcours("EN_PAUSE");
        return true;
    }

    /**
     * Passe au prochain client.
     */
    private void passerClient() {
        JSONObject objetNouvellesDonnees = new JSONObject();

        Client nouveauClient = getProchainClient();
        parcoursCourant.setDernierContactVisite(nouveauClient);

        try {
            objetNouvellesDonnees.put("idDernierContactVisite",
                                      nouveauClient.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ClientApi.modifierParcours(this, objetNouvellesDonnees, parcoursCourant.getId(), () -> {
            Client clientSuivant = getProchainClient();

            if (clientSuivant != null) {
                nomProchainClient.setText(clientSuivant.getEntreprise());
                prochaineDestination.setText(clientSuivant.getAdresse());
                numeroProchainClient.setText("N° " + getNumeroClient(clientSuivant) + "/"
                                             + itineraireCourant.getOrdreClients().size());
                SnackbarCustom.show(this,
                                    "Nouvelle destination : "
                                    + clientSuivant.getAdresse(),
                                    SnackbarCustom.STYLE_INFORMATION);
            } else {
                SnackbarCustom.show(this, R.string.parcours_termine, SnackbarCustom.STYLE_INFORMATION);
                modifierStatutParcours("TERMINE");
            }
        });
    }

    /**
     * @return Le prochain client à visiter.
     */
    private Client getProchainClient() {
        Client prochainClient = null;
        boolean prochainClientTrouve = false;

        if (parcoursCourant.getDernierContactVisite() == null) {
            return SingletonListeClient.getInstance().getClient(itineraireCourant.getOrdreClients().keySet().iterator().next());
        }

        for (Long idClient : itineraireCourant.getOrdreClients().keySet()) {
            if (prochainClientTrouve) {
                prochainClient = SingletonListeClient.getInstance().getClient(idClient);
                break;
            }
            if (itineraireCourant.getOrdreClients().get(idClient)
                    .equals(parcoursCourant.getDernierContactVisite().getEntreprise())) {
                prochainClientTrouve = true;
            }
        }

        return prochainClient;
    }

    /**
     * @return Le numéro du client actuel parmi les autres.
     */
    private int getNumeroClient(Client client) {
        int numeroClient = 1;

        for (Long idClient : itineraireCourant.getOrdreClients().keySet()) {
            if (itineraireCourant.getOrdreClients().get(idClient)
                    .equals(client.getEntreprise())) {
                break;
            } else {
                numeroClient++;
            }
        }
        return numeroClient;
    }

    /**
     * Modifie le statut du parcours.
     * @param statut Le nouveau statut du parcours.
     * @return true si la modification a été effectuée, false sinon.
     */
    private boolean modifierStatutParcours(String statut) {
        if (clientDeLocalisation != null) {
            clientDeLocalisation.removeLocationUpdates(locationCallback);
        }

        JSONObject objetNouvellesDonnees = new JSONObject();
        try {
            objetNouvellesDonnees.put("statut", statut);

            ClientApi.modifierParcours(this, objetNouvellesDonnees, parcoursCourant.getId(), () -> {
                SingletonListeParcours.getInstance().recupererParcours(this, () -> {
                    setResult(Activity.RESULT_OK);
                    finish();
                });
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkLocationHandler.post(networkLocationRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkLocationHandler.removeCallbacks(networkLocationRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUETE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                initialiserUI();
            } else {
                // TODO meilleure feedback
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void placerPoint(Itineraire itineraire) {

        for (int i = 0 ; i < itineraire.getListeCoordonnees().size() ; i++) {

            GeoPoint point = new GeoPoint(itineraire.getListeCoordonnees().get(i)[1],
                    itineraire.getListeCoordonnees().get(i)[0]);

            // Création d'un marqueur pour chaque point
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(itineraire.getOrdreClients().get(itineraire.getOrdreClients().keySet().toArray()[i]));

            // Ajout du marqueur à la carte
            map.getOverlays().add(marker);
        }
        Marker marker = new Marker(map);
        GeoPoint domicile = new GeoPoint(Double.parseDouble(Preferences.getLatitude(this)),
            Double.parseDouble(Preferences.getLongitude(this)));
        marker.setPosition(domicile);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Domicile - Arrivée");

        map.getOverlays().add(marker);
        map.invalidate();
    }

}
