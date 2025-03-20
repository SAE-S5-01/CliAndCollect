package fr.iutrodez.sae501.cliandcollect.clientUtils;

import org.json.JSONObject;

public class Client {

    private String entreprise;

    private String adresse;

    private String description;

    /** Longitude */
    private double x;

    /** Latitude */
    private double y;

    private String nomContact;

    private String prenomContact;

    private String telephone;

    private Long ID;

    private boolean estProspect;

    /**
     * Constructeur permettant d'instancier un ob
     * @param nom
     */
    public Client(String nom) {
        this.entreprise = nom;
    }

    public Client(JSONObject clientFromApi) {

        // Valeur obligatoirement retourné par l'api
        this.entreprise = clientFromApi.optString("nomEntreprise");
        this.adresse = clientFromApi.optString("adresse");
        this.x = clientFromApi.optDouble("longitude");
        this.y = clientFromApi.optDouble("latitude");
        this.estProspect = clientFromApi.optBoolean("prospect");
        this.ID = clientFromApi.optLong("id");

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

    public boolean isProspect() {
        return estProspect;
    }

    public Long getID(){
        return ID;
    }

    public void setEstProspect(boolean estProspect) {
        this.estProspect = estProspect;
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

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Override
    public String toString() {
        return adresse + " - " + entreprise;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;  // Vérification si c'est la même instance
        if (obj == null || getClass() != obj.getClass()) return false;  // Vérifie le type
        Client client = (Client) obj;  // Cast sécurisé

        return this.ID.equals(client.getID());  // Comparaison d'ID
    }
}
