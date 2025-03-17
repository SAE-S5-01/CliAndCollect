/*
 * ClientApi.java                                                   24 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.requetes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationItineraire;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailItineraire;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteGestionCompte;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteInscription;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.PointGPS;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.utile.Compte;
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
            request = new JsonRequest<String>(methode, url, donnees.toString(),
                response -> gestionReponseRequeteAvecDonnees(response, reussite, erreur),
                erreur) {
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    try {
                        String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
                    } catch (UnsupportedEncodingException e) {
                        return Response.error(new ParseError(e));
                    }
                }

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
     * Gestion d'une réponse de l'API à une requête contenant des données.
     * @param response La réponse de l'API
     * @param reussite La méthode à appeler en cas de réussite
     * @param erreur La méthode à appeler en cas d'erreur
     */
    private static void gestionReponseRequeteAvecDonnees(String response,
                                                  Response.Listener<String> reussite,
                                                  Response.ErrorListener erreur) {
        try {
            // Vérifier si la réponse est un JSONArray ou un JSONObject
            if (response.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(response);
                reussite.onResponse(jsonArray.toString());
            } else {
                JSONObject jsonObject = new JSONObject(response);
                reussite.onResponse(jsonObject.toString());
            }
        } catch (JSONException e) {
            erreur.onErrorResponse(new VolleyError("Réponse invalide", e));
        }
    }

    /**
     * Méthode permettant de se connecter à l'API.
     * @param contexte Le contexte de l'application
     * @param mail L'adresse mail de l'utilisateur
     * @param mdp Le mot de passe de l'utilisateur
     * @param connexionReussie La méthode à appeler en cas de connexion réussie
     */
    public static void connexion(Context contexte, String mail, String mdp, Runnable connexionReussie, Runnable connexionEchouee) {
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
                        String longitude = jsonReponse.getString("x");
                        String latitude = jsonReponse.getString("y");
                        Preferences.sauvegarderTokenApi(contexte, token);
                        Preferences.sauvegarderCoordonnees(contexte, latitude, longitude);
                        ((Activity) contexte).runOnUiThread(connexionReussie);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    if (connexionEchouee != null) {
                        connexionEchouee.run();
                    }
                    gestionErreur(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    /**
     * Récupère les informations du compte de l'utilisateur.
     * @param contexte Le contexte de l'application
     * @param getReussi La méthode à appeler en cas de récupération réussie
     * @param getEchoue La méthode à appeler en cas de récupération échouée
     */
    public static void getCompte(Context contexte, Runnable getReussi, Runnable getEchoue) {
        requeteApi(contexte, Request.Method.GET, "/utilisateur", null, null,
            response -> {
                try {
                    JSONObject jsonReponse = new JSONObject(response);
                    // On retire le mot de passe qui est ici haché (donc inutile)
                    jsonReponse.remove("motDePasse");
                    new Compte(jsonReponse);

                    ((ActiviteGestionCompte) contexte).runOnUiThread(getReussi);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            error -> {
                ((ActiviteGestionCompte) contexte).runOnUiThread(getEchoue);
            }
        );
    }

    /**
     * Méthode permettant de s'inscrire à l'API.
     * @param contexte Le contexte de l'application
     * @param donnees Les données de la requête (pour le body)
     * @param inscriptionReussie La méthode à appeler en cas d'inscription réussie
     */
    public static void inscription(Context contexte, JSONObject donnees, Runnable inscriptionReussie) {
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

                        ((ActiviteInscription) contexte).runOnUiThread(inscriptionReussie);
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

    /**
     * Méthode permettant de modifier ses données personnelles.
     * @param contexte Le contexte de l'application
     * @param donnees Les données de la requête (pour le body)
     * @param modificationReussie La méthode à appeler en cas de modification réussie
     */
    public static void modifierCompte(Context contexte, JSONObject donnees, Runnable modificationReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_modification));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.PUT, "/utilisateur", null, donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();

                        JSONObject jsonReponse = new JSONObject(response);
                        String token = jsonReponse.getString("token");
                        Preferences.sauvegarderTokenApi(contexte, token);

                        ((ActiviteGestionCompte) contexte).runOnUiThread(modificationReussie);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurModificationCompte(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    /**
     * Méthode permettant de récupérer la liste des clients depuis l'API.
     * @param contexte Le contexte de l'application
     * @param callback La méthode à appeler en cas de succès
     */
    public static void getListeClient(Context contexte , Runnable callback) {
        requeteApi(contexte, Request.Method.GET, "/contact", null, null,
            response -> {
                try {
                    JSONArray jsonReponse = new JSONArray(response);

                    SingletonListeClient.getInstance().viderListeClient();
                    for (int i = 0; i < jsonReponse.length(); i++) {
                        JSONObject jsonClient = jsonReponse.getJSONObject(i);
                        Client client = new Client(jsonClient);
                        SingletonListeClient.getInstance().ajouterClient(client);
                    }

                    callback.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            error -> {
                gestionErreur(contexte, error);
            }
        );
    }

    /**
     * Méthode permettant de récupérer la liste des itinéraires depuis l'API.
     * @param contexte Le contexte de l'application
     * @param callback La méthode à appeler en cas de succès
     */
    public static void getListeItineraires(Context contexte , Runnable callback) {
        requeteApi(contexte, Request.Method.GET, "/itineraire", null, null,
            response -> {
                try {
                    JSONArray jsonReponse = new JSONArray(response);

                    SingletonListeItineraire.getInstance().viderListeItineraire();
                    for (int i = 0; i < jsonReponse.length(); i++) {
                        JSONObject jsonItineraire = jsonReponse.getJSONObject(i);
                        Itineraire itineraire = new Itineraire(jsonItineraire);
                        SingletonListeItineraire.getInstance().ajouterItineraire(itineraire);
                    }

                    callback.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            error -> {
                gestionErreur(contexte, error);
            }
        );
    }

    public static void creationClient(Context contexte, JSONObject donnees, Runnable creationReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_ajout));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.POST, "/contact", null, donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();
                        // En cas de succès, on ajoute le client au singleton pour faire l'affichage
                        JSONObject jsonReponse = new JSONObject(response);
                        ((ActiviteCreationClient) contexte).runOnUiThread(creationReussie);
                        Client clientCree = new Client(jsonReponse);
                        SingletonListeClient.getInstance().ajouterClient(clientCree);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurClient(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    public static void modificationClient(Context contexte, JSONObject donnees, String id, Runnable modificationReussie) {
        HashMap<String,String> parametre = new HashMap<>();
        parametre.put("id", id);

        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_modification));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.PUT, "/contact", parametre, donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();
                        JSONArray jsonReponse = new JSONArray(response);

                        for (int i = 0; i < jsonReponse.length(); i++) {
                            SingletonListeItineraire.getInstance()
                            .supprimerItineraire(jsonReponse.getString(i));
                        }
                        ((ActiviteDetailClient) contexte).runOnUiThread(modificationReussie);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurClient(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    public static void supprimerClient(Context contexte, String id, Runnable suppressionReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_suppression));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.DELETE, "/contact/" + id, null, null,
                response -> {
                    spineurChargement.dismiss();
                    ((Activity) contexte).runOnUiThread(suppressionReussie);
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
     * Calcule et ordonne un itinéraire.
     * @param donnees Les données de l'itinéraire
     * @param contexte Le contexte de l'application
     * @param actionSuccess La méthode à appeler en cas de succès
     */
    public static void calculerItineraire(JSONObject donnees, Context contexte,
                                          Consumer<LinkedHashMap<Long, PointGPS>> actionSuccess) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_calcul_itineraire));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.POST, "/itineraire/calculer", null, donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();

                        JSONObject jsonReponse = new JSONObject(response);
                        LinkedHashMap<Long, PointGPS> point = parseItineraire(jsonReponse);

                        ((Activity) contexte).runOnUiThread(() -> actionSuccess.accept(point));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } ,
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurItineraire(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    /**
     * Parse un itinéraire pour le transformer en une liste ordonnée de points GPS.
     * @param jsonResponse La réponse de l'API
     * @return Un itinéraire ordonné
     */
    public static LinkedHashMap<Long, PointGPS> parseItineraire(JSONObject jsonResponse) {
        LinkedHashMap<Long, PointGPS> waypoints = new LinkedHashMap<>();

        try {
            // Extraire le tableau "itineraire" de l'objet JSON
            JSONArray itineraireArray = jsonResponse.getJSONArray("itineraire");
            JSONObject depart = itineraireArray.getJSONObject(0);
            itineraireArray.remove(0);
            waypoints.put(-1L, new PointGPS(depart.getDouble("latitude"), depart.getDouble("longitude"), depart.getString("nom")));

            // Parcourir le tableau pour récupérer les points
            for (int i = 0; i < itineraireArray.length(); i++) {
                JSONObject point = itineraireArray.getJSONObject(i);

                // Extraire les données du point
                Long id = point.getLong("id");
                String nom = point.getString("nom");
                double latitude = point.getDouble("latitude");
                double longitude = point.getDouble("longitude");

                // Ajouter le point dans la map avec le nom comme clé
                waypoints.put(id, new PointGPS(latitude, longitude, nom));
            }

            waypoints.put(-2L, new PointGPS(depart.getDouble("latitude"), depart.getDouble("longitude"), "Arrivée"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return waypoints;
    }

    public static void creationItineraire(Context contexte, JSONObject donnees, Runnable creationReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_ajout));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.POST, "/itineraire", null , donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();

                        // En cas de succès, on ajoute l'itinéraire au singleton pour faire l'affichage
                        JSONObject jsonReponse = new JSONObject(response);
                        //((ActiviteCreationItineraire) contexte).runOnUiThread(creationReussie);
                        Itineraire itineraireCree = new Itineraire(jsonReponse);
                        SingletonListeItineraire.getInstance().ajouterItineraire(itineraireCree);
                        ((ActiviteCreationItineraire) contexte).runOnUiThread(creationReussie);

                    } catch (Exception e) {
                        // TODO gestion erreur
                        Log.e("erreur creat client", e.toString());
                        //throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    // TODO gestion erreur api
                    //gestionErreur(contexte, error);
                    Log.e("erreur", error.toString());
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
            Log.e("erreur", e.toString());
        }
    }

    /**
     * Modification d'un itinéraire existant.
     * @param contexte Le contexte de l'application
     * @param donnees Les données de l'itinéraire
     * @param id L'identifiant de l'itinéraire
     * @param modificationReussie La méthode à appeler en cas de modification réussie
     */
    public static void modifierItineraire(Context contexte, JSONObject donnees, String id, Runnable modificationReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_modification));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.PUT, "/itineraire/" + id, null, donnees,
                response -> {
                    try {
                        spineurChargement.dismiss();
                        ((ActiviteDetailItineraire) contexte).runOnUiThread(modificationReussie);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    spineurChargement.dismiss();
                    gestionErreurItineraire(contexte, error);
                }
            );
        } catch (Exception e) {
            if (spineurChargement != null) spineurChargement.dismiss();
        }
    }

    /**
     * Méthode permettant de supprimer un itinéraire.
     * @param contexte Le contexte de l'application
     * @param id L'identifiant de l'itinéraire à supprimer
     * @param suppressionReussie La méthode à appeler en cas de suppression réussie
     */
    public static void supprimerItineraire(Context contexte, String id, Runnable suppressionReussie) {
        spineurChargement = new ProgressDialog(contexte);
        spineurChargement.setMessage(contexte.getString(R.string.chargement_suppression));
        spineurChargement.setCancelable(false);
        spineurChargement.show();

        try {
            requeteApi(contexte, Request.Method.DELETE, "/itineraire/" + id, null, null,
                response -> {
                    spineurChargement.dismiss();
                    ((Activity) contexte).runOnUiThread(suppressionReussie);
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
                    erreur.printStackTrace();
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
                    afficherErreurs((ActiviteInscription) contexte, erreurs, new int[] {
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
     * Afficher les erreurs lors de l'action.
     * @param activite L'activité
     * @param erreurs Les erreurs retournées par l'API
     * @param idChampsTextuels Les identifiants des inputs des champs textuels
     * @param cleChampsTextuels Les clés de réponse de l'API des champs textuels
     */
    private static void afficherErreurs(AppCompatActivity activite, JSONObject erreurs,
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

    /**
     * Méthode permettant de gérer les erreurs lors de la modification d'un compte.
     * @param contexte Le contexte de l'application
     * @param erreur L'erreur retournée par l'API
     */
    private static void gestionErreurModificationCompte(Context contexte, VolleyError erreur) {
        if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
            try {
                String responseBody = new String(erreur.networkResponse.data, "UTF-8");

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject erreurs = jsonResponse.getJSONObject("erreur");

                ((ActiviteGestionCompte) contexte).runOnUiThread(() -> {
                    afficherErreurs((ActiviteGestionCompte) contexte, erreurs, new int[] {
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
            ((ActiviteGestionCompte) contexte).runOnUiThread(() -> {
                SnackbarCustom.show(contexte, R.string.api_injoignable, SnackbarCustom.STYLE_ERREUR);
            });
        }
    }

    /**
     * Méthode permettant de gérer les erreurs lors de la création d'un client.
     * @param contexte Le contexte de l'application
     * @param erreur L'erreur retournée par l'API
     */
    private static void gestionErreurClient(Context contexte, VolleyError erreur) {
        AppCompatActivity activite = null;

        if (contexte instanceof ActiviteCreationClient) {
            activite = (ActiviteCreationClient) contexte;
        } else if (contexte instanceof ActiviteDetailClient) {
            activite = (ActiviteDetailClient) contexte;
        }

        final AppCompatActivity finalActivite = activite;

        if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
            try {
                String responseBody = new String(erreur.networkResponse.data, "UTF-8");

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject erreurs = jsonResponse.getJSONObject("erreur");

                activite.runOnUiThread(() -> {
                    afficherErreurs(finalActivite, erreurs, new int[] {
                        R.id.saisieNom, R.id.description, R.id.saisieAdresse,
                        R.id.prenomContact, R.id.nomContact, R.id.telephone
                    }, new String[] {
                        "entreprise", "description", "adresse", "prenom", "nom", "telephone"
                    });
                });
            } catch (Exception e) {
                Toast.makeText(contexte, R.string.erreur_inconnue, Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        } else {
            activite.runOnUiThread(() -> {
                SnackbarCustom.show(contexte, R.string.api_injoignable, SnackbarCustom.STYLE_ERREUR);
            });
            erreur.printStackTrace();
        }
    }

    /**
     * Méthode permettant de gérer les erreurs lors de la gestion d'un itinéraire.
     * @param contexte Le contexte de l'application
     * @param erreur L'erreur retournée par l'API
     */
    private static void gestionErreurItineraire(Context contexte, VolleyError erreur) {
        AppCompatActivity activite = null;

        if (contexte instanceof ActiviteCreationItineraire) {
            activite = (ActiviteCreationItineraire) contexte;
        } else if (contexte instanceof ActiviteDetailItineraire) {
            activite = (ActiviteDetailItineraire) contexte;
        }

        final AppCompatActivity finalActivite = activite;

        if (erreur.networkResponse != null && erreur.networkResponse.data != null) {
            try {
                String responseBody = new String(erreur.networkResponse.data, "UTF-8");

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject erreurs = jsonResponse.getJSONObject("erreur");

                activite.runOnUiThread(() -> {
                    afficherErreurs(finalActivite, erreurs, new int[] {
                        R.id.saisieNomItineraire
                    }, new String[] {
                        "nomItineraire"
                    });
                });
            } catch (Exception e) {
                Toast.makeText(contexte, R.string.erreur_inconnue, Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        } else {
            activite.runOnUiThread(() -> {
                SnackbarCustom.show(contexte, R.string.api_injoignable, SnackbarCustom.STYLE_ERREUR);
            });
            erreur.printStackTrace();
        }
    }
}