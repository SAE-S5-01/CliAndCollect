package fr.iutrodez.sae501.cliandcollect.activites;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.requetes.VolleyCallback;
import fr.iutrodez.sae501.cliandcollect.utile.Distance;
import fr.iutrodez.sae501.cliandcollect.utile.GestionBouton;
import fr.iutrodez.sae501.cliandcollect.utile.LocalisationAdapter;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;

/**
 * Activité de la page d'inscription.
 * @author Descriaud Lucas
 */
public class ActiviteInscription extends AppCompatActivity {

    final static int NB_MAX_ADRESSES_PROPOSEES = 7;

    private EditText mail;
    private EditText mdp;
    private EditText nom;
    private EditText prenom;
    private EditText adresse;
    private TextView messageErreur;
    private TextView messageErreurAdresse;

    public static SharedPreferences preferences;

    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;

    private CheckBox seRappelerDeMoi;

    private Button boutonObtenirCoordonnees;
    private Button boutonEditerAdresse;
    private Button boutonSubmitInscription;

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
        nom = findViewById(R.id.saisieNom);
        prenom = findViewById(R.id.saisiePrenom);
        adresse = findViewById(R.id.saisieAdresse);
        messageErreur = findViewById(R.id.messageErreur);
        messageErreurAdresse = findViewById(R.id.messageErreurAdresse);
        seRappelerDeMoi = findViewById(R.id.seRappelerDeMoi);
        boutonObtenirCoordonnees = findViewById(R.id.boutonObtenirCoordonnees);
        boutonEditerAdresse = findViewById(R.id.boutonEditerAdresse);
        boutonSubmitInscription = findViewById(R.id.boutonInscription);

        preferences = getDefaultSharedPreferences(getApplicationContext());

        setAdresseEditable(true);

        boutonEditerAdresse.setOnClickListener(v -> setAdresseEditable(true));
        boutonSubmitInscription.setOnClickListener(this::inscription);

        GestionBouton.activerBoutonSiTexteEntre(adresse, boutonObtenirCoordonnees);
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'inscription.
     * Récupère les informations saisies par l'utilisateur et les envoie à l'API.
     * Connecte l'utilisateur en cas d'informations valides
     * @param view Le bouton d'inscription
     *
     */
    private void inscription(View view) {
        masquerMessageErreur();

        JSONObject donnees = donneeFormulaireEnJson();
        if (Reseau.reseauDisponible(this, R.id.messageErreur, R.string.erreur_reseau) && donnees != null) {
            ClientApi.inscription(this, donnees, () -> {
                ActivitePrincipale
                .preferencesConnexion(seRappelerDeMoi.isChecked(),
                                      mail.getText().toString(),
                                      mdp.getText().toString());

                Intent menuPrincipal = new Intent(ActiviteInscription.this, GestionFragment.class);
                startActivity(menuPrincipal);
            });
        }
    }

    /**
     * Active ou désactive le mode édition pour le champ d'adresse.
     * @param editable True si le champ doit être éditable, false sinon.
     */
    private void setAdresseEditable(boolean editable) {
        adresse.setEnabled(editable);

        boutonEditerAdresse.setEnabled(!editable);
        boutonSubmitInscription.setEnabled(!editable);

        if (!adresse.getText().toString().isEmpty()) {
            boutonObtenirCoordonnees.setEnabled(editable);
        }
        boutonEditerAdresse.setVisibility(editable ? View.GONE : View.VISIBLE);
    }

    /**
     * Clic sur le bouton "Obtenir les coordonnées".
     * @param view Le bouton "Obtenir les coordonnées"
     */
    public void obtenirCoordonnees(View view) {
        masquerMessageErreur();

        String adresseEntree = adresse.getText().toString().trim();

        boolean reseauDisponible = Reseau.reseauDisponible(this, R.id.messageErreur, R.string.erreur_reseau);

        if (reseauDisponible && adresseEntree.isEmpty()) {
            afficherMessageErreur(R.string.erreur_adresse_vide, messageErreurAdresse);
        } else if (reseauDisponible) {
            try {
                // Création d'un "viewBox" (zone de recherche géographique) pour limiter la recherche à une zone spécifique
                double[] viewBox = Distance.creationViewBox(44.333333,2.566667);  // Coordonnées de base (Rodez)

                ClientApi.verifierAdresse(adresseEntree, viewBox, this, new VolleyCallback() {
                    @Override
                    public void onSuccess(List<Map<String, String>> resultats) {
                        gererAdressesProposees(resultats);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("Erreur API", "Problème lors de la récupération des coordonnées : " + error);
                        afficherMessageErreur(R.string.api_injoignable, messageErreur);
                        boutonSubmitInscription.setEnabled(false);
                    }
                });
            } catch (Exception e) {
                Log.e("Exception", "Erreur lors de l'obtention des coordonnées : " + e);
                afficherMessageErreur(R.string.erreur_inconnue, messageErreur);
                boutonSubmitInscription.setEnabled(false);
            }
        }
    }

    /**
     * Gère les adresses proposées par l'API.
     * @param resultats Liste des adresses proposées par l'API
     */
    private void gererAdressesProposees(List<Map<String, String>> resultats) {
        if (resultats == null || resultats.isEmpty()) {
            boutonSubmitInscription.setEnabled(false);
            afficherMessageErreur(R.string.adresse_non_trouvee, messageErreurAdresse);
        } else {
            ArrayList<String> options = new ArrayList<>();
            for (int i = 0;
                 i < resultats.size() && i < NB_MAX_ADRESSES_PROPOSEES;
                 i++) {
                options.add(resultats.get(i).get("display_name"));
            }

            AlertDialog.Builder builder
            = new AlertDialog.Builder(ActiviteInscription.this);
            builder.setTitle(R.string.titre_choix_adresse);

            LocalisationAdapter adapter
            = new LocalisationAdapter(ActiviteInscription.this,
                                      options);

            builder.setAdapter(adapter, (dialog, which) -> {
                Map<String, String> selectedLocation = resultats.get(which);
                String lat = selectedLocation.get("lat");
                String lon = selectedLocation.get("lon");

                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);

                adresse.setText(selectedLocation.get("display_name"));
                setAdresseEditable(false);
                messageErreurAdresse.setText("");
                dialog.dismiss();
            });

            builder.show();
        }
    }

    /**
     * Récupère les informations saisies par l'utilisateur et les transforme en objet JSON.
     * @return Les informations saisies par l'utilisateur sous forme d'objet JSON.
     */
    private JSONObject donneeFormulaireEnJson() {
        JSONObject donnees = new JSONObject();
        try {
            donnees.put("mail", mail.getText().toString());
            donnees.put("motDePasse", mdp.getText().toString());
            donnees.put("nom", nom.getText().toString());
            donnees.put("prenom", prenom.getText().toString());
            donnees.put("adresse", adresse.getText().toString());
            donnees.put("latitude", latitude);
            donnees.put("longitude", longitude);
        } catch (Exception e) {
            afficherMessageErreur(e.getMessage().equals("Forbidden numeric value: NaN")
                                  ? R.string.coordonnees_non_calculees
                                  : R.string.erreur_inscription,
                                  messageErreur);
            donnees = null;
        }
        return donnees;
    }

    /**
     * Affiche un message d'erreur global.
     * @param message L'id du message d'erreur à afficher.
     * @param entreeMessageErreur L'entrée de texte où afficher le message d'erreur.
     */
    private void afficherMessageErreur(int message, TextView entreeMessageErreur) {
        entreeMessageErreur.setVisibility(View.VISIBLE);
        entreeMessageErreur.setText(message);
    }

    /**
     * Masque le message d'erreur global.
     */
    private void masquerMessageErreur() {
        messageErreur.setVisibility(View.GONE);
        messageErreur.setText("");
    }
}