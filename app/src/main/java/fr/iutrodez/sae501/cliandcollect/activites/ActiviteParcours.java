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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
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
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.Parcours;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.SingletonListeParcours;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Compte;
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

    private TextView texteProchainContact;
    private TextView nomProchainClient;
    private TextView prochaineDestination;
    private TextView numeroProchainClient;

    private Parcours parcoursCourant;

    private ArrayList<Client> notificationsDejaEmises = new ArrayList<>();

    private static final float DISTANCE_THRESHOLD = 10.0f; // Distance minimale en mètres

    private Location lastLocation;

    private Itineraire itineraireCourant;
    private HandlerThread handlerThread;
    private Handler networkLocationHandler;

    private AlertDialog notificationProche;
    private AlertDialog currentAlertDialog;

    private final Runnable networkLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isLocalisationDisponible()) {
                runOnUiThread(() -> afficherAlerte("Localisation désactivée", "Veuillez activer la localisation."));
            } else if (!isReseauDisponible()) {
                runOnUiThread(() -> afficherAlerte("Perte de connexion réseau", "Veuillez vérifier votre connexion internet."));
            }

            // Replanifier la vérification après 5 secondes
            networkLocationHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Long idParcoursCourant = getIntent().getLongExtra("PARCOURS_ID", -1);

        parcoursCourant = SingletonListeParcours.getInstance().getParcours(idParcoursCourant);
        pathPoints = parcoursCourant.getPositionsGpsPrecedentes() != null
                     ? parcoursCourant.getPositionsGpsPrecedentes()
                     : new ArrayList<>();
        itineraireCourant = parcoursCourant.getItineraire();

        // Initialiser et démarrer le HandlerThread
        handlerThread = new HandlerThread("NetworkLocationThread");
        handlerThread.start();

        // Initialiser le Handler avec le Looper du HandlerThread
        networkLocationHandler = new Handler(handlerThread.getLooper());

        // Démarrer la vérification d'arrière-plan
        networkLocationHandler.post(networkLocationRunnable);

        if (parcoursCourant.getStatut().equals("EN_PAUSE")) {
            modifierStatutParcours("EN_COURS");
            parcoursCourant.setStatut("EN_COURS");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODE_REQUETE);
        } else {
            initialiserUI();
        }
    }

    /**
     * Initialise l'interface utilisateur.
     */
    private void initialiserUI() {
        setContentView(R.layout.activite_parcours);

        texteProchainContact = findViewById(R.id.texteProchainContact);
        nomProchainClient = findViewById(R.id.prochainClient);
        prochaineDestination = findViewById(R.id.prochaineDestination);
        numeroProchainClient = findViewById(R.id.numeroProchainClient);

        if (!parcoursCourant.getStatut().equals("EN_COURS")) {
            Client dernierContactVisite = parcoursCourant.getDernierContactVisite();
            if (dernierContactVisite != null) {
                texteProchainContact.setText(dernierContactVisite.isProspect() ? R.string.dernier_prospect : R.string.dernier_client);
                nomProchainClient.setText(dernierContactVisite.getEntreprise());
                prochaineDestination.setText(dernierContactVisite.getAdresse());
                numeroProchainClient.setText("N° " + getNumeroClient(dernierContactVisite) + "/"
                                             + itineraireCourant.getOrdreClients().size());
            } else {
                texteProchainContact.setText(R.string.aucune_visite_effectuee);
                nomProchainClient.setText("");
                prochaineDestination.setText("");
                numeroProchainClient.setText(itineraireCourant.getOrdreClients().size()
                                             + " étape(s)");
            }
        } else {
            Client prochainClient = getProchainClient();
            texteProchainContact.setText(prochainClient.isProspect() ? R.string.prochain_prospect : R.string.prochain_client);
            nomProchainClient.setText(prochainClient.getEntreprise());
            prochaineDestination.setText(prochainClient.getAdresse());
            numeroProchainClient.setText("N° " + getNumeroClient(prochainClient) + "/"
                                         + itineraireCourant.getOrdreClients().size());
        }

        findViewById(R.id.boutonPause).setOnClickListener(v ->
            SnackbarCustom.show(this, R.string.clic_long_pour_action, SnackbarCustom.STYLE_INFORMATION));
        findViewById(R.id.boutonPause).setOnLongClickListener(v -> {
            if (isParcoursEnCours()) modifierStatutParcours("EN_PAUSE");
            else afficherErreurParcoursPasEnCours();
            return true;
        });

        findViewById(R.id.boutonPasser).setOnClickListener(v -> {
            if (isParcoursEnCours()) passerClient();
            else afficherErreurParcoursPasEnCours();
        });

        findViewById(R.id.boutonStop).setOnClickListener(v ->
            SnackbarCustom.show(this, R.string.clic_long_pour_action, SnackbarCustom.STYLE_INFORMATION));
        findViewById(R.id.boutonStop).setOnLongClickListener(v -> {
            if (isParcoursEnCours()) modifierStatutParcours("ARRETE");
            else afficherErreurParcoursPasEnCours();
            return true;
        });

        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(4.0);
        map.setMaxZoomLevel(20.0);

        path = new Polyline();
        path.setWidth(8f);
        // Récupérer la couleur et la convertir en string hexadécimale
        int colorInt = ContextCompat.getColor(this, R.color.colorPrimary);
        String colorHex = String.format("#%06X", (0xFFFFFF & colorInt));
        // Appliquer la couleur au Polyline
        path.setColor(Color.parseColor(colorHex));

        map.getOverlays().add(path);

        mapController = map.getController();
        mapController.setZoom(8);
        mapController.setCenter(new GeoPoint(
            Double.parseDouble(Preferences.getLatitude(this)),
            Double.parseDouble(Preferences.getLongitude(this))));

        if (itineraireCourant != null) {
            placerPoint(itineraireCourant);
        }
        if (parcoursCourant.getStatut().equals("EN_COURS")) {
            userMarker = new Marker(map);
            userMarker.setTitle("Ma position");
            userMarker.setIcon(getResources().getDrawable(R.drawable.ic_ma_position));
            map.getOverlays().add(userMarker);

            clientDeLocalisation = LocationServices.getFusedLocationProviderClient(this);
            demarrerSuiviLocalisations();
        }
    }

    /**
     * Démarre le suivi des localisations de l'utilisateur.
     */
    private void demarrerSuiviLocalisations() {
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
                    mettreAJourLocalisation(location);
                }
            }
        };

        clientDeLocalisation.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Vérifie si le réseau est disponible.
     * @return true si le réseau est disponible, false sinon.
     */
    private boolean isReseauDisponible() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Vérifie si la localisation est disponible.
     * @return true si la localisation est disponible, false sinon.
     */
    private boolean isLocalisationDisponible() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
               || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Affiche une alerte à l'utilisateur.
     * @param titre Le titre de l'alerte
     * @param message Le message de l'alerte
     */
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


    /**
     * Met à jour la position de l'utilisateur sur la carte.
     * @param location
     */
    private void mettreAJourLocalisation(Location location) {
        if (lastLocation == null) {
            mapController.setZoom(20);
        } else if (location.distanceTo(lastLocation) < DISTANCE_THRESHOLD) {
            return;
        }

        Long idClient = getProchainClient().getID();
        Double longitude = location.getLongitude();
        Double latitude = location.getLatitude();
        ClientApi.getProche(this, idClient, longitude, latitude, contacts -> {
            runOnUiThread(() -> afficherContactsProches(contacts));
        });

        lastLocation = location;
        GeoPoint userPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
        pathPoints.add(userPosition);
        path.setPoints(pathPoints);

        userMarker.setPosition(userPosition);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapController.animateTo(userPosition);
        map.invalidate();
    }

    /** @return true si le parcours est en cours, false sinon. */
    private boolean isParcoursEnCours() {
        return parcoursCourant.getStatut().equals("EN_COURS");
    }

    /**
     * Affiche une erreur indiquant que le parcours n'est pas en cours.
     */
    private void afficherErreurParcoursPasEnCours() {
        SnackbarCustom.show(this, R.string.erreur_parcours_pas_en_cours, SnackbarCustom.STYLE_ERREUR);
    }

    /**
     * Passe au prochain client.
     */
    private void passerClient() {
        if (!isParcoursEnCours()) {
            return;
        }

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
                texteProchainContact.setText(clientSuivant.isProspect() ? R.string.prochain_prospect : R.string.prochain_client);
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
     * @param statut Le nouveau statut du parcours
     * @return true si la modification a été effectuée, false sinon.
     */
    private boolean modifierStatutParcours(String statut) {
        if (clientDeLocalisation != null) {
            clientDeLocalisation.removeLocationUpdates(locationCallback);
        }

        JSONObject objetNouvellesDonnees = new JSONObject();
        try {
            objetNouvellesDonnees.put("statut", statut);
            objetNouvellesDonnees.put("positionsGpsPrecedentes", convertirPointsEnJson(pathPoints));

            ClientApi.modifierParcours(this, objetNouvellesDonnees, parcoursCourant.getId(), () -> {
                SingletonListeParcours.getInstance().recupererParcours(this, () -> {
                    if (!statut.equals("EN_COURS")) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
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

    /**
     * Méthode appelée lorsque l'utilisateur a répondu à une demande de permission.
     * @param requestCode Le code de la demande de permission.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUETE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                initialiserUI();
            } else {
                SnackbarCustom.show(this, R.string.permission_localisation_refusee, SnackbarCustom.STYLE_ERREUR);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }, 3500);
            }
        }
    }

    private void placerPoint(Itineraire itineraire) {

        for (int i = 0 ; i < itineraire.getListeCoordonnees().size() ; i++) {
            GeoPoint point = new GeoPoint(itineraire.getListeCoordonnees().get(i)[1],
                                          itineraire.getListeCoordonnees().get(i)[0]);

            Long idClient = (Long) itineraire.getOrdreClients().keySet().toArray()[i];
            Client client = SingletonListeClient.getInstance().getClient(idClient);

            // Création d'un marqueur pour chaque point
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle((i+1) + " - " + client.getEntreprise());
            marker.setSubDescription(client.getAdresse());

            // Choix de l'icône en fonction du type de contact
            if (client.isProspect()) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_point_prospect));
            } else {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_point_client));
            }

            // Ajout du marqueur à la carte
            map.getOverlays().add(marker);
        }

        Marker marker = new Marker(map);
        GeoPoint domicile = new GeoPoint(Double.parseDouble(Preferences.getLatitude(this)),
                                         Double.parseDouble(Preferences.getLongitude(this)));
        marker.setPosition(domicile);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Domicile - Arrivée");
        marker.setSubDescription(Compte.getInstance().getAdresse());
        marker.setIcon(getResources().getDrawable(R.drawable.ic_point_maison));

        map.getOverlays().add(marker);
        map.invalidate();
    }

    /**
     * Convertit un tableau de points en un objet JSON.
     * @param points Les points à convertir
     * @return Un objet JSON contenant les points
     */
    public static JSONArray convertirPointsEnJson(List<GeoPoint> points) {
        JSONArray jsonArray = new JSONArray();

        for (GeoPoint point : points) {
            if (point != null) {    
                JSONObject jsonPoint = new JSONObject();
                try {
                    jsonPoint.put("latitude", point.getLatitude());
                    jsonPoint.put("longitude", point.getLongitude());
                    jsonArray.put(jsonPoint);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonArray;
    }

    /**
     * Affiche une notification pour les contacts proches.
     * @param proches La liste des contacts proches
     */
    private void afficherContactsProches(ArrayList<Client> proches) {
        ArrayList<Client> procheNonNotifie = new ArrayList<>();

        for (Client proche : proches) {
            if (!notificationsDejaEmises.contains(proche)) {
                notificationsDejaEmises.add(proche);
                if (!proche.isProspect() || !parcoursCourant.contientClient(proche)) {
                    procheNonNotifie.add(proche);
                }
            }
        }


        if (!procheNonNotifie.isEmpty()) {
            // Créer une liste de noms à afficher
            String[] nomsClients = new String[procheNonNotifie.size()];
            for (int i = 0; i < procheNonNotifie.size(); i++) {
                Client c = procheNonNotifie.get(i);
                nomsClients[i] = (c.isProspect() ? "Prospect" : "Prochain client")
                    + " : " + c.getEntreprise() + " (" + c.getAdresse() + ")";
            }

            // Construire l'AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Contact(s) proche(s) détecté(s)")
                .setItems(nomsClients, (dialog, which) -> {
                    // Gérer le clic sur un client dans la liste si nécessaire
                })
                .setPositiveButton("Fermer", (dialog, which) -> dialog.dismiss());

            // Créer et afficher la boîte de dialogue
            notificationProche = builder.create();
            notificationProche.show();

            // Ajouter une vibration à l’ouverture du popup
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)); // Vibration 300ms
            }

            // Fermer automatiquement après 15 secondes
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (notificationProche != null && notificationProche.isShowing()) {
                    notificationProche.dismiss();
                }
            }, 15000); // 15 secondes
        }
    }
}
