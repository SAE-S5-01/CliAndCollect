/*
 * ActiviteMap.java                                                 30 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Recherche une adresse et la positionne sur une carte.
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class ActiviteMap extends AppCompatActivity {

    private SearchView vueRecherche;

    private MapView carte;

    private Button boutonRechercher;

    private Button boutonValider;

    private Marker marqueur;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context contexte = getApplicationContext();

        // configure le User Agent pour ne pas être banni des serveurs OpenStreetMap
        Configuration.getInstance().load(contexte, PreferenceManager.getDefaultSharedPreferences(contexte));

        setContentView(R.layout.carte);

        carte = findViewById(R.id.map);
        carte.setTileSource(TileSourceFactory.MAPNIK);
        carte.setMultiTouchControls(true);
        carte.setBuiltInZoomControls(true);
        IMapController mapController = carte.getController();
        mapController.setZoom(15.5);

        // TODO centrer sur géolocalisation
        GeoPoint startPoint = new GeoPoint(44.3511408, 2.5728493);
        mapController.setCenter(startPoint);

        vueRecherche = findViewById(R.id.search_view);
        boutonRechercher = findViewById(R.id.button_search);
        boutonRechercher.setOnClickListener(this::rechercherAdresse);

        boutonValider = findViewById(R.id.button_validate);
        boutonValider.setOnClickListener(this::validerAdresse);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    private void rechercherAdresse(View v) {
        String adresseRecherchee = vueRecherche.getQuery().toString();

        // Crée un objet Geocoder pour convertir l'adresse en coordonnées GPS
        Geocoder geocoder = new Geocoder(this , Locale.getDefault());

        if (!adresseRecherchee.isEmpty()) {
            try {
                // Récupère une liste de résultat (1 ici)
                List<Address> addresses = geocoder.getFromLocationName(adresseRecherchee, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address adresseTrouvee = addresses.get(0);
                    GeoPoint point = new GeoPoint(adresseTrouvee.getLatitude(), adresseTrouvee.getLongitude());

                    adresseRecherchee = adresseTrouvee.getAddressLine(0);
                    vueRecherche.setQuery(adresseRecherchee, false);

                    // Récupère le contrôleur de la carte pour changer la position de la vue
                    IMapController mapController = carte.getController();
                    mapController.setCenter(point); // centre sur le point trouvé
                    mapController.setZoom(19.0);

                    // Crée un marqueur pour afficher le point sur la carte
                    marqueur = new Marker(carte);
                    marqueur.setPosition(point);
                    marqueur.setTitle(adresseRecherchee);
                    marqueur.setDraggable(true);
                    marqueur.setOnMarkerClickListener((marker, mapView) -> {
                        new AlertDialog.Builder(ActiviteMap.this)
                            .setTitle(R.string.marqueur_titre)
                            .setMessage(R.string.marqueur_message)
                            .setPositiveButton(R.string.marqueur_confirmation, (dialog, which) -> {
                                carte.getOverlays().remove(marker);
                                carte.invalidate();
                                boutonValider.setVisibility(View.GONE);
                            })
                            .setNegativeButton(R.string.marqueur_annulation, null).show();
                        return true;
                    });

                    // Supprime les anciens marqueurs avant d'ajouter le nouveau
                    carte.getOverlays().clear();
                    carte.getOverlays().add(marqueur);
                    boutonValider.setVisibility(View.VISIBLE);

                    // Met à jour l'affichage de la carte
                    carte.invalidate();

                } else {
                    Toast.makeText(this, R.string.recherche_adresse_aucun_resultat, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, R.string.recherche_adresse_erreur, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void validerAdresse(View v) {
        Intent retourInscription = new Intent();
        retourInscription.putExtra("adresse", marqueur.getTitle());
        retourInscription.putExtra("longitude", marqueur.getPosition().getLongitude());
        retourInscription.putExtra("latitude", marqueur.getPosition().getLatitude());
        setResult(RESULT_OK, retourInscription);
        finish();
    }
}
