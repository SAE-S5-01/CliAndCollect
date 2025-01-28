package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.util.ArrayMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;

public class Itineraire {

    private String nom;

    private LinkedHashMap<Long, String> listeClients;

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

    public Itineraire(JSONObject itineraireFromApi){

        // Valeur obligatoirement retourné par l'api
        this.nom = itineraireFromApi.optString("nomItineraire");
        this.listeClients = new LinkedHashMap<>();
        for (int i = 0; i < itineraireFromApi.optJSONArray("clients").length(); i++){
            //this.listeClients.put(itineraireFromApi.optJSONArray("ordreClients :{ ").optLong(i)[0], itineraireFromApi.optJSONArray("ordreClients").optString(i)[1]);
            this.listeClients.put(itineraireFromApi.optLong("ordreClients"), itineraireFromApi.optString("ordreClients"));
        }
        return;
    }

    public String setNom(String nom){
        return this.nom = nom;
    }

    public LinkedHashMap<Long, String> setItineraire(LinkedHashMap<Long, String> listeClients){
        return this.listeClients = listeClients;
    }

}
