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

    private RequeteVolley(Context context) {
        this.context = context;
        fileRequete = getFileRequete();
    }

    public static synchronized RequeteVolley getInstance(Context context) {
        if (instance == null) {
            instance = new RequeteVolley(context);
        }
        return instance;
    }

    public RequestQueue getFileRequete() {
        if (fileRequete == null) {
            fileRequete = Volley.newRequestQueue(context.getApplicationContext());
        }
        return fileRequete;
    }

    public <T> void ajoutFileRequete(Request<T> requete) {
        getFileRequete().add(requete);
    }
}


