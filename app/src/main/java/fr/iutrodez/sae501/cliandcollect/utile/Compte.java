/*
 * Compte.java                                                      07 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stockage et gestion d'un compte utilisateur
 *
 * @author Loïc FAUGIERES
 */
public class Compte {

    private String email;

    private String motDePasse;

    private String nom;

    private String prenom;

    private String adresse;

    private double longitude;

    private double latitude;

    private static Compte instance;

    /**
     * Instancier un compte
     * @param donnees Les données du compte
     */
    public Compte(JSONObject donnees) {
        try {
            this.email = donnees.getString("mail");
            this.motDePasse = donnees.optString("motDePasse");
            this.nom = donnees.getString("nom");
            this.prenom = donnees.getString("prenom");
            this.adresse = donnees.getString("adresse");
            this.longitude = donnees.getDouble("longitude");
            this.latitude = donnees.getDouble("latitude");
            instance = this;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return L'instance du compte
     */
    public static Compte getInstance() {
        return instance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
