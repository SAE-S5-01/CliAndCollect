/*
 * Preferences.java                                                 29 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Gestions des préférences de l'application.
 *
 * @author Loïc FAUGIERES
 */
public class Preferences {

    private static SharedPreferences preferencesApplication;

    /**
     * Initialisation des préférences de l'application.
     * @param context Contexte de l'application.
     */
    private static void init(Context context) {
        if (preferencesApplication == null) {
            preferencesApplication = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
    }

    /**
     * Vérifie si l'utilisateur est connecté.
     * @param context Contexte de l'application.
     * @return true si l'utilisateur est connecté, false sinon.
     */
    public static boolean estConnecte(Context context) {
        init(context);
        return preferencesApplication.contains("mail");
    }

    /**
     * Récupérer l'email de l'utilisateur.
     * @param context Contexte de l'application.
     * @return L'email de l'utilisateur.
     */
    public static String getEmail(Context context) {
        init(context);
        return preferencesApplication.getString("mail", "");
    }

    /**
     * Récupérer le mot de passe de l'utilisateur.
     * @param context Contexte de l'application.
     * @return Le mot de passe de l'utilisateur.
     */
    public static String getMotDePasse(Context context) {
        init(context);
        return preferencesApplication.getString("mdp", "");
    }

    /**
     * Réccupérer le token API.
     * @param context Contexte de l'application.
     * @return Le token API.
     */
    public static String getTokenApi(Context context) {
        init(context);
        return preferencesApplication.getString("tokenApi", "");
    }

    /**
     * Sauvegarder les informations de connexion.
     * @param context Contexte de l'application.
     * @param mail L'email de l'utilisateur.
     * @param mdp Le mot de passe de l'utilisateur.
     * @param seRappelerdeMoi Si l'utilisateur souhaite être rappelé.
     */
    public static void sauvegarderInfosConnexion(Context context, String mail, String mdp, boolean seRappelerdeMoi) {
        init(context);
        SharedPreferences.Editor editor = preferencesApplication.edit();
        if (seRappelerdeMoi) {
            editor.putString("mail", mail);
            editor.putString("mdp", mdp);
        } else {
            editor.remove("mail");
            editor.remove("mdp");
        }
        editor.apply();
    }

    /**
     * Sauvegarder le token API.
     * @param context Contexte de l'application.
     * @param tokenApi Le token API.
     */
    public static void sauvegarderTokenApi(Context context, String tokenApi) {
        init(context);
        SharedPreferences.Editor editor = preferencesApplication.edit();
        editor.putString("tokenApi", tokenApi);
        editor.apply();
    }

    /**
     * Effacer les informations de connexion.
     * @param context Contexte de l'application.
     */
    public static void effacerInfosConnexion(Context context) {
        init(context);
        SharedPreferences.Editor editeur = preferencesApplication.edit();
        editeur.remove("mail");
        editeur.remove("mdp");
        editeur.remove("tokenApi");
        editeur.apply();
    }

}
