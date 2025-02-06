/*
 * ActiviteDetailItineraire.java                                    04 fev. 2025
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
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Activité de la page de détail d'un itinéraire.
 *
 * @author Loïc FAUGIERES
 * @author Simon GUIRAUD
 */
public class ActiviteDetailItineraire extends AppCompatActivity {

    private EditText nomItineraire;

    private Button boutonValider;

    private int id;

    private Itineraire itineraire;

    private Intent intentionRetour;

    private Intent intention;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_itineraire);

        intention = getIntent();

        nomItineraire = findViewById(R.id.saisieNom);

        Button boutonRetour = findViewById(R.id.boutonRetour);
        boutonValider = findViewById(R.id.boutonModifier);

        boutonRetour.setOnClickListener(this::retour);
        boutonValider.setOnClickListener(this::valider);

        id = intention.getIntExtra("ID", 0);
        initialiser();

        intentionRetour = new Intent();
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

        EditText[] champs = {nomItineraire};

        for (EditText champ : champs) {
            champ.addTextChangedListener(champModifieListener);
        }
    }

    public void initialiser() {
        itineraire = SingletonListeItineraire.getInstance().getItineraire(id);
        nomItineraire.setText(itineraire.getNom());
    }

    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        intentionRetour.putExtra("ID", id);
        if (isModifie()) {
            if (nomItineraire.getText().toString().isEmpty()) {
                this.nomItineraire.setError(getString(R.string.erreur_nom_entreprise_non_renseigne));
            } else {
                JSONObject donnees = formulaireEnJson();
                if (Reseau.reseauDisponible(ActiviteDetailItineraire.this, true)
                    && donnees != null) {
                    ClientApi.modifierItineraire(this, donnees, itineraire.getID().toString(),
                        () -> {
                            setResult(AppCompatActivity.RESULT_OK, intentionRetour);
                            finish();
                        });
                }
            }
        } else {
            SnackbarCustom.show(this, R.string.pasModifie, SnackbarCustom.STYLE_ATTENTION);
        }
    }

    private JSONObject formulaireEnJson() {
        JSONObject donnees = new JSONObject();

        try {
            donnees.put("nomItineraire" , nomItineraire.getText().toString());
        } catch (Exception e) {
            SnackbarCustom.show(this,
                                R.string.erreur_modification_itineraire,
                                SnackbarCustom.STYLE_ERREUR);
            donnees = null;
        }
        return donnees;
    }

    private boolean isModifie() {
        return false; // TODO
    }
}