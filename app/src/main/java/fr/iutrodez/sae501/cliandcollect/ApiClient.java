package fr.iutrodez.sae501.cliandcollect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Classe définissant les différentes méthodes pour communiquer avec l'API.
 * @author Descriaud Lucas
 */
public class ApiClient {

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
    public static void apijoignable(Context context) {
        String url = BASE_URL + "/api/apijoignable";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String reponse) {
                        Log.d("API : ", reponse.toString());
                        ((PageConnexion) context).runOnUiThread(() -> {
                            Button boutonConnexion = ((PageConnexion) context).findViewById(R.id.boutonConnexion);
                            Button boutonInscription = ((PageConnexion) context).findViewById(R.id.boutonInscription);
                            boutonConnexion.setEnabled(true);
                            boutonInscription.setEnabled(true);
                        });
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // L'api ne repond pas on affiche a l'utilisateur sur la page de connexion
                // TODO : afficher un message d'erreur
                Log.e("API : ", error.toString());
                ((PageConnexion) context).runOnUiThread(() -> {
                    TextView messageErreur = ((PageConnexion) context).findViewById(R.id.messageErreur);
                    messageErreur.setText(R.string.apiInjoignable);
                });
            }
        });

        RequeteVolley.getInstance(context).ajoutFileRequete(stringRequest);
    }

    public static void connexion(Context context, String mail, String mdp , Runnable connexionReussie) {
        String url = BASE_URL + "/api/connexion?mail=" + mail + "&motDePasse=" + mdp;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String reponse) {
                        Log.d("API : ", reponse.toString());
                            ((PageConnexion) context).runOnUiThread(connexionReussie);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // L'api ne repond pas on affiche a l'utilisateur sur la page de connexion
                Log.e("url : " , url);
                Log.e("API : " , error.toString());
                ((PageConnexion) context).runOnUiThread(() -> {
                    TextView messageErreur = ((PageConnexion) context).findViewById(R.id.messageErreur);
                    messageErreur.setText(new String(error.networkResponse.data));
                });
            }
        });

        RequeteVolley.getInstance(context).ajoutFileRequete(stringRequest);
    }


}