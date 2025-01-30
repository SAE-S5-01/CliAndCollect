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


public class ActiviteMap extends AppCompatActivity {


    private SearchView searchView;
    private MapView carte;

    private Button boutonRechercher;

    private Button boutonValider;

    private Marker marker;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.carte);


        carte = findViewById(R.id.map);
        carte.setTileSource(TileSourceFactory.MAPNIK);
        carte.setMultiTouchControls(true);
        carte.setBuiltInZoomControls(true);
        IMapController mapController = carte.getController();
        mapController.setZoom(11.8);

        // TODO centrer sur geolocalisation ?
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        searchView = findViewById(R.id.search_view);
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
        String adresse = searchView.getQuery().toString();

        // Crée un objet Geocoder pour convertir l'adresse en coordonnées GPS
        Geocoder geocoder = new Geocoder(this , Locale.getDefault());

        if (!adresse.isEmpty()) {
            try {
                // Récupère une liste de résultat (1 ici)
                List<Address> addresses = geocoder.getFromLocationName(adresse, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());

                    // Récupère le contrôleur de la carte pour changer la position de la vue
                    IMapController mapController = carte.getController();
                    mapController.setCenter(point); // centre sur le point trouvé
                    mapController.setZoom(15.0);

                    // Crée un marqueur pour afficher le point sur la carte
                    marker = new Marker(carte);
                    marker.setPosition(point);
                    marker.setTitle(adresse);
                    marker.setDraggable(true);
                    marker.setOnMarkerClickListener((marker, mapView) -> {
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
                    carte.getOverlays().add(marker);
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
        Log.i("Adresse", marker.getPosition().toString());

        Intent retourInscription = new Intent();
        retourInscription.putExtra("adresse", marker.getTitle());
        retourInscription.putExtra("longitude", marker.getPosition().getLongitude());
        retourInscription.putExtra("latitude", marker.getPosition().getLatitude());
        setResult(RESULT_OK, retourInscription);
        finish();
    }
}
