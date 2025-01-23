/*
 * ActivitePrincipale.java                                                27 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteConnexion;
import fr.iutrodez.sae501.cliandcollect.fragments.GestionFragment;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;


/**
 * Activité principale et point d'entrée de l'application.
 * @author Descriaud Lucas
 */
public class ActivitePrincipale extends AppCompatActivity {


    public static SharedPreferences preferences;

    private Intent activiteALancer;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getDefaultSharedPreferences(getApplicationContext());

        /*
         * Vérification uniquement du mail car le mail n'est jamais écrit sans le mot de passe.
         * Si les valeurs n'ont pas changé dans la bd de l'api on redirige vers la vue principale
         * Dans n'importe qu'elle autre cas on redirige vers la vue de connexion.
         */
        if (ClientApi.reseauDisponible(this) && preferences.contains("mail")) {
            ClientApi.connexion(this,
                preferences.getString("mail", ""),
                preferences.getString("mdp", ""),
                () -> { lancementActivite(GestionFragment.class); },
                () -> { lancementActivite(ActiviteConnexion.class); }
            );
        } else {
            activiteALancer = new Intent(this, ActiviteConnexion.class);
            startActivity(activiteALancer);
            finish();
        }
    }

    /**
     * Méthode permettant de lancer une activité.
     * @param activite L'activité à lancer
     */
    private void lancementActivite(Class<?> activite) {
        Intent intent = new Intent(this, activite);
        startActivity(intent);
        finish();
    }

    /**
     * Méthode permettant de gérer les préférences de connexion.
     * @param seRappelerdeMoi Si l'utilisateur souhaite être rappelé
     * @param mail Le mail de l'utilisateur
     * @param mdp Le mot de passe de l'utilisateur
     */
    public static void preferencesConnexion(boolean seRappelerdeMoi, String mail, String mdp) {
        if (seRappelerdeMoi) {
            preferences.edit().putString("mail", mail).apply();
            preferences.edit().putString("mdp", mdp).apply();
        } else if (preferences.contains("mail")) {
            preferences.edit().remove("mail").apply();
            preferences.edit().remove("mdp").apply();
        }
    }
}