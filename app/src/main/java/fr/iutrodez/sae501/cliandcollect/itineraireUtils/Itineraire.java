/*
 * Itineraire.java                                                  06 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Représentation d'un itinéraire.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class Itineraire {

    private String nom;

    private String ID;

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
        this.ID = itineraireFromApi.optString("idItineraire");
        this.ordreClientsJson = itineraireFromApi.getJSONObject("ordreClients");
        String key = "";
        String valeur = "";
        this.ordreClients = new LinkedHashMap<>();
        for (Iterator<String> it = ordreClientsJson.keys(); it.hasNext(); ) {
            key = it.next();
            valeur = ordreClientsJson.getString(key);
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

    public LinkedHashMap<Long, String> setItineraire(LinkedHashMap<Long, String> ordreClients){
        return this.ordreClients = ordreClients;
    }

    public String getID(){
        return ID;
    }

    public String getNom(){
        return nom;
    }

    public String setNom(String nom){
        return this.nom = nom;
    }

    public LinkedHashMap<Long, String> getOrdreClients() {
        return ordreClients;
    }

    public ArrayList<Double[]> getListeCoordonnees() {
        return listeCoordonnees;
    }

}
