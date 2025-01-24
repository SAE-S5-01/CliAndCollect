/*
 * Reseau.java                                                      24 jan. 2025
 * IUT de Rodez, pas de "copyright" ni de copyleft.
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

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
     * @param afficherToastSiErreur true pour afficher un toast en cas de réseau
     *                              indisponible, false sinon.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte, boolean afficherToastSiErreur) {
        return reseauDisponible(contexte, afficherToastSiErreur, R.string.erreur_reseau);
    }

    /**
     * Vérifie qu'une connexion internet est disponible et affiche un message d'erreur.
     * @param contexte Le contexte de l'application.
     * @param afficherToastSiErreur true pour afficher un toast en cas de réseau
     *                              indisponible, false sinon.
     * @param idMessageErreur L'identifiant du message d'erreur à afficher.
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context contexte, boolean afficherToastSiErreur,
                                           int idMessageErreur) {
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

        if (afficherToastSiErreur && !resultat) {
            Toast.makeText(contexte, idMessageErreur, Toast.LENGTH_LONG).show();
        }
        return resultat;
    }

}
