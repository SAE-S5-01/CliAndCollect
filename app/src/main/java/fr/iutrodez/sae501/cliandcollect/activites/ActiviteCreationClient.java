package fr.iutrodez.sae501.cliandcollect.activites;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.requetes.VolleyCallback;
import fr.iutrodez.sae501.cliandcollect.utile.Distance;

public class ActiviteCreationClient extends AppCompatActivity {

    private static EditText nomEntreprise;

    private static EditText saisieAdresse;

    private static EditText description;

    private static EditText prenomContact;


    private static EditText telephone;

    private static EditText nomContact;

    private static RadioGroup clientProspect;

    private static EditText coordonnees;

    private  static TextView erreur;

    private Client nouveauClient;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;
    private Button boutonValider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajouter_client);
        nomEntreprise = findViewById(R.id.nomEntreprise);
        saisieAdresse = findViewById(R.id.adresseEntreprise);
        telephone = findViewById(R.id.telephone);
        description = findViewById(R.id.description);
        coordonnees = findViewById(R.id.coordonnees);
        prenomContact = findViewById(R.id.prenomContact);
        nomContact = findViewById(R.id.nomContact);
        clientProspect = findViewById(R.id.clientProspect);
        erreur = findViewById(R.id.erreurFormulaire);
        boutonValider = findViewById(R.id.boutonValider);
        boutonValider.setEnabled(false);

    }

    public void retour(View view){
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        Log.d("latitude",  "" + latitude);
        Log.d("longitude",  "" + longitude);
        if (nomEntreprise.getText().toString().isEmpty()) {
            Toast.makeText(this, "Le nom de l'entreprise est obligatoire", Toast.LENGTH_LONG).show();
        } else if (saisieAdresse.getText().toString().isEmpty()) {
            Toast.makeText(this, "L'adresse de l'entreprise est obligatoire", Toast.LENGTH_LONG).show();
        } else {
            JSONObject donnees = formulaireEnJson();
            // TODO ajout toast ?
            if (ClientApi.reseauDisponible(this)) {
                ClientApi.creationClient(this, donnees, () -> {
                    setResult(AppCompatActivity.RESULT_OK);
                    finish();
                });
            } else {
                Toast.makeText(this, R.string.erreur_reseau, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void obtenirCoordonnees(View view) {
        try {
            // STUB (rodez) TODO remplacer par geoloc
            double[] viewBox = Distance.creationViewBox(44.333333  , 2.566667);
            ClientApi.verifierAddresse(saisieAdresse.getText().toString(),  viewBox,this, new VolleyCallback() {
                @Override
                public void onSuccess(List<Map<String , String>> results) {
                    String[] options = new String[results.size()];
                    for (int i = 0; i < results.size(); i++) {
                        options[i] = results.get(i).get("display_name");
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(ActiviteCreationClient.this);
                    builder.setTitle("Choisissez une option");
                    builder.setItems(options, (dialog, which) -> {
                        Map<String, String> selectedLocation = results.get(which);
                        String lat = selectedLocation.get("lat");
                        String lon = selectedLocation.get("lon");
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(lon);
                        saisieAdresse.setText(selectedLocation.get("display_name"));
                        boutonValider.setEnabled(true);
                    });
                    builder.show();
                }
                @Override
                public void onError(String error) {
                    Log.e("erro", "------------------------------------" + error);
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e("erreur", "------------------------------------" + e);
        }
    }

    private static JSONObject formulaireEnJson() {
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
            // TODO
            Log.e("erreur", "Catch form json" + e);
        }
        return donnees;
    }
}
