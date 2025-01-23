package fr.iutrodez.sae501.cliandcollect.clientUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {

    private String entreprise;

    private String adresse;

    private String description;

    private double x;

    private double y;

    private String nomContact;

    private String prenomContact;

    private String telephone;

    private boolean clientPropspect;

    public Client(JSONObject clientFromApi){

        // Valeur obligatoirement retourné par l'api
        this.entreprise = clientFromApi.optString("nomEntreprise");
        this.adresse = clientFromApi.optString("adresse");
        this.x = clientFromApi.optDouble("longitude");
        this.y = clientFromApi.optDouble("latitude");
        this.clientPropspect = clientFromApi.optBoolean("prospect");

        // Valeur optionnelles
        this.description = clientFromApi.optString("description" , null);
        this.nomContact = clientFromApi.optString("nomContact" , null);
        this.prenomContact = clientFromApi.optString("prenomContact" , null);
        this.telephone = clientFromApi.optString("telephone" , null);
    }
    public String getDescription() {
        return description;
    }


    public String getAdresse() {
        return adresse;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public String getNomContact() {
        return nomContact;
    }

    public String getPrenomContact() {
        return prenomContact;
    }

    public String getTelephone() {
        return telephone;
    }

    public boolean isClientPropspect() {
        return clientPropspect;
    }

    public double getX() {
        return x;
    }

    public void setClientPropspect(boolean clientPropspect) {
        this.clientPropspect = clientPropspect;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setPrenomContact(String prenomContact) {
        this.prenomContact = prenomContact;
    }

    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

}
