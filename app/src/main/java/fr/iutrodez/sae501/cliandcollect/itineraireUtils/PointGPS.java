package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import org.osmdroid.util.GeoPoint;

public class PointGPS extends GeoPoint {

    private String nom;

    public PointGPS(double latitude, double longitude , String nom) {
        super(latitude , longitude);
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}
