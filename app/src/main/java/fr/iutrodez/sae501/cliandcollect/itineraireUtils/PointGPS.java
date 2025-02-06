/*
 * PointGPS.java                                                    06 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import org.osmdroid.util.GeoPoint;

/**
 * Représentation d'un point GPS sur la carte doté d'une latitude, d'une longitude et d'un nom.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class PointGPS extends GeoPoint {

    private String nom;

    /**
     * Constructeur de la classe PointGPS
     * @param latitude Latitude du point GPS
     * @param longitude Longitude du point GPS
     * @param nom Nom du point GPS
     */
    public PointGPS(double latitude, double longitude , String nom) {
        super(latitude , longitude);
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}
