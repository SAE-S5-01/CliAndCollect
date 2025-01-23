package fr.iutrodez.sae501.cliandcollect.requetes;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import fr.iutrodez.sae501.cliandcollect.ActivitePrincipale;
import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteInscription;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;

/**
 * Différentes méthodes de communication avec l'API.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class ClientApi {

    private static String BASE_URL;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream
             = ClientApi.class.getClassLoader()
                        .getResourceAsStream("assets/config.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                BASE_URL = properties.getProperty("BASE_URL");
            } else {
                throw new RuntimeException("Fichier de configuration absent");
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture du fichier de configuration", e);
        }
    }

    private static ProgressDialog spineurChargement;

    /**
     * Vérifie qu'une connexion internet est disponible.
     * @param context Le contexte de l'application
     * @return true si une connexion internet est disponible sinon false.
     */
    public static boolean reseauDisponible(Context context) {
        spineurChargement = new ProgressDialog(context);
        spineurChargement.setMessage(context.getString(R.string.attente_reseau));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

                spineurChargement.dismiss();
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        spineurChargement.dismiss();
        return false;
    }

    /**
     * Méthode permettant de générer les headers pour les requêtes à l'API.
     */
    private static Map<String, String> genererHeaders(String route) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String token = ActivitePrincipale.preferences.getString("tokenApi", "");
        if (!token.isEmpty() && !(route.equals("/utilisateur/connexion")
            || route.equals("/utilisateur/inscription"))) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
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
                    return genererHeaders(route);
                }
            };
        } else {
            // Utiliser StringRequest si aucun body JSON n'est fourni
            request = new StringRequest(methode, url, reussite, erreur) {
                @Override
                public Map<String, String> getHeaders() {
                    return genererHeaders(route);
                }
            };
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
    public static void connexion(Context contexte, String mail, String mdp, Runnable connexionReussie,
        Runnable erreurConnexion) {
        Map<String, String> parametre = new HashMap<>();

        parametre.put("mail", mail);
        parametre.put("motDePasse", mdp);

        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.attente_connexion));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.GET, "/utilisateur/connexion", parametre, null,
                response -> {
                    /*
                     * Pas de gestion propre de l'erreur car l'API retourne un code 401 en cas d'erreur
                     * On ne passera dans le catch que si le code de l'api venait a etre modifier en
                     * retirant le token obligatoire pour le reste de l'utilisation de l'application
                     * dans sa reponse
                     */
                    try {
                        spineurChargement.dismiss();

                        JSONObject jsonReponse = new JSONObject(response);
                        String token = jsonReponse.getString("token");
                        ActivitePrincipale.preferences.edit().putString("tokenApi", token).apply();
                        ((Activity) contexte).runOnUiThread(connexionReussie);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();

                    /* erreurConnexion ne sert que pour Activite principale */
                    if (erreurConnexion != null) {
                        ((Activity) contexte).runOnUiThread(erreurConnexion);
                    }
                    gestionErreur(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    /**
     * Méthode permettant de s'inscrire à l'API.
     * @param contexte Le contexte de l'application
     * @param donnees Les données de la requête (pour le body)
     * @param connexionReussie La méthode à appeler en cas de connexion réussie
     */
    public static void inscription(Context contexte, JSONObject donnees, Runnable connexionReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.attente_inscription));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.POST, "/utilisateur/inscription", null , donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();

                        JSONObject jsonReponse = new JSONObject(response);
                        String token = jsonReponse.getString("token");
                        ActivitePrincipale.preferences.edit().putString("tokenApi", token).apply();

                        Toast toast = Toast.makeText(contexte, R.string.inscription_reussie, Toast.LENGTH_LONG);
                        toast.show();

                        ((ActiviteInscription) contexte).runOnUiThread(connexionReussie);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } ,
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurInscription(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    public static void getListeClient(Context contexte , Runnable callback) {
        requeteApi(contexte, Request.Method.GET, "/contact", null, null,
                response -> {
                    try {
                        JSONArray jsonReponse = new JSONArray(response);

                        for (int i = 0; i < jsonReponse.length(); i++) {
                            JSONObject jsonClient = jsonReponse.getJSONObject(i);
                            Client client = new Client(jsonClient);
                            SingletonListeClient.getInstance().ajouterClient(client);
                        }
                        callback.run();
                    } catch (Exception e) {
                        Log.e("getListeClient", "Erreur lors du traitement de la réponse", e);
                    }
                },
                error -> {
                    Log.e("getListeClient", "Erreur reçue", error);
                    gestionErreur(contexte, error);
                }
        );
    }

    public static void creationClient(Context contexte, JSONObject donnees, Runnable creationReussie) {
        try {
            requeteApi(contexte, Request.Method.POST, "/contact", null , donnees,
                response -> {
                    try {
                        // En cas de succès, on ajoute le client au singleton pour faire l'affichage
                        JSONObject jsonReponse = new JSONObject(response);
                        ((ActiviteCreationClient) contexte).runOnUiThread(creationReussie);
                        Client clientCree = new Client(jsonReponse);
                        SingletonListeClient.getInstance().ajouterClient(clientCree);
                    } catch (Exception e) {
                        // TODO gestion erreur
                        Log.e("erreur", e.toString());
                        //throw new RuntimeException(e);
                    }
                } ,
                error -> {
                // TODO gestion erreur api
                    //gestionErreur(contexte, error);
                    Log.e("erreur", error.toString());
                }
            );
        } catch (Exception e) {
            Log.e("erreur", e.toString());
        }
    }


    public static void verifierAddresse(String adresse, double[] viewbox ,Context contexte, 
                                        VolleyCallback callback) throws UnsupportedEncodingException {
        String urlApi = "https://nominatim.openstreetmap.org/search?q="
            + URLEncoder.encode(adresse, "UTF-8")
            + "&countrycodes=fr&viewbox=" + viewbox[0] + "," + viewbox[1] + "," + viewbox[2] + "," + viewbox[3]
            + "&bounded=1&format=json&addressdetails=1";
        JsonArrayRequest requete = new JsonArrayRequest(
            Request.Method.GET,
            urlApi,
            null,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        // Liste pour stocker les résultats
                        List<Map<String , String>> results = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            Map<String, String> locationInfo = new HashMap<>();
                            locationInfo.put("display_name", jsonObject.getString("display_name"));
                            locationInfo.put("lat", jsonObject.getString("lat"));
                            locationInfo.put("lon", jsonObject.getString("lon"));
                            results.add(locationInfo);
                        }

                        // Retourner les résultats via le callback
                        callback.onSuccess(results);

                    } catch (JSONException e) {
                        // Gérer l'exception JSON
                        callback.onError("error catch : " + e.toString());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Retourner l'erreur via le callback
                    callback.onError(error.toString());
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "cliAndCollect/1.0 ");
                return headers;
            }
        };

        // Ajouter la requête à la file d'attente
        RequeteVolley.getInstance(contexte).ajoutFileRequete(requete);
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
                Toast.makeText(contexte, R.string.api_injoignable, Toast.LENGTH_LONG).show();
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
                Toast.makeText(contexte, R.string.api_injoignable, Toast.LENGTH_LONG).show();
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