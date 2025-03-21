/*
 * ActiviteDetailItineraire.java                                    10 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.ClientAdapter;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.PointGPS;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de détails d'un itinéraire.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class ActiviteDetailItineraire extends AppCompatActivity {
    private MapView mapView;
    private IMapController mapController;
    private EditText inputNomItineraire;
    private RecyclerView recyclerClientsDispo, recyclerClientsSelectionnes;
    private ClientAdapter adapterClientsDispo, adapterClientsSelectionnes;
    private List<Client> clientsDisponibles, clientsAjoutes;
    private Button boutonValider;
    private Itineraire itineraire;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_itineraire);

        // Initialisation des vues
        inputNomItineraire = findViewById(R.id.saisieNomItineraire);
        recyclerClientsDispo = findViewById(R.id.listeClient);
        recyclerClientsSelectionnes = findViewById(R.id.clientSelectionne);
        boutonValider = findViewById(R.id.boutonValider);

        Intent intention = getIntent();
        id = intention.getIntExtra("ID", 0);

        recyclerClientsDispo.setLayoutManager(new LinearLayoutManager(this));
        recyclerClientsSelectionnes.setLayoutManager(new LinearLayoutManager(this));

        // Récupération des listes
        clientsDisponibles = new ArrayList<>(SingletonListeClient.getInstance().getListeClient());
        clientsAjoutes = new ArrayList<>();

        initialiserChamps();

        // Initialisation des adaptateurs
        adapterClientsDispo = new ClientAdapter(clientsDisponibles, this::ajouterClient);
        adapterClientsSelectionnes = new ClientAdapter(clientsAjoutes, this::retirerClient);

        // Associer les adaptateurs aux RecyclerView
        recyclerClientsDispo.setAdapter(adapterClientsDispo);
        recyclerClientsSelectionnes.setAdapter(adapterClientsSelectionnes);

        findViewById(R.id.boutonRetour).setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        TextWatcher champModifieListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boutonValider.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        inputNomItineraire.addTextChangedListener(champModifieListener);
    }

    /**
     * Initialise les champs de la page avec les valeurs de l'itinéraire.
     */
    public void initialiserChamps() {
        itineraire = SingletonListeItineraire.getInstance().getItineraire(id);
        inputNomItineraire.setText(itineraire.getNom());
        clientsAjoutes = new ArrayList<>();
        for (Map.Entry<Long, String> entry : itineraire.getOrdreClients().entrySet()) {
            clientsAjoutes.add(SingletonListeClient.getInstance().getClient(entry.getKey()));
        }
        for (Client c : clientsAjoutes) {
            if (clientsDisponibles.contains(c)) {
                clientsDisponibles.remove(c);
            }
        }
    }

    private void ajouterClient(int position) {
        if (clientsAjoutes.size() >= 8) {
            SnackbarCustom.show(this, R.string.nombre_etape_depasse, SnackbarCustom.STYLE_ATTENTION);
        } else {
            Client client = clientsDisponibles.remove(position);
            clientsAjoutes.add(client);
            mettreAJourListes();
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
        boutonValider.setEnabled(true);
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
                    .put("x", Preferences.getLongitude(this)));
                for (Client c : clientsAjoutes) {
                    JSONObject point = new JSONObject();
                    point.put("x", c.getX());
                    point.put("y", c.getY());
                    listePoint.put(String.valueOf(c.getID()), point);
                }
                jsonFinal.put("nomItineraire", inputNomItineraire.getText().toString());
                jsonFinal.put("listePoint", listePoint);
            } catch (Exception e) {
                Log.e("Itineraire", "Erreur lors de la génération du JSON : " + e);
            }

            ClientApi.calculerItineraire(jsonFinal, this, this::afficherCarteAvecItineraire);
        }
    }

    /**
     * Afficher la carte avec l'itinéraire calculé.
     * @param points Les points de l'itinéraire
     */
    private void afficherCarteAvecItineraire(LinkedHashMap<Long, PointGPS> points) {
        runOnUiThread(() -> {
            ProgressDialog spineurChargement = new ProgressDialog(this);
            spineurChargement.setMessage(this.getString(R.string.chargement_calcul_itineraire));
            spineurChargement.setCancelable(false);
            spineurChargement.show();

            try {
                // Création et configuration de la MapView dans un conteneur
                LinearLayout mapContainer = new LinearLayout(this);

                mapView = new MapView(this);
                mapView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
                mapContainer.addView(mapView);

                // Configuration de base de la carte
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                mapView.setBuiltInZoomControls(true);
                mapView.setMultiTouchControls(true);
                Configuration.getInstance().setUserAgentValue(getPackageName());

                // Initialiser le contrôleur de carte
                mapController = mapView.getController();
                mapController.setZoom(14.0);

                // Extraction et centrage sur les points dans l'ordre défini
                ArrayList<PointGPS> waypoints = new ArrayList<>(points.values());
                if (!waypoints.isEmpty()) {
                    mapController.setCenter(waypoints.get(0));
                }

                // Création de la boîte de dialogue affichant la carte
                AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(inputNomItineraire.getText().toString().isEmpty()
                              ? "Itinéraire : voici l'itinéraire calculé pour votre tournée, voulez-vous valider la modification ?"
                              : String.format("Voici l'itinéraire calculé pour votre tournée \"%s\", voulez-vous valider la modification ?",
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

                // Calculer l'itinéraire avec ORS dans un thread séparé
                new Thread(() -> {
                    try {
                        String apiKey = ClientApi.API_ORS_TOKEN;
                        JSONObject jsonRequest = buildJsonRequest(waypoints);

                        URL url = new URL("https://api.openrouteservice.org/v2/directions/driving-car/geojson");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Authorization", apiKey);
                        conn.setRequestProperty("Content-Type", "application/json");

                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(jsonRequest.toString().getBytes("UTF-8"));
                        }

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            String response = readResponse(conn);
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray features = jsonResponse.getJSONArray("features");
                            if (features.length() > 0) {
                                // Construction de la polyline selon l’ordre fourni
                                Polyline routePolyline = buildRoutePolyline(features.getJSONObject(0));
                                routePolyline.setWidth(10);

                                // Récupérer la couleur et la convertir en string hexadécimale
                                int colorInt = ContextCompat.getColor(this, R.color.colorPrimaryDark);
                                String colorHex = String.format("#%06X", (0xFFFFFF & colorInt));
                                // Appliquer la couleur au Polyline
                                routePolyline.setColor(Color.parseColor(colorHex));

                                runOnUiThread(() -> {
                                    mapView.getOverlays().add(routePolyline);
                                    addMarkers(mapView, waypoints);
                                    spineurChargement.dismiss();
                                    mapView.invalidate();
                                });
                                return;
                            }
                        }
                        // En cas d'erreur ou de réponse vide : tracer des lignes "à vol d'oiseau"
                        runOnUiThread(() -> {
                            spineurChargement.dismiss();
                            drawBirdFlightLines(mapView, waypoints);
                            addMarkers(mapView, waypoints);
                            mapView.invalidate();
                            SnackbarCustom.show(this, "Erreur lors du calcul de l'itinéraire avec ORS", SnackbarCustom.STYLE_ERREUR);
                        });
                    } catch (Exception e) {
                        spineurChargement.dismiss();
                        e.printStackTrace();
                        runOnUiThread(() ->
                            SnackbarCustom.show(this, "Erreur lors du calcul de l'itinéraire : ", SnackbarCustom.STYLE_ERREUR));
                    }
                }).start();
            } catch (Exception e) {
                spineurChargement.dismiss();
                e.printStackTrace();
                SnackbarCustom.show(this, R.string.erreur_affichage_carte, SnackbarCustom.STYLE_ERREUR);
            }
        });
    }

    /**
     * Construit la requête JSON pour l'API OpenRouteService avec
     * les coordonnées des points de l'itinéraire.
     * @param waypoints Les points de l'itinéraire
     * @throws JSONException En cas d'erreur de construction JSON
     */
    private JSONObject buildJsonRequest(ArrayList<PointGPS> waypoints) throws JSONException {
        JSONObject jsonRequest = new JSONObject();
        JSONArray coordinatesArray = new JSONArray();
        for (PointGPS point : waypoints) {
            JSONArray coord = new JSONArray();
            coord.put(point.getLongitude());
            coord.put(point.getLatitude());
            coordinatesArray.put(coord);
        }
        jsonRequest.put("coordinates", coordinatesArray);
        jsonRequest.put("instructions", false);
        return jsonRequest;
    }

    /**
     * Lit la réponse de la requête HTTP.
     * @param conn La connexion HTTP
     * @return La réponse de la requête
     * @throws IOException En cas d'erreur de lecture de la réponse
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }

    /**
     * Construit une polyline à partir d'un objet JSON représentant une feature.
     * @param feature L'objet JSON représentant une feature
     * @return La polyline construite
     * @throws JSONException En cas d'erreur de lecture des coordonnées
     */
    private Polyline buildRoutePolyline(JSONObject feature) throws JSONException {
        Polyline polyline = new Polyline();
        JSONObject geometry = feature.getJSONObject("geometry");
        JSONArray coords = geometry.getJSONArray("coordinates");
        for (int i = 0; i < coords.length(); i++) {
            JSONArray coord = coords.getJSONArray(i);
            double lon = coord.getDouble(0);
            double lat = coord.getDouble(1);
            polyline.addPoint(new GeoPoint(lat, lon));
        }
        return polyline;
    }

    /**
     * Ajoute des marqueurs pour chaque point de l'itinéraire.
     * @param mapView La carte
     * @param waypoints Les points de l'itinéraire
     */
    private void addMarkers(MapView mapView, ArrayList<PointGPS> waypoints) {
        for (int i = 0; i < waypoints.size(); i++) {
            PointGPS point = waypoints.get(i);
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle((i + 1) + " - " + point.getNom());
            mapView.getOverlays().add(marker);
        }
    }

    /**
     * Dessine des lignes "à vol d'oiseau" entre chaque point de l'itinéraire.
     * @param mapView La carte
     * @param waypoints Les points de l'itinéraire
     */
    private void drawBirdFlightLines(MapView mapView, ArrayList<PointGPS> waypoints) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Polyline birdFlightLine = new Polyline();
            birdFlightLine.addPoint(waypoints.get(i));
            birdFlightLine.addPoint(waypoints.get(i + 1));
            birdFlightLine.setColor(Color.YELLOW);
            birdFlightLine.setWidth(5);
            mapView.getOverlays().add(birdFlightLine);
        }
    }

    /**
     * Créer l'itinéraire avec les points fournis.
     * @param listeEtape Les points de l'itinéraire
     */
    private void creationItineraire(LinkedHashMap<Long, PointGPS> listeEtape) {
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

            ClientApi.modifierItineraire(this, jsonFinal, itineraire.getID(), this::modificationValide);
        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarCustom.show(this, R.string.erreur_creation_itineraire, SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Action à effectuer après la modification de l'itinéraire.
     */
    private void modificationValide() {
        itineraire.setNom(inputNomItineraire.getText().toString());

        LinkedHashMap<Long, String> ordreClients = new LinkedHashMap<>();
        for (Client c : clientsAjoutes) {
            ordreClients.put(c.getID(), c.getEntreprise());
        }
        itineraire.setOrdreClients(ordreClients);

        setResult(AppCompatActivity.RESULT_OK);
        finish();
    }
}
