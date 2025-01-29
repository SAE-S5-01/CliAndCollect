package fr.iutrodez.sae501.cliandcollect.activites;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

/**
 * Activité de la page de connexion.
 * @author Descriaud Lucas
 */
public class ActiviteConnexion extends AppCompatActivity {

    private EditText mail;
    private EditText mdp;

    private CheckBox seRappelerDeMoi;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_connexion);

        if (Reseau.reseauDisponible(this, true)
            && Preferences.estConnecte(this)) {
            ClientApi.connexion(this,
                Preferences.getEmail(this),
                Preferences.getMotDePasse(this),
                () -> lancerMenuPrincipal()
            );
        } else {
            ImageView boutonOptionMenu = findViewById(R.id.boutonOptionMenu);
            Button boutonConnexion = findViewById(R.id.boutonConnexion);
            Button boutonInscription = findViewById(R.id.boutonInscription);
            mail = findViewById(R.id.saisieMail);
            mdp = findViewById(R.id.saisieMdp);
            seRappelerDeMoi = findViewById(R.id.seRappelerDeMoi);

            boutonOptionMenu.setVisibility(View.INVISIBLE);

            boutonConnexion.setOnClickListener(this::clicConnexion);
            boutonInscription.setOnClickListener(this::clicInscription);
        }
    }

    /**
     * Méthode invoquée lors du clic sur le bouton de connexion.
     * @param bouton Le bouton de connexion
     */
    private void clicConnexion(View bouton) {
        String mail, mdp;
        mail = this.mail.getText().toString();
        mdp = this.mdp.getText().toString();

        if (mail.isEmpty()) {
            this.mail.setError(getString(R.string.erreur_mail_non_renseigne));
        } else if (mdp.isEmpty()) {
            this.mdp.setError(getString(R.string.erreur_mdp_non_renseigne));
        } else if (Reseau.reseauDisponible(this, true)) {
            ClientApi.connexion(this, mail, mdp, () -> {
                Preferences.sauvegarderInfosConnexion(this, mail, mdp, seRappelerDeMoi.isChecked());
                lancerMenuPrincipal();
            });
        }
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Redirige vers la page d'inscription
     * @param bouton Le bouton d'inscription
     */
    private void clicInscription(View bouton) {
        Intent incription = new Intent(this, ActiviteInscription.class);
        startActivity(incription);
    }

    /**
     * Méthode invoquée lors de la connexion réussie.
     * Redirige vers l'activité principale.
     */
    private void lancerMenuPrincipal() {
        Intent menuPrincipal = new Intent(this, GestionFragment.class);
        startActivity(menuPrincipal);
        finish();
    }
}