package fr.iutrodez.sae501.cliandcollect.requetes;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteConnexion;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteInscription;

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

    /**
     * Méthode permettant de faire une requête à l'API.
     * @param contexte Le contexte de l'application
     * @param methode La méthode de la requête (GET, POST, PUT, DELETE)
     * @param route La route de l'API
     * @param parametres Les paramètres de la requête (pour du GET / DELETE)
     * @param donnees Les données de la requête (pour le body)
     * @param reussite La méthode à appeler en cas de réussite
     * @param erreur La méthode à appeler en cas d'erreur
     */
    public static void requeteApi(Context contexte, int methode, String route, Map<String, String> parametres,
                                  JSONObject donnees, Response.Listener<String> reussite, Response.ErrorListener erreur) {
        // Construire l'URL complète en ajoutant la route
        String url = BASE_URL + route;

        // Gestion des paramètres pour les requêtes GET
        if (methode == Request.Method.GET && parametres != null && !parametres.isEmpty()) {
            StringBuilder urlACompleter = new StringBuilder(url);
            urlACompleter.append("?"); // Début de la query string

            for (Map.Entry<String, String> parametre : parametres.entrySet()) {
                try {
                    // Encodage des clés et valeurs des paramètres pour les rendre compatibles avec l'URL
                    urlACompleter.append(URLEncoder.encode(parametre.getKey(), "UTF-8")) // Encode la clé
                            .append("=")
                            .append(URLEncoder.encode(parametre.getValue(), "UTF-8")) // Encode la valeur
                            .append("&"); // Ajoute un '&' pour séparer les paramètres
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // Supprimer le dernier '&' ajouté
            url = urlACompleter.substring(0, urlACompleter.length() - 1);
        }

        // Déterminer le type de requête
        Request request;
        if (donnees != null) {
            // Utiliser JsonObjectRequest si un body JSON est présent
            request = new JsonObjectRequest(methode, url, donnees, response -> reussite.onResponse(response.toString()), erreur) {
                @Override
                public Map<String, String> getHeaders() {
                    // Ajouter des headers si nécessaire
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
        } else {
            // Utiliser StringRequest si aucun body JSON n'est fourni
            request = new StringRequest(methode, url, reussite, erreur);
        }
        // Ajouter la requête à la file d'attente
        RequeteVolley.getInstance(contexte).ajoutFileRequete(request);
    }


    /**
     * Méthode permettant de se connecter à l'API.
     * @param contexte Le contexte de l'application
     * @param mail L'adresse mail de l'utilisateur
     * @param mdp Le mot de passe de l'utilisateur
     * @param connexionReussie La méthode à appeler en cas de connexion réussie
     */
    public static void connexion(Context contexte, String mail, String mdp, Runnable connexionReussie ,
    Runnable erreurConnexion) {
        Map<String, String> parametre = new HashMap<>();
        parametre.put("mail", mail);
        parametre.put("motDePasse", mdp);
        Toast toast = Toast.makeText(contexte, R.string.attente_connexion, Toast.LENGTH_LONG);
        try {
            requeteApi(contexte, Request.Method.GET, "/api/connexion", parametre, null,
                response -> {
                    /*
                     * Pas de gestion propre de l'erreur car l'API retourne un code 401 en cas d'erreur*
                     * On ne passera dans le catch que si le code de l'api venait a etre modifier en
                     * retirant le token obligatoire pour le reste de l'utilisation de l'application
                     * dans sa reponse
                     */
                    try {
                        JSONObject jsonReponse = new JSONObject(response);
                        String token = jsonReponse.getString("token");
                        toast.cancel();
                        ActivitePrincipale.preferences.edit().putString("tokenApi", token).apply();
                        ((Activity) contexte).runOnUiThread(connexionReussie);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    /* erreurConnexion ne sert que pour Activite principale*/
                    if(erreurConnexion != null) {
                        ((Activity) contexte).runOnUiThread(erreurConnexion);
                    }
                    toast.cancel();
                    gestionErreur(contexte, error);
                }
            );
        } catch (Exception e) {
        }
    }

    /**
     * Méthode permettant de s'inscrire à l'API.
     * @param contexte Le contexte de l'application
     * @param donnees Les données de la requête (pour le body)
     * @param connexionReussie La méthode à appeler en cas de connexion réussie
     */
    public static void inscription(Context contexte, JSONObject donnees, Runnable connexionReussie) {
        Toast toast = Toast.makeText(contexte, R.string.attente_inscription, Toast.LENGTH_LONG);
        try {
            requeteApi(contexte, Request.Method.POST, "/api/inscription", null , donnees,
                response -> {
                    try {
                        JSONObject jsonReponse = new JSONObject(response);
                        String token = jsonReponse.getString("token");
                        ActivitePrincipale.preferences.edit().putString("tokenApi", token).apply();
                        toast.cancel();
                        toast.makeText(contexte, R.string.inscription_reussie, Toast.LENGTH_LONG).show();
                        ((ActiviteInscription) contexte).runOnUiThread(connexionReussie);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } ,
                error -> {
                    toast.cancel();
                    gestionErreurInscription(contexte, error);
                }
            );
        } catch (Exception e) {
        }
    }

    /**
     * Méthode permettant de gérer les erreurs lors de la communication avec l'API.
     * @param contexte Le contexte de l'application
     * @param erreur L'erreur retournée par l'API
     */
    private static void gestionErreur(Context contexte, VolleyError erreur) {
        if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
            ((Activity) contexte).runOnUiThread(() -> {
                Toast.makeText(contexte, new String(erreur.networkResponse.data), Toast.LENGTH_LONG).show();
            });
        } else {
            ((Activity) contexte).runOnUiThread(() -> {
                Toast.makeText(contexte, R.string.api_injoignable ,  Toast.LENGTH_LONG).show();
            });
        }
    }

    /**
     * Méthode permettant de gérer les erreurs lors de l'inscription.
     * @param contexte Le contexte de l'application
     * @param erreur L'erreur retournée par l'API
     */
    private static void gestionErreurInscription(Context contexte, VolleyError erreur) {
        if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
            try {
                String responseBody = new String(erreur.networkResponse.data, "UTF-8");
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject erreurs = jsonResponse.getJSONObject("erreur");
                JSONObject saisie = jsonResponse.getJSONObject("saisie");
                ((ActiviteInscription) contexte).runOnUiThread(() -> {
                    remplirChampsInscription((ActiviteInscription) contexte, saisie);
                    afficherErreursInscription((ActiviteInscription) contexte, erreurs);
                });
            } catch (Exception e) {
            }
        } else {
            ((ActiviteInscription) contexte).runOnUiThread(() -> {
                Toast.makeText(contexte, R.string.api_injoignable ,  Toast.LENGTH_LONG).show();
            });
        }
    }

    /**
     * Méthode permettant de remplir les champs du formulaire d'inscription.
     * @param activite L'activité d'inscription
     * @param saisie Les données saisies par l'utilisateur
     */
    private static void remplirChampsInscription(ActiviteInscription activite, JSONObject saisie) {
        try {
            EditText mail = activite.findViewById(R.id.saisieMail);
            EditText mdp = activite.findViewById(R.id.saisieMdp);
            EditText nom = activite.findViewById(R.id.saisieNom);
            EditText prenom = activite.findViewById(R.id.saisiePrenom);
            EditText adresse = activite.findViewById(R.id.saisieAdresse);
            EditText ville = activite.findViewById(R.id.saisieVille);

            if (saisie.has("mail")) {
                mail.setText(saisie.getString("mail"));
            }
            if (saisie.has("motDePasse")) {
                mdp.setText(saisie.getString("motDePasse"));
            }
            if (saisie.has("nom")) {
                nom.setText(saisie.getString("nom"));
            }
            if (saisie.has("prenom")) {
                prenom.setText(saisie.getString("prenom"));
            }
            if (saisie.has("adresse")) {
                adresse.setText(saisie.getString("adresse"));
            }
            if (saisie.has("ville")) {
                ville.setText(saisie.getString("ville"));
            }
        } catch (JSONException e) {
        }
    }

    /**
     * Méthode permettant d'afficher les erreurs lors de l'inscription.
     * @param activite L'activité d'inscription
     * @param erreurs Les erreurs retournées par l'API
     */
    private static void afficherErreursInscription(ActiviteInscription activite, JSONObject erreurs) {
        try {
            TextView messageErreurMail = activite.findViewById(R.id.messageErreurMail);
            TextView messageErreurMdp = activite.findViewById(R.id.messageErreurMdp);
            TextView messageErreurNom = activite.findViewById(R.id.messageErreurNom);
            TextView messageErreurPrenom = activite.findViewById(R.id.messageErreurPrenom);
            TextView messageErreurAdresse = activite.findViewById(R.id.messageErreurAdresse);
            TextView messageErreurVille = activite.findViewById(R.id.messageErreurVille);
            messageErreurMail.setText("");
            messageErreurMdp.setText("");
            messageErreurNom.setText("");
            messageErreurPrenom.setText("");
            messageErreurAdresse.setText("");
            messageErreurVille.setText("");
            if (erreurs.has("mail")) {
                messageErreurMail.setText(erreurs.getString("mail"));
            }
            if (erreurs.has("motDePasse")) {
                messageErreurMdp.setText(erreurs.getString("motDePasse"));
            }
            if (erreurs.has("nom")) {
                messageErreurNom.setText(erreurs.getString("nom"));
            }
            if (erreurs.has("prenom")) {
                messageErreurPrenom.setText(erreurs.getString("prenom"));
            }
            if (erreurs.has("adresse")) {
                messageErreurAdresse.setText(erreurs.getString("adresse"));
            }
            if (erreurs.has("ville")) {
                messageErreurVille.setText(erreurs.getString("ville"));
            }
        } catch (JSONException e) {
        }
    }
}