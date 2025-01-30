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
import android.widget.TextView;

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

    private  EditText mail;
    private  EditText mdp;
    private EditText nom;
    private EditText prenom;
    private EditText adresse;
    private TextView messageErreur;
    public static SharedPreferences preferences;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;
    private CheckBox seRappelerdeMoi;
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
        mail = findViewById(R.id.saisieMail);
        mdp =  findViewById(R.id.saisieMdp);
        nom = findViewById(R.id.saisieNom);
        prenom = findViewById(R.id.saisiePrenom);
        adresse = findViewById(R.id.saisieAdresse);
        adresse.setEnabled(false);
        messageErreur = findViewById(R.id.messageErreur);
        seRappelerdeMoi = findViewById(R.id.seRappelerDeMoi);
        preferences = getDefaultSharedPreferences(getApplicationContext());
        boutonSubmitInscription = findViewById(R.id.boutonInscription);
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
        if (Reseau.reseauDisponible(this, true) && donnees != null) {
            ClientApi.inscription(this, donnees, () -> {
                ActivitePrincipale
                .preferencesConnexion(seRappelerdeMoi.isChecked(),
                                      mail.getText().toString(),
                                      mdp.getText().toString());

                Intent menuPrincipal = new Intent(ActiviteInscription.this, GestionFragment.class);
                startActivity(menuPrincipal);
            });
        }
    }

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
            Log.e("Inscription", "Erreur lors de la création de l'objet JSON", e);
            messageErreur.setText(e.getMessage().equals("Forbidden numeric value: NaN")
                                  ? R.string.coordonnees_non_calculees
                                  : R.string.erreur_inscription);
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
        }
    }
}