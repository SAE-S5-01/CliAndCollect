package fr.iutrodez.sae501.cliandcollect.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Activité de la page d'inscription.
 * @author Descriaud Lucas
 */
public class ActiviteInscription extends AppCompatActivity {

    private  EditText mail;
    private  EditText mdp;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_inscription);
        mail = findViewById(R.id.saisieMail);
        mdp =  findViewById(R.id.saisieMdp);
        Button boutonSubmitInscription = findViewById(R.id.boutonInscription);
        boutonSubmitInscription.setOnClickListener(this::inscription);
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Récupère les informations saisies par l'utilisateur et les envoie à l'API.
     * Connecte l'utilisateur en cas d'informations valides
     * @param view Le bouton d'inscription
     *
     */
    private void inscription(View view) {
        // TODO : appelApi
        Intent menuPrincipal = new Intent(ActiviteInscription.this, ActivitePrincipale.class);
        startActivity(menuPrincipal);
    }

}