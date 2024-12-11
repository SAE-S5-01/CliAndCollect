package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;

public class ActiviteCreationClient extends AppCompatActivity {

    private EditText nomEntreprise;

    private EditText saisieAdresse;

    private EditText description;

    private EditText prenomContact;


    private EditText telephone;

    private EditText nomContact;

    private RadioGroup clientProspect;

    private EditText coordonnees;

    private TextView erreur;

    private Client nouveauClient;


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
    }

    public void retour(View view){
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }

    public void valider(View view) {
        Intent intention = getIntent();
        boolean client = clientProspect.getCheckedRadioButtonId() == R.id.client;
        nouveauClient = new Client(nomEntreprise.getText().toString(),
                saisieAdresse.getText().toString(),coordonnees.getText().toString(),client);
        finish();
    }

}
