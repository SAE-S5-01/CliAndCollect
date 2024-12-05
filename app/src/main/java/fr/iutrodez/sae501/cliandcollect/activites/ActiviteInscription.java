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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;

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
    private EditText ville;
    private TextView messageErreur;
    public static SharedPreferences preferences;

    private CheckBox seRappelerdeMoi;

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
        ville = findViewById(R.id.saisieVille);
        messageErreur = findViewById(R.id.messageErreur);
        seRappelerdeMoi = findViewById(R.id.seRappelerDeMoi);
        preferences = getDefaultSharedPreferences(getApplicationContext());
        Button boutonSubmitInscription = findViewById(R.id.boutonInscription);
        boutonSubmitInscription.setOnClickListener(this::inscription);
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
        if (ClientApi.reseauDisponible(this)) {
            ClientApi.inscription(this, donnees, () -> {
                ActivitePrincipale.preferencesConnexion(seRappelerdeMoi.isChecked(),
                        mail.getText().toString(), mdp.getText().toString());
                Intent menuPrincipal = new Intent(ActiviteInscription.this, GestionFragment.class);
                startActivity(menuPrincipal);
            });
        } else {
            Toast.makeText(this, R.string.erreur_reseau ,  Toast.LENGTH_LONG).show();
        }
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
            donnees.put("ville", ville.getText().toString());
        } catch (Exception e) {
            messageErreur.setText(R.string.erreur_inscription);
        }
        return donnees;
    }
}