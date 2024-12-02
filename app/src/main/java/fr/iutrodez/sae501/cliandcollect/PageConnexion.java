package fr.iutrodez.sae501.cliandcollect;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activité de la page de connexion.
 * @author Descriaud Lucas
 */
public class PageConnexion extends AppCompatActivity {

    private ActivityResultLauncher<Intent> lanceurInscription;
    private EditText mail;
    private EditText mdp;

    private TextView messageErreur;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_connexion);
        boolean reseau = ApiClient.reseauDisponible(this);
        Button boutonConnexion = findViewById(R.id.boutonConnexion);
        Button boutonInscription = findViewById(R.id.boutonInscription);
        boutonConnexion.setEnabled(false);
        boutonConnexion.setEnabled(false);
        /*
         * Affiche au lancement une eventuelle connexion réseau manquante ou alors l'impossibilité
         * de joindre l'api
         */
        if(reseau) {
            ApiClient.apijoignable(this);
        } else {
            messageErreur.setText(R.string.messageErreurReseau);
        }

        mail = findViewById(R.id.saisieMail);
        mdp = findViewById(R.id.saisieMdp);
        messageErreur = findViewById(R.id.messageErreur);

        boutonConnexion.setOnClickListener(this::clicConnexion);
        boutonInscription.setOnClickListener(this::clicInscription);
    }

    /**
     * Méthode invoquée lors du clic sur le bouton de connexion.
     * @param bouton Le bouton de connexion
     */
    private void clicConnexion(View bouton) {
        // TODO : appelApi pour verifier la connection
        String mail , mdp;
        mail = this.mail.getText().toString();
        mdp = this.mdp.getText().toString();

        if (mail.isEmpty() || mdp.isEmpty()) {
            messageErreur.setText(R.string.messageErreurClient);
        } else if (ApiClient.reseauDisponible(this)) {
            ApiClient.connexion(this, mail, mdp, () -> {
                Intent menuPrincipal = new Intent(PageConnexion.this, MainActivity.class);
                startActivity(menuPrincipal);
            });
        } else {
            messageErreur.setText(R.string.messageErreurReseau);
        }
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Redirige vers la page d'inscription
     * @param bouton Le bouton d'inscription
     */
    private void clicInscription(View bouton) {
        Intent incription = new Intent(PageConnexion.this, PageInscription.class);
        startActivity(incription);
    }
}