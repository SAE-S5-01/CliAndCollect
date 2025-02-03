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

    private Long ID;

    private JSONObject ordreClientsJson;

    private LinkedHashMap<Long, String> ordreClients;

    private JSONArray coordonnees;

    private JSONObject point;

    // Liste des couples x et y issues de JSONObject point
private ArrayList<Double[]> listeCoordonnees;


    // TODO parse le geojsonlinestring pour avoir la liste des coordonnées des clients

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
        this.ID = itineraireFromApi.optLong("idItineraire");
        this.ordreClientsJson = itineraireFromApi.getJSONObject("ordreClients");
        String key = "";
        String valeur = "";
        this.ordreClients = new LinkedHashMap<>();
        for (Iterator<String> it = ordreClientsJson.keys(); it.hasNext(); ) {
            key = it.next();
            valeur = ordreClientsJson.getString(key);
            System.out.println(valeur);
            this.ordreClients.put(Long.parseLong(key), valeur);

        }

        coordonnees = itineraireFromApi.getJSONObject("geoJsonLineString").getJSONArray("coordinates");

        listeCoordonnees = new ArrayList<>();
        // Stockage des coordonnées sous forme de liste
        for (int i = 0; i < coordonnees.length(); i++) {
            point = coordonnees.getJSONObject(i);
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            listeCoordonnees.add(new Double[]{x, y});
        }
    }

    public String setNom(String nom){
        return this.nom = nom;
    }

    public LinkedHashMap<Long, String> setItineraire(LinkedHashMap<Long, String> ordreClients){
        return this.ordreClients = ordreClients;
    }

    public Long getID(){
        return ID;
    }

    public String getNom(){
        return nom;
    }

    public LinkedHashMap<Long, String> getOrdreClients() {
        return ordreClients;
    }

    public ArrayList<Double[]> getListeCoordonnees() {
        return listeCoordonnees;
    }




}
