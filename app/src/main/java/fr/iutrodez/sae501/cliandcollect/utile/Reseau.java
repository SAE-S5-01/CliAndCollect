/*
 * Reseau.java                                                      24 jan. 2025
 * IUT de Rodez, pas de "copyright" ni de copyleft.
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Gestion du réseau et des connexions internet.
 *
 * @author Loïc FAUGIERES
 */
public class Reseau {

    /**
     * Vérifie qu'une connexion internet est disponible et affiche une erreur générique.
     * @param contexte Le contexte de l'application.
     * @param afficherSnackbarSiErreur true pour afficher un snackbar en cas de réseau
     *                                 indisponible, false sinon.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte, boolean afficherSnackbarSiErreur) {
        return reseauDisponible(contexte, afficherSnackbarSiErreur, R.string.erreur_reseau);
    }

    /**
     * Vérifie qu'une connexion internet est disponible et affiche un toast d'erreur.
     * @param contexte Le contexte de l'application.
     * @param afficherSnackbarSiErreur true pour afficher un snackbar en cas de réseau
     *                                 indisponible, false sinon.
     * @param idMessageErreur L'identifiant du message d'erreur à afficher.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte, boolean afficherSnackbarSiErreur,
                                           int idMessageErreur) {
        if (!reseauDisponible(contexte)) {
            if (afficherSnackbarSiErreur) {
                SnackbarCustom.show(contexte, idMessageErreur, SnackbarCustom.STYLE_ATTENTION);
            }
            return false;
        }
        return true;
    }

    /**
     * Vérifie qu'une connexion internet est disponible et affiche un message d'erreur.
     * @param contexte Le contexte de l'application.
     * @param idEntreeMessageErreur L'identifiant de l'entrée de texte où afficher le message d'erreur.
     * @param idMessageErreur L'identifiant du message d'erreur à afficher.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte, int idEntreeMessageErreur,
                                           int idMessageErreur) {
        if (!reseauDisponible(contexte)) {
            Activity activite = (Activity) contexte;
            TextView vue = activite.findViewById(idEntreeMessageErreur);

            if (vue != null) {
                vue.setVisibility(View.VISIBLE);
                vue.setText(idMessageErreur);
            }
            return false;
        }
        return true;
    }

    /**
     * Vérifie qu'une connexion internet est disponible.
     * @param contexte Le contexte de l'application.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte) {
        boolean resultat = false;

        ProgressDialog spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.attente_reseau));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        ConnectivityManager cm = (ConnectivityManager) contexte.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                resultat = capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        spineurChargement.dismiss();

        return resultat;
    }

}
