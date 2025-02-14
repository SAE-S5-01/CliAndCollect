/*
 * ActiviteCreationItineraire.java                                  10 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.ClientAdapter;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.PointGPS;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de création d'un itinéraire.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class ActiviteCreationItineraire extends AppCompatActivity {
    private MapView mapView;
    private IMapController mapController;
    private EditText inputNomItineraire;
    private RecyclerView recyclerClientsDispo, recyclerClientsSelectionnes;
    private ClientAdapter adapterClientsDispo, adapterClientsSelectionnes;
    private List<Client> clientsDisponibles, clientsAjoutes;
    private Button boutonValider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_itineraire);

        ((TextView) findViewById(R.id.titreDetailItineraire)).setText(R.string.ajouter_itineraire);

        // Initialisation des vues
        inputNomItineraire = findViewById(R.id.saisieNomItineraire);
        recyclerClientsDispo = findViewById(R.id.listeClient);
        recyclerClientsSelectionnes = findViewById(R.id.clientSelectionne);
        boutonValider = findViewById(R.id.boutonValider);

        recyclerClientsDispo.setLayoutManager(new LinearLayoutManager(this));
        recyclerClientsSelectionnes.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des listes
        clientsDisponibles = new ArrayList<>(SingletonListeClient.getInstance().getListeClient());
        clientsAjoutes = new ArrayList<>();

        // Initialisation des adaptateurs
        adapterClientsDispo = new ClientAdapter(clientsDisponibles, this::ajouterClient);
        adapterClientsSelectionnes = new ClientAdapter(clientsAjoutes, this::retirerClient);

        // Associer les adaptateurs aux RecyclerView
        recyclerClientsDispo.setAdapter(adapterClientsDispo);
        recyclerClientsSelectionnes.setAdapter(adapterClientsSelectionnes);

        findViewById(R.id.boutonRetour).setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        if (clientsDisponibles.isEmpty()) {
            SnackbarCustom.show(this, R.string.erreur_aucun_client, SnackbarCustom.STYLE_ERREUR);
            actionRetardee(() -> retour(null));
        }
    }

    private void ajouterClient(int position) {
        if (clientsAjoutes.size() >= 8) {
            SnackbarCustom.show(this, R.string.nombre_etape_depasse, SnackbarCustom.STYLE_ATTENTION);
        } else {
            Client client = clientsDisponibles.remove(position);
            clientsAjoutes.add(client);
            mettreAJourListes();
            boutonValider.setEnabled(true);
        }
    }

    private void retirerClient(int position) {
        Client client = clientsAjoutes.remove(position);
        clientsDisponibles.add(client);
        mettreAJourListes();
    }

    private void mettreAJourListes() {
        adapterClientsDispo.notifyDataSetChanged();
        adapterClientsSelectionnes.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    /**
     * Clic sur le bouton "Retour".
     * @param view Le bouton "Retour"
     */
    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    /**
     * Clic sur le bouton "Valider".
     * @param view Le bouton "Valider"
     */
    public void valider(View view) {
        if (inputNomItineraire.getText().toString().isEmpty()) {
            inputNomItineraire.setError(getString(R.string.erreur_nom_itineraire_non_renseigne));
        } else if (clientsAjoutes.isEmpty()) {
            SnackbarCustom.show(this, R.string.erreur_ajout_client_vide, SnackbarCustom.STYLE_ERREUR);
        } else if (Reseau.reseauDisponible(this, true)) {
            JSONObject jsonFinal = new JSONObject();
            JSONObject listePoint = new JSONObject();
            try {
                jsonFinal.put("domicile", new JSONObject()
                    .put("y", Preferences.getLatitude(this))
                    .put("x", Preferences.getLongitude(this))
            );
                for (Client c : clientsAjoutes) {
                    JSONObject point = new JSONObject();
                    point.put("x", c.getX());
                    point.put("y", c.getY());
                    listePoint.put(String.valueOf(c.getID()), point);
                }
                jsonFinal.put("nom", inputNomItineraire.getText().toString());
                jsonFinal.put("listePoint", listePoint);
            } catch (Exception e) {
                Log.e("Itineraire", "Erreur lors de la génération du JSON : " + e);
            }

            ClientApi.calculerItineraire(jsonFinal, this, this::afficherCarteAvecItineraire);
        }
    }

    private void afficherCarteAvecItineraire(LinkedHashMap<Long , PointGPS> points) {
        runOnUiThread(() -> {
            try {
                // Créer un conteneur pour la MapView
                LinearLayout mapContainer = new LinearLayout(this);
                mapContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(400)
                ));

                // Initialiser la MapView
                mapView = new MapView(this);
                mapView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
                mapContainer.addView(mapView);

                // Configuration de base de la carte
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                mapView.setBuiltInZoomControls(true);
                mapView.setMultiTouchControls(true);
                Configuration.getInstance().setUserAgentValue(getPackageName());

                // Initialiser le contrôleur de carte
                mapController = mapView.getController();
                mapController.setZoom(14.0);

                // Créer la liste des points
                ArrayList<PointGPS> waypoints = new ArrayList<>(points.values());

                // Centrer la carte sur le premier point
                if (!waypoints.isEmpty()) {
                    mapController.setCenter(waypoints.get(0));
                }

                // Créer et afficher l'AlertDialog
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(inputNomItineraire.getText().toString().isEmpty()
                                ? "Itinéraire : voici l'itinéraire calculé pour votre tournée, voulez-vous le créer ?"
                                : String.format("%s : Voici l'itinéraire calculé pour votre tournée, voulez-vous le créer ?",
                                inputNomItineraire.getText().toString()))
                        .setView(mapContainer)
                        .setPositiveButton("Valider", (dialogInterface, which) -> {
                            if (mapView != null) {
                                mapView.onDetach();
                            }
                            creationItineraire(points);
                        })
                        .setNegativeButton("Annuler", null)
                        .create();

                dialog.setOnDismissListener(dialogInterface -> {
                    if (mapView != null) {
                        mapView.onDetach();
                    }
                });

                dialog.show();

                // Calculer l'itinéraire par segments dans un thread séparé
                new Thread(() -> {
                    try {
                        // Initialiser le RoadManager avec le nouveau service
                        RoadManager roadManager = new OSRMRoadManager(this, getPackageName());
                        ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_CAR);

                        List<Polyline> routes = new ArrayList<>();

                        // Calculer route par segments de 2 points
                        for (int i = 0; i < waypoints.size() - 1; i++) {
                            ArrayList<GeoPoint> segment = new ArrayList<>();
                            segment.add(waypoints.get(i));
                            segment.add(waypoints.get(i + 1));

                            // Créer l'URL pour ce segment avec les coordonnées des deux points
                            String url = String.format("https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?alternatives=false&overview=full&steps=true",
                                    segment.get(0).getLongitude(), segment.get(0).getLatitude(),
                                    segment.get(1).getLongitude(), segment.get(1).getLatitude());

                            Log.d("OSRM", "Request URL: " + url);
                            // Envoie de la requete http
                            Road road = roadManager.getRoad(segment);

                            if (road.mStatus == Road.STATUS_OK) {
                                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                                routes.add(roadOverlay);
                            } else {

                                // Calculer et ajouter une ligne "à vol d'oiseau" (ligne droite)
                                GeoPoint start = segment.get(0); // Premier point du segment
                                GeoPoint end = segment.get(1);   // Deuxième point du segment

                                // Créer une polyline pour la ligne droite
                                Polyline birdFlightLine = new Polyline();
                                birdFlightLine.addPoint(start); // Ajouter le premier point
                                birdFlightLine.addPoint(end);   // Ajouter le deuxième point

                                // Définir la couleur et la largeur de la ligne "à vol d'oiseau"
                                birdFlightLine.setColor(Color.RED);  // Par exemple, en rouge
                                birdFlightLine.setWidth(5);          // Largeur de la ligne

                                // Ajouter la ligne à vol d'oiseau aux overlays de la carte
                                mapView.getOverlays().add(birdFlightLine);
                            }
                        }

                        // Afficher les routes sur l'UI thread
                        runOnUiThread(() -> {
                            if (!routes.isEmpty()) {
                                // Ajouter toutes les routes
                                for (Polyline route : routes) {
                                    route.setColor(Color.BLUE);
                                    route.setWidth(10);
                                    mapView.getOverlays().add(route);
                                }

                                // Ajouter les marqueurs pour chaque point
                                for (GeoPoint point : waypoints) {
                                    Marker marker = new Marker(mapView);
                                    marker.setTitle(waypoints.indexOf(point) + 1 + " - " + waypoints.get(waypoints.indexOf(point)).getNom());
                                    marker.setPosition(point);
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                    mapView.getOverlays().add(marker);
                                }

                                mapView.invalidate();
                            } else {
                                Toast.makeText(ActiviteCreationItineraire.this,
                                        "Erreur lors du calcul de l'itinéraire ici",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(ActiviteCreationItineraire.this,
                                    "Erreur lors du calcul de l'itinéraire: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de l'affichage de la carte",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode utilitaire pour convertir dp en pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void creationItineraire(LinkedHashMap<Long , PointGPS> listeEtape) {
        PointGPS domicile = listeEtape.get(-1L);
        listeEtape.remove(-1L); // Supprimer le point de départ
        listeEtape.remove(-2L); // Supprimer le point d'arrivée
        JSONObject jsonFinal = new JSONObject();
        try {
            jsonFinal.put("nomItineraire", inputNomItineraire.getText().toString());
            JSONObject ordreClients = new JSONObject();

            jsonFinal.put("domicile", new JSONObject()
                    .put("x", domicile.getLongitude())
                    .put("y", domicile.getLatitude()));
            // Parcourir la LinkedHashMap pour ajouter chaque point
            for (Map.Entry<Long, PointGPS> entry : listeEtape.entrySet()) {
                Long clientKey = entry.getKey();
                GeoPoint point = entry.getValue();

                // Créer un objet JSON pour chaque point avec ses coordonnées
                JSONObject clientPoint = new JSONObject();
                clientPoint.put("x", point.getLongitude()); // Longitude
                clientPoint.put("y", point.getLatitude());  // Latitude

                // Ajouter ce point dans "ordreClients" avec la clé client
                ordreClients.put(String.valueOf(clientKey), clientPoint);
            }

            // Ajouter "ordreClients" à l'objet final
            jsonFinal.put("listePoint", ordreClients);

            ClientApi.creationItineraire(this, jsonFinal, this::creationOk);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.erreur_creation_itineraire,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void creationOk() {
        setResult(AppCompatActivity.RESULT_OK);
        finish();
    }

    /**
     * Attendre 10 secondes puis effectuer l'action passée en paramètre
     *
     * @param action Action à effectuer
     */
    private void actionRetardee(Runnable action) {
        new android.os.Handler().postDelayed(() -> action.run(), 3000);
    }
}
