package fr.iutrodez.sae501.cliandcollect.requetes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import fr.iutrodez.sae501.cliandcollect.activites.ActiviteConnexion;
import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Classe définissant les différentes méthodes pour communiquer avec l'API.
 * @author Descriaud Lucas
 */
public class ClientApi {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    /**
     * Méthode permettant de verifier si l'API est joignable.
     * @param context Le contexte de l'application
     */

    /**
     * Vérifie qu'une connexion internet est disponible.
     * @param context Le contexte de l'application
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return false;
    }

    public static void connexion(Context context, String mail, String mdp , Runnable connexionReussie) {
        String url = BASE_URL + "/api/connexion?mail=" + mail + "&motDePasse=" + mdp;

        ((ActiviteConnexion) context).runOnUiThread(connexionReussie);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String reponse) {
                        Log.d("API : ", reponse.toString());
                        ((ActiviteConnexion) context).runOnUiThread(connexionReussie);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // L'api ne repond pas on affiche a l'utilisateur sur la page de connexion
                Log.e("url : " , url);
                Log.e("API : " , error.toString());
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    ((PageConnexion) context).runOnUiThread(() -> {
                        TextView messageErreur = ((PageConnexion) context).findViewById(R.id.messageErreur);
                        messageErreur.setText(new String(error.networkResponse.data));
                    });
                } else {
                    ((PageConnexion) context).runOnUiThread(() -> {
                        TextView messageErreur = ((PageConnexion) context).findViewById(R.id.messageErreur);
                        messageErreur.setText(R.string.apiInjoignable);
                    });
                }
            }
        });
        RequeteVolley.getInstance(context).ajoutFileRequete(stringRequest);
    }
}