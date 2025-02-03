package fr.iutrodez.sae501.cliandcollect.activites;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;


public class ActiviteCreationItineraire extends AppCompatActivity {
    private MapView mapView;
    private IMapController mapController;
    private EditText inputNomItineraire;
    private RecyclerView recyclerClientsDispo, recyclerClientsSelectionnes;
    private ClientAdapter adapterClientsDispo, adapterClientsSelectionnes;
    private List<Client> clientsDisponibles, clientsAjoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajouter_itineraire);

        // Initialisation des vues
        inputNomItineraire = findViewById(R.id.nomItineraire);
        recyclerClientsDispo = findViewById(R.id.listeClient);
        recyclerClientsSelectionnes = findViewById(R.id.clientSelectionne);

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
    }

    private void ajouterClient(int position) {
        Client client = clientsDisponibles.remove(position);
        clientsAjoutes.add(client);
        mettreAJourListes();
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

    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        JSONObject jsonFinal = new JSONObject();
        JSONObject listePoint = new JSONObject();
        try {
            jsonFinal.put("domicile", new JSONObject()
                    .put("y", 42.7720709)
                    .put("x", 2.98383)
            );
            for (Client c : clientsAjoutes) {
                JSONObject point = new JSONObject();
                point.put("x", c.getX());
                point.put("y", c.getY());
                listePoint.put(String.valueOf(c.getID()), point);
            }
            jsonFinal.put("nom", inputNomItineraire.getText().toString());
            jsonFinal.put("listePoint", listePoint);

            Log.i("Itineraire", "JSON généré : " + jsonFinal.toString());
        } catch (Exception e) {
            Log.e("Itineraire", "Erreur lors de la génération du JSON : " + e);
        }

        if (Reseau.reseauDisponible(this, true)) {
            ClientApi.calculerItineraire(jsonFinal, this, this::afficherCarteAvecItineraire);
        }
    }

    private void afficherCarteAvecItineraire(LinkedHashMap<String , GeoPoint> points) {
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
                ArrayList<GeoPoint> waypoints = new ArrayList<>(points.values());
                ArrayList<String> nomMarkeur = new ArrayList<>(points.keySet());

                // Centrer la carte sur le premier point
                if (!waypoints.isEmpty()) {
                    mapController.setCenter(waypoints.get(0));
                }

                // Créer et afficher l'AlertDialog
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Itinéraire : Voici l'itinéraire calculé pour votre tournée , voulez vous le créer ? ")
                        .setView(mapContainer)
                        .setPositiveButton("Valider", (dialogInterface, which) -> {
                            if (mapView != null) {
                                mapView.onDetach();
                            }
                            creationClient(points);
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

                                // Affichage d'un message indiquant que la ligne à vol d'oiseau a été ajoutée
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
                                    marker.setTitle(waypoints.indexOf(point) + 1 + " - " + nomMarkeur.get(waypoints.indexOf(point)));
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

    private void creationClient(LinkedHashMap<String , GeoPoint> listeEtape) {
        listeEtape.remove("-1");
        listeEtape.remove("-2");
        JSONObject jsonFinal = new JSONObject();
        try {
            jsonFinal.put("nomItineraire", inputNomItineraire.getText().toString());
            JSONObject ordreClients = new JSONObject();

            // Parcourir la LinkedHashMap pour ajouter chaque point
            for (Map.Entry<String, GeoPoint> entry : listeEtape.entrySet()) {
                String clientKey = entry.getKey();
                GeoPoint point = entry.getValue();

                // Créer un objet JSON pour chaque point avec ses coordonnées
                JSONObject clientPoint = new JSONObject();
                clientPoint.put("x", point.getLongitude()); // Longitude
                clientPoint.put("y", point.getLatitude());  // Latitude

                // Ajouter ce point dans "ordreClients" avec la clé client
                ordreClients.put(clientKey, clientPoint);
            }

            // Ajouter "ordreClients" à l'objet final
            jsonFinal.put("ordreClients", ordreClients);

            ClientApi.creationItineraire(this, jsonFinal, this::retour);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.erreur_creation_itineraire,
                    Toast.LENGTH_SHORT).show();
        }
        ClientApi.creationItineraire(this, jsonFinal, this::retour);
    }

    private void retour() {
        //setResult(AppCompatActivity.RESULT_OK);
        //finish();
        Log.i("Itineraire", "Itinéraire créé avec succès !");
    }
}
