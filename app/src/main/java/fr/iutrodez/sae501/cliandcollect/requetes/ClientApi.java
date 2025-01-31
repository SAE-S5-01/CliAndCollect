/*
 * ClientApi.java                                                   24 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.requetes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
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
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationItineraire;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteInscription;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Différentes méthodes de communication avec l'API.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class ClientApi {

    private final static String CHEMIN_FICHIER_CONFIGURATION
        = "assets/config.properties";

    private static String BASE_URL;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream
             = ClientApi.class.getClassLoader()
                        .getResourceAsStream(CHEMIN_FICHIER_CONFIGURATION)) {
            if (inputStream != null) {
                properties.load(inputStream);
                BASE_URL = properties.getProperty("BASE_URL");
            } else {
                throw new RuntimeException("Fichier de configuration illisible : "
                                           + CHEMIN_FICHIER_CONFIGURATION);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture du fichier de configuration :"
                                       + CHEMIN_FICHIER_CONFIGURATION, e);
        }
    }

    private static ProgressDialog spineurChargement;

    /**
     * Méthode permettant de générer les headers pour les requêtes à l'API.
     */
    private static Map<String, String> genererHeaders(String route, Context contexte) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String token = Preferences.getTokenApi(contexte);
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
        if ((methode == Request.Method.GET || methode == Request.Method.PUT) && parametres != null && !parametres.isEmpty()) {
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
                    return genererHeaders(route, contexte);
                }
            };
        } else {
            // Utiliser StringRequest si aucun body JSON n'est fourni
            request = new StringRequest(methode, url, reussite, erreur) {
                @Override
                public Map<String, String> getHeaders() {
                    return genererHeaders(route, contexte);
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
    public static void connexion(Context contexte, String mail, String mdp, Runnable connexionReussie) {
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
                        Preferences.sauvegarderTokenApi(contexte, token);
                        ((Activity) contexte).runOnUiThread(connexionReussie);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
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
                        Preferences.sauvegarderTokenApi(contexte, token);

                        ((ActiviteInscription) contexte).runOnUiThread(connexionReussie);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
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

    public static void getListeItineraire(Context contexte , Runnable callback) {
        requeteApi(contexte, Request.Method.GET, "/itineraire", null, null,
                response -> {
                    try {
                        JSONArray jsonReponse = new JSONArray(response);

                        for (int i = 0; i < jsonReponse.length(); i++) {
                            JSONObject jsonItineraire = jsonReponse.getJSONObject(i);
                            Itineraire itineraire = new Itineraire(jsonItineraire);
                            SingletonListeItineraire.getInstance().ajouterItineraire(itineraire);
                        }
                        callback.run();
                    } catch (Exception e) {
                        Log.e("getListeItineraire", "Erreur lors du traitement de la réponse", e);
                    }
                },
                error -> {
                    Log.e("getListeItineraire", "Erreur reçue", error);
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

    public static void creationItineraire(Context contexte, JSONObject donnees, Runnable creationReussie) {
        try {
            requeteApi(contexte, Request.Method.POST, "/itineraire", null , donnees,
                    response -> {
                        try {
                            // En cas de succès, on ajoute l'itinéraire au singleton pour faire l'affichage
                            JSONObject jsonReponse = new JSONObject(response);
                            ((ActiviteCreationItineraire) contexte).runOnUiThread(creationReussie);
                            Itineraire itineraireCree = new Itineraire(jsonReponse);
                            SingletonListeItineraire.getInstance().ajouterItineraire(itineraireCree);
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

    public static void modificationClient(Context contexte, JSONObject donnees, String id) {
        HashMap<String,String> parametre = new HashMap<>();
        parametre.put("id", id);

        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.attente_chargement));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.PUT, "/contact", parametre , donnees,
                response -> {
                    spineurChargement.dismiss(); },
                error -> {
                    spineurChargement.dismiss();
                    gestionErreur(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
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
                try {
                    String erreurToString = new String(erreur.networkResponse.data, "UTF-8");
                    JSONObject objetErreur = new JSONObject(erreurToString);

                    SnackbarCustom.show(contexte, objetErreur.getString("description"), SnackbarCustom.STYLE_ERREUR);
                } catch (Exception e) {
                    Toast.makeText(contexte, R.string.erreur_inconnue, Toast.LENGTH_LONG);
                    throw new RuntimeException(e);
                }
            });
        } else {
            ((Activity) contexte).runOnUiThread(() -> {
                SnackbarCustom.show(contexte, R.string.api_injoignable, SnackbarCustom.STYLE_ERREUR);
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

                ((ActiviteInscription) contexte).runOnUiThread(() -> {
                    afficherErreursInscription((ActiviteInscription) contexte, erreurs, new int[] {
                        R.id.saisieMail, R.id.saisieMdp, R.id.saisieNom,
                        R.id.saisiePrenom, R.id.saisieAdresse
                    }, new String[] {
                        "mail", "motDePasse", "nom", "prenom", "adresse"
                    });
                });
            } catch (Exception e) {
                Toast.makeText(contexte, R.string.erreur_inconnue, Toast.LENGTH_LONG);
                throw new RuntimeException(e);
            }
        } else {
            ((ActiviteInscription) contexte).runOnUiThread(() -> {
                SnackbarCustom.show(contexte, R.string.api_injoignable, SnackbarCustom.STYLE_ERREUR);
            });
        }
    }

    /**
     * Afficher les erreurs lors de l'inscription.
     * @param activite L'activité d'inscription
     * @param erreurs Les erreurs retournées par l'API
     * @param idChampsTextuels Les identifiants des inputs des champs textuels
     * @param cleChampsTextuels Les clés de réponse de l'API des champs textuels
     */
    private static void afficherErreursInscription(ActiviteInscription activite, JSONObject erreurs,
                                                   int[] idChampsTextuels, String [] cleChampsTextuels) {
        for (int i = 0; i < idChampsTextuels.length; i++) {
            try {
                EditText champ = activite.findViewById(idChampsTextuels[i]);
                if (erreurs.has(cleChampsTextuels[i])) {
                    champ.setError(erreurs.getString(cleChampsTextuels[i]));
                }
            } catch (JSONException e) {
            }
        }
    }
}