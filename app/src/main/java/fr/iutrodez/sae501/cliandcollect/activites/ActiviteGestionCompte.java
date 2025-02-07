/*
 * ActiviteGestionCompte.java                                       07 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Compte;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de la page de gestion du compte utilisateur.
 *
 * @author Loïc FAUGIERES
 */
public class ActiviteGestionCompte extends AppCompatActivity {

    private Compte compte;

    private EditText mail;
    private EditText mdp;
    private EditText nom;
    private EditText prenom;
    private EditText adresse;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;

    private CheckBox seRappelerDeMoi;

    private Button boutonObtenirCoordonnees;
    private Button boutonModifier;

    private ActivityResultLauncher<Intent> lanceurMap;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_compte);

        TextView titre = findViewById(R.id.titrePageCompte);
        titre.setText(R.string.modifier_compte);

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
        boutonObtenirCoordonnees.setActivated(true);
        Button boutonRetour = findViewById(R.id.boutonRetour);
        boutonModifier = findViewById(R.id.boutonModifier);

        findViewById(R.id.actionInscription).setVisibility(View.GONE);
        findViewById(R.id.actionsGestionCompte).setVisibility(View.VISIBLE);
        seRappelerDeMoi.setChecked(!Preferences.getEmail(this).isEmpty());

        initialiserChamps();

        boutonObtenirCoordonnees.setOnClickListener(this::obtenirCoordonnees);
        boutonRetour.setOnClickListener(this::retour);
        boutonModifier.setOnClickListener(this::validerModifications);

        lanceurMap = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                                               this::retourMap);
    }

    /**
     * Initialise les champs de la page avec les valeurs du compte.
     */
    public void initialiserChamps() {
        if (Reseau.reseauDisponible(this, false)) {
            ClientApi.getCompte(this, () -> {
                compte = Compte.getInstance();
                mail.setText(compte.getEmail());
                nom.setText(compte.getNom());
                prenom.setText(compte.getPrenom());
                adresse.setText(compte.getAdresse());
                latitude = compte.getLatitude();
                longitude = compte.getLongitude();
            }, () -> {
                SnackbarCustom.show(this, R.string.erreur_recuperation_compte, SnackbarCustom.STYLE_ERREUR);
                actionRetardee(() -> retour(null));
            });
        } else {
            Intent menuConnexion = new Intent(ActiviteGestionCompte.this, ActiviteConnexion.class);
            startActivity(menuConnexion);
            finish();
        }
    }

    /**
     * Attendre 3 secondes puis effectuer l'action passée en paramètre
     *
     * @param action Action à effectuer
     */
    private void actionRetardee(Runnable action) {
        new android.os.Handler().postDelayed(() -> action.run(), 3000);
    }

    /**
     * Clic sur le bouton "Retour".
     * @param view Le bouton "Retour"
     */
    public void retour(View view) {
        Intent menuPrincipal = new Intent(ActiviteGestionCompte.this, GestionFragment.class);
        startActivity(menuPrincipal);
        finish();
    }

    /**
     * Méthode invoquée lors du clic sur le bouton de validation.
     * Récupère les informations saisies par l'utilisateur et les envoie à l'API.
     * Connecte l'utilisateur en cas d'informations valides
     * @param view Le bouton de validation
     */
    private void validerModifications(View view) {
        JSONObject donnees = donneeFormulaireEnJson();
        if (Reseau.reseauDisponible(this) && donnees != null) {
            ClientApi.modifierCompte(this, donnees, () -> {
                Preferences
                .sauvegarderInfosConnexion(this,
                                           mail.getText().toString(),
                                           mdp.getText().toString(),
                                           seRappelerDeMoi.isChecked());

                compte.setEmail(mail.getText().toString());
                compte.setMotDePasse(mdp.getText().toString());
                compte.setNom(nom.getText().toString());
                compte.setPrenom(prenom.getText().toString());
                compte.setAdresse(adresse.getText().toString());
                compte.setLatitude(latitude);
                compte.setLongitude(longitude);

                Intent menuPrincipal = new Intent(ActiviteGestionCompte.this, GestionFragment.class);

                startActivity(menuPrincipal);
                finish();
            });
        } else {
            SnackbarCustom.show(this, R.string.erreur_reseau, SnackbarCustom.STYLE_ERREUR);
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
            donnees.put("latitude", latitude);
            donnees.put("longitude", longitude);
        } catch (Exception e) {
            SnackbarCustom.show(this,
                                e.getMessage().equals("Forbidden numeric value: NaN")
                                ? R.string.coordonnees_non_calculees
                                : R.string.erreur_modification_compte,
                                SnackbarCustom.STYLE_ERREUR);
            donnees = null;
        }
        return donnees;
    }

    /**
     * Clic sur le bouton "Obtenir les coordonnées".
     * @param view Le bouton "Obtenir les coordonnées"
     */
    public void obtenirCoordonnees(View view) {
        Intent map = new Intent(ActiviteGestionCompte.this, ActiviteMap.class);
        lanceurMap.launch(map);
    }

    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            adresse.setText(retour.getStringExtra("adresse"));
            boutonObtenirCoordonnees.setActivated(true);
            boutonModifier.setEnabled(true);
        }
    }
}