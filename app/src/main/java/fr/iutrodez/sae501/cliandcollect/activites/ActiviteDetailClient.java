/*
 * ActiviteDetailClient.java                                        31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONObject;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
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

    private boolean isProspect;

    private Client client;

    private Intent intentionRetour;

    private ActivityResultLauncher<Intent> lanceurMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_client);

        nomEntreprise = findViewById(R.id.saisieNom);
        saisieAdresse = findViewById(R.id.saisieAdresse);
        description = findViewById(R.id.description);
        prenomContact = findViewById(R.id.prenomContact);
        nomContact = findViewById(R.id.nomContact);
        clientProspect = findViewById(R.id.clientProspect);
        telephone = findViewById(R.id.telephone);

        Button obtenirCoordonnees = findViewById(R.id.obtenirCoordonnees);
        Button boutonRetour = findViewById(R.id.boutonRetour);
        boutonValider = findViewById(R.id.boutonValider);

        obtenirCoordonnees.setOnClickListener(this::obtenirCoordonnees);
        boutonRetour.setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        Intent intention = getIntent();
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
            isProspect = checkedId == R.id.prospect;
            boutonValider.setEnabled(true);
        });
    }

    /**
     * Initialise les champs de la page avec les valeurs du client.
     */
    public void initialiserChamps() {
        client = SingletonListeClient.getInstance().getClient(id);
        nomEntreprise.setText(client.getEntreprise());
        saisieAdresse.setText(client.getAdresse());
        description.setText(client.getDescription());
        clientProspect.check(client.isProspect() ? R.id.prospect : R.id.client);
        prenomContact.setText(client.getPrenomContact());
        nomContact.setText(client.getNomContact());
        telephone.setText(client.getTelephone());

        latitude = client.getY();
        longitude = client.getX();
    }

    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    /**
     * Valider les modifications du client.
     * @param view Le bouton "Valider"
     */
    public void valider(View view) {
        intentionRetour.putExtra("ID", id);
        if (isModifie()) {
            if (nomEntreprise.getText().toString().isEmpty()) {
                this.nomEntreprise.setError(getString(R.string.erreur_nom_entreprise_non_renseigne));
            } else {
                if (!nomEntreprise.getText().toString().equals(client.getEntreprise())
                    && SingletonListeItineraire.estContactDansItineraire(client.getID())) {
                    SnackbarCustom.show(ActiviteDetailClient.this,
                                        R.string.info_modif_nom_entreprise,
                                        SnackbarCustom.STYLE_INFORMATION);

                    // On attend 3 secondes avant de continuer
                    new Handler().postDelayed(this::secondePartieValidation, 3500);
                } else {
                    secondePartieValidation();
                }
            }
        } else {
            SnackbarCustom.show(this, R.string.pasModifie, SnackbarCustom.STYLE_ATTENTION);
        }
    }

    /**
     * Seconde partie de la modification d'un contact exécutée ou non avec un délais de 3 secondes.
     */
    private void secondePartieValidation() {
        JSONObject donnees = formulaireEnJson();
        if (Reseau.reseauDisponible(ActiviteDetailClient.this, true)
            && donnees != null) {
            ClientApi.modificationClient(this, donnees, client.getID().toString(),
                () -> {
                    client.setEntreprise(nomEntreprise.getText().toString());
                    client.setDescription(description.getText().toString());
                    client.setAdresse(saisieAdresse.getText().toString());
                    client.setX(longitude);
                    client.setY(latitude);
                    client.setNomContact(nomContact.getText().toString());
                    client.setPrenomContact(prenomContact.getText().toString());
                    client.setTelephone(telephone.getText().toString());
                    client.setEstProspect(isProspect);
                    setResult(AppCompatActivity.RESULT_OK, intentionRetour);
                    finish();
                });
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

    private boolean isModifie() {
        return !nomEntreprise.getText().toString().equals(client.getEntreprise())
                || !saisieAdresse.getText().toString().equals(client.getAdresse())
                || !description.getText().toString().equals(client.getDescription())
                || !prenomContact.getText().toString().equals(client.getPrenomContact())
                || !nomContact.getText().toString().equals(client.getNomContact())
                || !telephone.getText().toString().equals(client.getTelephone())
                || isProspect != client.isProspect();
    }

    /**
     * Clic sur le bouton "Obtenir les coordonnées".
     * @param view Le bouton "Obtenir les coordonnées"
     */
    public void obtenirCoordonnees(View view) {
        Intent map = new Intent(ActiviteDetailClient.this, ActiviteMap.class);
        lanceurMap.launch(map);
    }

    /**
     * Gestion du retour de la vue de sélection d'une adresse.
     * Si le changement porte sur l’adresse et que l’entreprise figure
     * déjà dans un itinéraire, l’utilisateur sera prévenu et informé
     * que les itinéraires comportant ce client seront supprimés.
     * Une possibilité d'annulation est présente.
     *
     * @param retourMap Le retour de la vue de sélection d'une adresse
     */
    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            String nouvelleAdresse = retour.getStringExtra("adresse");
            double nouvelleLatitude = retour.getDoubleExtra("latitude", Double.NaN);
            double nouvelleLongitude = retour.getDoubleExtra("longitude", Double.NaN);

            if (!nouvelleAdresse.equals(client.getAdresse())
                && SingletonListeItineraire.estContactDansItineraire(client.getID())) {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.changement_adresse)
                    .setMessage(R.string.adresse_confirmation_modification)
                    .setPositiveButton("Oui", (dialog, which) -> mettreAJourAdresse(nouvelleAdresse, nouvelleLatitude, nouvelleLongitude))
                    .setNegativeButton("Non", (dialog, which) -> mettreAJourAdresse(client.getAdresse(), client.getY(), client.getX()))
                    .show();
            } else {
                mettreAJourAdresse(nouvelleAdresse, nouvelleLatitude, nouvelleLongitude);
            }
        }
    }

    /**
     * Mettre à jour l'adresse du client.
     * @param adresse L'adresse du client
     * @param latitude La latitude
     * @param longitude La longitude
     */
    private void mettreAJourAdresse(String adresse, double latitude, double longitude) {
        this.saisieAdresse.setText(adresse);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
