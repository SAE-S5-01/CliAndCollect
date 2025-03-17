package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Date;

import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;

public class Parcours {

    private Long id;

    private Itineraire itineraire;

    private Date dateParcours;

    private String etatParcours;

    private int nombreEtapes;

    public Parcours(JSONObject objetParcours) throws JSONException {
        this.id = objetParcours.getLong("id");
        this.itineraire = SingletonListeItineraire.getInstance().getItineraire(objetParcours.getString("idItineraire"));
        // Conversion de la date ISO 8601 en java.util.Date
        String dateStr = objetParcours.getString("dateCreation");
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr);
        this.dateParcours = Date.from(offsetDateTime.toInstant());
        this.etatParcours = objetParcours.getString("statut");
        this.nombreEtapes = itineraire.getListeCoordonnees().size();
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

    public Date getDateParcours() {
        return dateParcours;
    }

    public void setDateParcours(Date dateParcours) {
        this.dateParcours = dateParcours;
    }

    public String getEtatParcours() {
        return etatParcours;
    }

    public void setEtatParcours(String etatParcours) {
        this.etatParcours = etatParcours;
    }

    public int getNombreEtapes() {
        return nombreEtapes;
    }

    public void setNombreEtapes(int nombreEtapes) {
        this.nombreEtapes = nombreEtapes;
    }

}
