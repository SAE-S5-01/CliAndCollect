package fr.iutrodez.sae501.cliandcollect.requetes;


import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Classe Singleton permettant de gérer les requêtes Volley.
 * @author Descriaud Lucas
 */
public class RequeteVolley {

    private static RequeteVolley instance;
    private RequestQueue fileRequete;

    private static Context context;

    /**
     * Constructeur de la classe RequeteVolley.
     * @param context Le contexte de l'application
     */
    private RequeteVolley(Context context) {
        this.context = context;
        fileRequete = getFileRequete();
    }

    /***
     * Méthode permettant de récupérer l'instance de la classe RequeteVolley.
     * @param context Le contexte de l'application
     * @return L'instance de la classe RequeteVolley
     */
    public static synchronized RequeteVolley getInstance(Context context) {
        if (instance == null) {
            instance = new RequeteVolley(context);
        }
        return instance;
    }

    /**
     * Méthode permettant de récupérer la file de requêtes.
     * @return La file de requêtes
     */
    public RequestQueue getFileRequete() {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(context.getApplicationContext());
        }
        return fileRequete;
    }

    /**
     * Méthode permettant d'ajouter une requete à la file de requêtes.
     * @param requete la requete a ajoute
     * @param <T> le type de la requete
     */
    public <T> void ajoutFileRequete(Request<T> requete) {
        getFileRequete().add(requete);
    }
}


