package fr.iutrodez.sae501.cliandcollect.activites;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;

/**
 * Activité de la page de connexion.
 * @author Descriaud Lucas
 */
public class ActiviteConnexion extends AppCompatActivity {

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
        setContentView(R.layout.activite_connexion);
        boolean reseau = ClientApi.reseauDisponible(this);
        Button boutonConnexion = findViewById(R.id.boutonConnexion);
        Button boutonInscription = findViewById(R.id.boutonInscription);

        //boutonConnexion.setEnabled(false);
        /*
         * Affiche au lancement une eventuelle connexion réseau manquante ou alors l'impossibilité
         * de joindre l'api
         */
        /*if (reseau) {
            ClientApi.apijoignable(this);
        } else {
            messageErreur.setText(R.string.erreur_reseau);
        }*/

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
            messageErreur.setText(R.string.erreur_champ_connexion_vide);
        } else if (ClientApi.reseauDisponible(this)) {
            //ClientApi.connexion(this, mail, mdp, () -> {
                Intent menuPrincipal = new Intent(ActiviteConnexion.this, ActivitePrincipale.class);
                startActivity(menuPrincipal);
            //});
        } else {
            messageErreur.setText(R.string.erreur_reseau);
        }
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Redirige vers la page d'inscription
     * @param bouton Le bouton d'inscription
     */
    private void clicInscription(View bouton) {
        Intent incription = new Intent(ActiviteConnexion.this, ActiviteInscription.class);
        startActivity(incription);
    }
}