package fr.iutrodez.sae501.cliandcollect.activites;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.requetes.VolleyCallback;
import fr.iutrodez.sae501.cliandcollect.utile.Distance;
import fr.iutrodez.sae501.cliandcollect.utile.GestionBouton;
import fr.iutrodez.sae501.cliandcollect.utile.LocalisationAdapter;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

/**
 * Activité de la page d'inscription.
 * @author Descriaud Lucas
 */
public class ActiviteInscription extends AppCompatActivity {

    final static int NB_MAX_ADRESSES_PROPOSEES = 7;

    private EditText mail;
    private EditText mdp;
    private EditText nom;
    private EditText prenom;
    private EditText adresse;
    private TextView messageErreur;
    private TextView messageErreurAdresse;

    public static SharedPreferences preferences;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;

    private CheckBox seRappelerDeMoi;

    private Button boutonObtenirCoordonnees;
    private Button boutonEditerAdresse;
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
        messageErreur = findViewById(R.id.messageErreur);
        messageErreurAdresse = findViewById(R.id.messageErreurAdresse);
        seRappelerDeMoi = findViewById(R.id.seRappelerDeMoi);
        boutonObtenirCoordonnees = findViewById(R.id.boutonObtenirCoordonnees);
        boutonEditerAdresse = findViewById(R.id.boutonEditerAdresse);
        boutonSubmitInscription = findViewById(R.id.boutonInscription);

        preferences = getDefaultSharedPreferences(getApplicationContext());

        setAdresseEditable(true);

        boutonEditerAdresse.setOnClickListener(v -> setAdresseEditable(true));
        boutonSubmitInscription.setOnClickListener(this::inscription);

        GestionBouton.activerBoutonSiTexteEntre(adresse, boutonObtenirCoordonnees);
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
        masquerMessageErreur();

        JSONObject donnees = donneeFormulaireEnJson();
        if (Reseau.reseauDisponible(this, R.id.messageErreur, R.string.erreur_reseau) && donnees != null) {
            ClientApi.inscription(this, donnees, () -> {
                Preferences
                .sauvegarderInfosConnexion(this,
                                           mail.getText().toString(),
                                           mdp.getText().toString(),
                                           seRappelerDeMoi.isChecked());

                Intent menuPrincipal = new Intent(ActiviteInscription.this, GestionFragment.class);
                startActivity(menuPrincipal);
                finish();
            });
        }
    }

    /**
     * Active ou désactive le mode édition pour le champ d'adresse.
     * @param editable True si le champ doit être éditable, false sinon.
     */
    private void setAdresseEditable(boolean editable) {
        adresse.setEnabled(editable);

        boutonEditerAdresse.setEnabled(!editable);
        boutonSubmitInscription.setEnabled(!editable);

        if (!adresse.getText().toString().isEmpty()) {
            boutonObtenirCoordonnees.setEnabled(editable);
        }
        boutonEditerAdresse.setVisibility(editable ? View.GONE : View.VISIBLE);
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
            afficherMessageErreur(e.getMessage().equals("Forbidden numeric value: NaN")
                                  ? R.string.coordonnees_non_calculees
                                  : R.string.erreur_inscription,
                                  messageErreur);
            donnees = null;
        }
        return donnees;
    }

    /**
     * Affiche un message d'erreur global.
     * @param message L'id du message d'erreur à afficher.
     * @param entreeMessageErreur L'entrée de texte où afficher le message d'erreur.
     */
    private void afficherMessageErreur(int message, TextView entreeMessageErreur) {
        entreeMessageErreur.setVisibility(View.VISIBLE);
        entreeMessageErreur.setText(message);
    }

    /**
     * Masque le message d'erreur global.
     */
    private void masquerMessageErreur() {
        messageErreur.setVisibility(View.GONE);
        messageErreur.setText("");
    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            adresse.setText(retour.getStringExtra("adresse"));
        }
    }
}