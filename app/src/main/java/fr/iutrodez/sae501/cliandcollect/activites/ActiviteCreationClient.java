/*
 * ActiviteCreationClient.java                                      31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONObject;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de la page de création d'un client / prospect.
 *
 * @author Loïc FAUGIERES
 * @author Noah MIQUEL
 */
public class ActiviteCreationClient extends AppCompatActivity {

    private EditText nomEntreprise;
    private EditText saisieAdresse;
    private EditText description;
    private EditText prenomContact;
    private EditText telephone;
    private EditText nomContact;

    private Button boutonValider;

    private RadioGroup clientProspect;

    double latitude;
    double longitude;

    private ActivityResultLauncher<Intent> lanceurMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_client);

        TextView titre = findViewById(R.id.titreDetailClient);
        titre.setText(R.string.ajouter_client);

        nomEntreprise = findViewById(R.id.saisieNom);
        saisieAdresse = findViewById(R.id.saisieAdresse);
        description = findViewById(R.id.description);
        prenomContact = findViewById(R.id.prenomContact);
        nomContact = findViewById(R.id.nomContact);
        clientProspect = findViewById(R.id.clientProspect);
        telephone = findViewById(R.id.telephone);

        Button obtenirCoordonnees = findViewById(R.id.obtenirCoordonnees);
        Button boutonRetour = findViewById(R.id.boutonRetour);
        boutonValider = findViewById(R.id.boutonModifier);

        obtenirCoordonnees.setOnClickListener(this::obtenirCoordonnees);
        obtenirCoordonnees.setBackgroundTintList(getResources().getColorStateList(R.color.bouton_principal));

        boutonRetour.setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        lanceurMap = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::retourMap);
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
        if (nomEntreprise.getText().toString().isEmpty()) {
            this.nomEntreprise.setError(getString(R.string.erreur_nom_entreprise_non_renseigne));
        } else {
            JSONObject donnees = formulaireEnJson();
            if (Reseau.reseauDisponible(this, true) && donnees != null) {
                ClientApi.creationClient(this, donnees, () -> {
                    setResult(AppCompatActivity.RESULT_OK);
                    finish();
                });
            }
        }
    }

    /**
     * @return Les données du formulaire sous forme de JSON
     */
    private JSONObject formulaireEnJson() {
        JSONObject donnees = new JSONObject();

        try {
            donnees.put("adresse" , saisieAdresse.getText().toString());
            donnees.put("nomEntreprise", nomEntreprise.getText().toString());
            donnees.put("telephone", telephone.getText().toString());
            donnees.put("description", description.getText().toString());
            donnees.put("prenomContact", prenomContact.getText().toString());
            donnees.put("nomContact", nomContact.getText().toString());
            donnees.put("prospect", clientProspect.getCheckedRadioButtonId() == R.id.prospect);
            donnees.put("latitude", latitude);
            donnees.put("longitude", longitude);

        } catch (Exception e) {
            SnackbarCustom.show(this,
                                e.getMessage().equals("Forbidden numeric value: NaN")
                                ? R.string.coordonnees_non_calculees
                                : R.string.erreur_creation_client,
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
        Intent map = new Intent(ActiviteCreationClient.this, ActiviteMap.class);
        lanceurMap.launch(map);
    }

    /**
     * Retour de l'activité de la carte.
     * @param retourMap Le retour de l'activité de la carte
     */
    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            saisieAdresse.setText(retour.getStringExtra("adresse"));
            boutonValider.setEnabled(true);
        }
    }
}