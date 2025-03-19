/*
 * Parcours.java                                                    18 mar. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;

import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.PointGPS;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;

/**
 * Réprésente un parcours
 *
 * @author Loïc FAUGIERES
 */
public class Parcours {

    private Long id;

    private Itineraire itineraire;

    private Date dateCreation;

    private String statut;

    private int nombreEtapes;

    private Client dernierContactVisite;

    private ArrayList<PointGPS> positionsGpsPrecedentes;

    public Parcours(JSONObject objetParcours) throws JSONException {
        this.id = objetParcours.getLong("id");
        this.itineraire = SingletonListeItineraire.getInstance().getItineraire(objetParcours.getString("idItineraire"));
        // Conversion de la date ISO 8601 en java.util.Date
        String dateStr = objetParcours.getString("dateCreation");
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr);
        this.dateCreation = Date.from(offsetDateTime.toInstant());
        this.statut = objetParcours.getString("statut");
        this.nombreEtapes = itineraire != null ? itineraire.getListeCoordonnees().size() : -1;

        if (objetParcours.has("idDernierContactVisite")
            && !objetParcours.isNull("idDernierContactVisite")) {
            this.dernierContactVisite
            = SingletonListeClient.getInstance().getClient(
                objetParcours.getLong("idDernierContactVisite"));
        }

        if (objetParcours.has("positionsGpsPrecedentes")
            && !objetParcours.isNull("positionsGpsPrecedentes")) {
            this.positionsGpsPrecedentes = new ArrayList<>();

            JSONArray positionsGpsPrecedentes = objetParcours.getJSONArray("positionsGpsPrecedentes");

            for (int i = 0; i < positionsGpsPrecedentes.length(); i++) {
                JSONObject positionPrecedente = positionsGpsPrecedentes.getJSONObject(i);
                this.positionsGpsPrecedentes.add(new PointGPS(
                    positionPrecedente.getDouble("y"),
                    positionPrecedente.getDouble("x"),
                    ""
                ));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Itineraire getItineraire() {
        return itineraire;
    }

    public void setItineraire(Itineraire itineraire) {
        this.itineraire = itineraire;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getNombreEtapes() {
        return nombreEtapes;
    }

    public void setNombreEtapes(int nombreEtapes) {
        this.nombreEtapes = nombreEtapes;
    }

    public Client getDernierContactVisite() {
        return this.dernierContactVisite;
    }

    public void setDernierContactVisite(Client dernierContactVisite) {
        this.dernierContactVisite = dernierContactVisite;
    }

    public ArrayList<PointGPS> getPositionsGpsPrecedentes() {
        return positionsGpsPrecedentes;
    }

    public void setPositionsGpsPrecedentes(ArrayList<PointGPS> positionsGpsPrecedentes) {
        this.positionsGpsPrecedentes = positionsGpsPrecedentes;
    }
}
