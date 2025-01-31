/*
 * ActiviteDetailClient.java                                        31 jan. 2025
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
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONObject;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de la page de détail d'un client.
 *
 * @author Loïc FAUGIERES
 * @author Noah MIQUEL
 */
public class ActiviteDetailClient extends AppCompatActivity {

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

    private int id;

    private boolean isClient;

    private Client client;

    private Intent intentionRetour;

    private Intent intention;

    private ActivityResultLauncher<Intent> lanceurMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intention = getIntent();
        setContentView(R.layout.detail_client);
        nomEntreprise = findViewById(R.id.saisieNom);
        saisieAdresse = findViewById(R.id.saisieAdresse);
        description = findViewById(R.id.Description);
        prenomContact = findViewById(R.id.prenomContact);
        nomContact = findViewById(R.id.nomContact);
        clientProspect = findViewById(R.id.clientProspect);
        telephone = findViewById(R.id.telephone);

        Button obtenirCoordonnees = findViewById(R.id.obtenirCoordonnees);
        Button boutonRetour = findViewById(R.id.boutonRetour);
        boutonValider = findViewById(R.id.boutonModifier);

        obtenirCoordonnees.setOnClickListener(this::obtenirCoordonnees);
        boutonRetour.setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        id = intention.getIntExtra("ID", 0);
        initialiserChamps();

        intentionRetour = new Intent();

        lanceurMap = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::retourMap);
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

        EditText[] champs = {nomEntreprise, saisieAdresse, description, prenomContact, nomContact, telephone};

        for (EditText champ : champs) {
            champ.addTextChangedListener(champModifieListener);
        }

        clientProspect.setOnCheckedChangeListener((group, checkedId) -> {
            isClient = checkedId == R.id.client;
            boutonValider.setEnabled(true);
        });
    }

    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        intentionRetour.putExtra("ID", id);
        if (isModifie()) {
            client.setEntreprise(nomEntreprise.getText().toString());
            client.setDescription(description.getText().toString());
            client.setAdresse(saisieAdresse.getText().toString());
            client.setX(longitude);
            client.setY(latitude);
            client.setNomContact(nomContact.getText().toString());
            client.setPrenomContact(prenomContact.getText().toString());
            client.setTelephone(telephone.getText().toString());
            client.setClientPropspect(isClient);

            JSONObject donnees = formulaireEnJson();

            if (Reseau.reseauDisponible(this,true)) {
                ClientApi.modificationClient(this, donnees, client.getID().toString());
                setResult(AppCompatActivity.RESULT_OK, intentionRetour);
                finish();
            }
        } else {
            SnackbarCustom.show(this, R.string.pasModifie, SnackbarCustom.STYLE_ATTENTION);
        }
    }



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
                : R.string.erreur_modification_client,
                SnackbarCustom.STYLE_ERREUR);
            donnees = null;
        }
        return donnees;
    }

    public void initialiserChamps() {
        client = SingletonListeClient.getClient(id);
        nomEntreprise.setText(client.getEntreprise());
        saisieAdresse.setText(client.getAdresse());
        description.setText(client.getDescription());
        clientProspect.check(client.isClient() ? R.id.client : R.id.prospect);
        prenomContact.setText(client.getPrenomContact());
        nomContact.setText(client.getNomContact());
        telephone.setText(client.getTelephone());
    }

    private boolean isModifie() {
        return !nomEntreprise.getText().toString().equals(client.getEntreprise())
                || !saisieAdresse.getText().toString().equals(client.getAdresse())
                || !description.getText().toString().equals(client.getDescription())
                || !prenomContact.getText().toString().equals(client.getPrenomContact())
                || !nomContact.getText().toString().equals(client.getNomContact())
                || !telephone.getText().toString().equals(client.getTelephone())
                || isClient != client.isClient();
    }

    /**
     * Clic sur le bouton "Obtenir les coordonnées".
     * @param view Le bouton "Obtenir les coordonnées"
     */
    public void obtenirCoordonnees(View view) {
        Intent map = new Intent(ActiviteDetailClient.this, ActiviteMap.class);
        lanceurMap.launch(map);
    }

    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            saisieAdresse.setText(retour.getStringExtra("adresse"));

        }
    }
}
