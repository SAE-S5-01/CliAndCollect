/*
 * ActiviteInscription.java                                         30 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONObject;

import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de la page d'inscription.
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class ActiviteInscription extends AppCompatActivity {

    private EditText mail;
    private EditText mdp;
    private EditText nom;
    private EditText prenom;
    private EditText adresse;

    public static SharedPreferences preferences;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;

    private CheckBox seRappelerDeMoi;

    private Button boutonObtenirCoordonnees;
    private Button boutonSubmitInscription;

    private ActivityResultLauncher<Intent> lanceurMap;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_inscription);

        ImageView boutonOptionMenu = findViewById(R.id.boutonOptionMenu);
        boutonOptionMenu.setVisibility(View.INVISIBLE);

        mail = findViewById(R.id.saisieMail);
        mdp =  findViewById(R.id.saisieMdp);
        nom = findViewById(R.id.saisieNom);
        prenom = findViewById(R.id.saisiePrenom);
        adresse = findViewById(R.id.saisieAdresse);
        adresse.setEnabled(false);
        seRappelerDeMoi = findViewById(R.id.seRappelerDeMoi);
        boutonObtenirCoordonnees = findViewById(R.id.boutonObtenirCoordonnees);
        boutonSubmitInscription = findViewById(R.id.boutonInscription);

        preferences = getDefaultSharedPreferences(getApplicationContext());

        boutonObtenirCoordonnees.setOnClickListener(this::obtenirCoordonnees);
        boutonSubmitInscription.setOnClickListener(this::inscription);

        lanceurMap = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                                               this::retourMap);
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Récupère les informations saisies par l'utilisateur et les envoie à l'API.
     * Connecte l'utilisateur en cas d'informations valides
     * @param view Le bouton d'inscription
     *
     */
    private void inscription(View view) {
        JSONObject donnees = donneeFormulaireEnJson();
        if (Reseau.reseauDisponible(this) && donnees != null) {
            ClientApi.inscription(this, donnees, () -> {
                Preferences
                .sauvegarderInfosConnexion(this,
                                           mail.getText().toString(),
                                           mdp.getText().toString(),
                                           seRappelerDeMoi.isChecked());

                Intent menuPrincipal = new Intent(ActiviteInscription.this, GestionFragment.class);
                menuPrincipal.putExtra(GestionFragment.CLE_EXTRA_MSG_BIENVENUE, true);

                startActivity(menuPrincipal);
                finish();
            });
        } else {
            SnackbarCustom.show(this, R.string.erreur_reseau, SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Clic sur le bouton "Obtenir les coordonnées".
     * @param view Le bouton "Obtenir les coordonnées"
     */
    public void obtenirCoordonnees(View view) {
        Intent map = new Intent(ActiviteInscription.this, ActiviteMap.class);
        lanceurMap.launch(map);
    }

    /**
     * Récupère les informations saisies par l'utilisateur et les transforme en objet JSON.
     * @return Les informations saisies par l'utilisateur sous forme d'objet JSON.
     */
    private JSONObject donneeFormulaireEnJson() {
        JSONObject donnees = new JSONObject();
        try {
            donnees.put("mail", mail.getText().toString());
            donnees.put("motDePasse", mdp.getText().toString());
            donnees.put("nom", nom.getText().toString());
            donnees.put("prenom", prenom.getText().toString());
            donnees.put("adresse", adresse.getText().toString());
            donnees.put("latitude", latitude);
            donnees.put("longitude", longitude);
        } catch (Exception e) {
            SnackbarCustom.show(this,
                                e.getMessage().equals("Forbidden numeric value: NaN")
                                ? R.string.coordonnees_non_calculees
                                : R.string.erreur_inscription,
                                SnackbarCustom.STYLE_ERREUR);
            donnees = null;
        }
        return donnees;
    }

    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            adresse.setText(retour.getStringExtra("adresse"));
            boutonObtenirCoordonnees.setActivated(true);
            boutonSubmitInscription.setEnabled(true);
        }
    }
}