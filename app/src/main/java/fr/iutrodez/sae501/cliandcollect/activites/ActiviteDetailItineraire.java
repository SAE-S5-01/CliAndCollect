package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

public class ActiviteDetailItineraire extends AppCompatActivity {
    //TODO classe
    private EditText nomItineraire;

    private EditText listeContacts;

    private Itineraire itineraire;

    private int id;

    private Intent intentionRetour;

    private Intent intention;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intention = getIntent();
        setContentView(R.layout.detail_itineraire);
        nomItineraire = findViewById(R.id.saisieNom);
        // TODO listeContacts = findViewById(R.id.saisie_liste_contacts);
        Button retour = findViewById(R.id.boutonRetour);
        Button valider = findViewById(R.id.boutonModifier);

        retour.setOnClickListener(this::retour);
        valider.setOnClickListener(this::valider);

        id = intention.getIntExtra("ID", 0);
        initialiser();

        intentionRetour = new Intent();
    }

    public void retour(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        intentionRetour.putExtra("ID", id);
        if (isModifie()) {
            if (!nomItineraire.getText().toString().equals(itineraire.getNom())) {
                itineraire.setNom(nomItineraire.getText().toString());
            }
            /*if (!saisieAdresse.getText().toString().equals(client.getAdresse())) {
                client.setAdresse(saisieAdresse.getText().toString());
                client.setX(longitude);
                client.setY(latitude);
            }*/

            JSONObject donnees = formulaireEnJson();

            if (Reseau.reseauDisponible(this, true)) {
                ClientApi.modificationItineraire(this, donnees, itineraire.getID().toString());
                setResult(AppCompatActivity.RESULT_OK, intentionRetour);
                finish();
            }
        } else {
            Toast.makeText(this, R.string.pasModifier, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private JSONObject formulaireEnJson() {
        JSONObject donnees = new JSONObject();
        try {
            donnees.put("nomItineraire", nomItineraire.getText().toString());
            //donnees.put("ordreClients", listeContacts.getText().toString());

        } catch (Exception e) {
            // TODO
            Log.e("erreur", "Catch form json" + e);
        }
        return donnees;
    }


    public void initialiser() {
        itineraire = SingletonListeItineraire.getItineraire(id);
        nomItineraire.setText(itineraire.getNom());
        //listeContacts.setText(itineraire.getOrdreClients().get(id));
    }

    private boolean isModifie() {
        return !nomItineraire.getText().toString().equals(itineraire.getNom())
                //|| !listeContacts.getText().toString().equals(itineraire.getOrdreClients())
                  ;
    }

}
