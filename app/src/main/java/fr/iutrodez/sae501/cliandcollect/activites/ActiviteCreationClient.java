package fr.iutrodez.sae501.cliandcollect.activites;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

public class ActiviteCreationClient extends AppCompatActivity {

    private static EditText nomEntreprise;

    private static EditText saisieAdresse;

    private static EditText description;

    private static EditText prenomContact;

    private static EditText telephone;

    private static EditText nomContact;

    private static RadioGroup clientProspect;

    private  static TextView erreur;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;

    private ActivityResultLauncher<Intent> lanceurMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajouter_client);
        nomEntreprise = findViewById(R.id.nomEntreprise);
        saisieAdresse = findViewById(R.id.adresseEntreprise);
        saisieAdresse.setEnabled(false);
        telephone = findViewById(R.id.telephone);
        description = findViewById(R.id.description);
        prenomContact = findViewById(R.id.prenomContact);
        nomContact = findViewById(R.id.nomContact);
        clientProspect = findViewById(R.id.clientProspect);
        erreur = findViewById(R.id.erreurFormulaire);
        lanceurMap = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                this::retourMap);
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
            // TODO modifier gestion erreur réseau ?
            if (Reseau.reseauDisponible(this, true)) {
                ClientApi.creationClient(this, donnees, () -> {
                    setResult(AppCompatActivity.RESULT_OK);
                    finish();
                });
            }
        }
    }

    public void obtenirCoordonnees(View view) {
        Intent map = new Intent(this, ActiviteMap.class);
        lanceurMap.launch(map);
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

    private void retourMap(ActivityResult retourMap) {
        Intent retour = retourMap.getData();

        if (retourMap.getResultCode() == RESULT_OK) {
            latitude = retour.getDoubleExtra("latitude", Double.NaN);
            longitude = retour.getDoubleExtra("longitude", Double.NaN);
            saisieAdresse.setText(retour.getStringExtra("adresse"));
        }
    }
}
