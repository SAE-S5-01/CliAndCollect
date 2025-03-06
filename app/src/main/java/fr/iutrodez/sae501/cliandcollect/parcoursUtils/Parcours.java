package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import java.util.Date;

public class Parcours {

    private String nomItineraire;

    private Date dateParcours;

    private String etatParcours;

    private int nombreEtapes;

    public Parcours(String nomItineraire, Date dateParcours, String etatParcours, int nombreEtapes) {
        this.nomItineraire = nomItineraire;
        this.dateParcours = dateParcours;
        this.etatParcours = etatParcours;
        this.nombreEtapes = nombreEtapes;
    }

    public String getNomItineraire() {
        return nomItineraire;
    }

    public void setNomItineraire(String nomItineraire) {
        this.nomItineraire = nomItineraire;
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
