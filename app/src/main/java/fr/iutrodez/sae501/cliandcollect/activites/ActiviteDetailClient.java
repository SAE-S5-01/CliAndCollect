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
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

public class ActiviteDetailClient extends AppCompatActivity {

    private EditText nomEntreprise;

    private EditText saisieAdresse;

    private EditText description;

    private EditText prenomContact;

    private EditText telephone;

    private EditText nomContact;

    private RadioGroup clientProspect;

    private TextView coordonnees;

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
        clientProspect = findViewById(R.id.ClientProspect);
        clientProspect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isClient = checkedId == R.id.client;
            }
        });
        coordonnees = findViewById(R.id.coordonnees);
        telephone = findViewById(R.id.telephone);
        Button retour = findViewById(R.id.boutonRetour);
        Button valider = findViewById(R.id.boutonModifier);

        retour.setOnClickListener(this::retour);
        valider.setOnClickListener(this::valider);

        id = intention.getIntExtra("ID", 0);
        initialiser();

        intentionRetour = new Intent();

        lanceurMap = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                this::retourMap);
    }

    public void retour(View view){
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view){
        intentionRetour.putExtra("ID",id);
        if(isModifie()) {
            if (!nomEntreprise.getText().toString().equals(client.getEntreprise())) {
                client.setEntreprise(nomEntreprise.getText().toString());
            }
            if(!saisieAdresse.getText().toString().equals(client.getAdresse())){
                client.setAdresse(saisieAdresse.getText().toString());
                client.setX(longitude);
                client.setY(latitude);
            }
            if(!description.getText().toString().equals(client.getDescription())){
                client.setDescription(description.getText().toString());
            }
            if(!nomContact.getText().toString().equals(client.getNomContact())){
                client.setNomContact(nomContact.getText().toString());
            }
            if(!prenomContact.getText().toString().equals(client.getPrenomContact())){
                client.setPrenomContact(prenomContact.getText().toString());
            }
            if(!telephone.getText().toString().equals(client.getTelephone())){
                client.setTelephone(telephone.getText().toString());
            }
            if(isClient != client.isClient()){
                client.setClientPropspect(isClient);
            }

            JSONObject donnees = formulaireEnJson();

            if (Reseau.reseauDisponible(this,true)) {
                ClientApi.modificationClient(this, donnees, client.getID().toString());
                setResult(AppCompatActivity.RESULT_OK,intentionRetour);
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


    public void initialiser(){
        client = SingletonListeClient.getClient(id);
        nomEntreprise.setText(client.getEntreprise());
        saisieAdresse.setText(client.getAdresse());
        description.setText(client.getDescription());
        // On lit les co gps N/S puis O/E donc y puis x
        coordonnees.setText(String.format("%s : %s",client.getY(), client.getX()));
        clientProspect.check(client.isClient()?R.id.client : R.id.propspect);
        prenomContact.setText(client.getPrenomContact());
        nomContact.setText(client.getNomContact());
        telephone.setText(client.getTelephone());
    }

    private boolean isModifie(){
        return !nomEntreprise.getText().toString().equals(client.getEntreprise())
                || !saisieAdresse.getText().toString().equals(client.getAdresse())
                || !description.getText().toString().equals(client.getDescription())
                || !prenomContact.getText().toString().equals(client.getPrenomContact())
                || !nomContact.getText().toString().equals(client.getNomContact())
                || !telephone.getText().toString().equals(client.getTelephone())
                || !(isClient == client.isClient());
    }

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
            coordonnees.setText(String.format("%s : %s", latitude, longitude));
        }
    }
}
