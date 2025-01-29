package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.util.ArrayMap;

import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;

public class Itineraire {

    private String nom;

    private String id;
    private LinkedHashMap<Long, String> listeClients;

    // TODO parse le geojsonlinestring pour avoir la liste des coordonnées des clients

    public String getNom(){
        return nom;
    }

    public LinkedHashMap<Long, String> getItineraire() { return listeClients; }

    /**public GeoJsonLineString getGeoJsonLineString(){
        GeoJsonLineString geoJsonLineString = new GeoJsonLineString();
        for (Client client : listeClients){
            geoJsonLineString.addPoint(client.getGeoJsonPoint());
        }
        return geoJsonLineString;
    }*/

    public Itineraire(JSONObject itineraireFromApi) throws JSONException {

        // Valeur obligatoirement retourné par l'api
        this.nom = itineraireFromApi.optString("nomItineraire");
        this.id = itineraireFromApi.optString("idItineraire");
        JSONObject listeClients = itineraireFromApi.getJSONObject("ordreClient");

        for (Iterator<String> it = listeClients.keys(); it.hasNext(); ) {
            String key = it.next();
            String valeur = listeClients.optString(key, null);
            this.listeClients.put(Long.parseLong(key), valeur);
        }

        // TODO instancier les co
    }

    public String setNom(String nom){
        return this.nom = nom;
    }

    public LinkedHashMap<Long, String> setItineraire(LinkedHashMap<Long, String> listeClients){
        return this.listeClients = listeClients;
    }

}
