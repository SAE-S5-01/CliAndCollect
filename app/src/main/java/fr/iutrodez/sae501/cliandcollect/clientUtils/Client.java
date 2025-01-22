package fr.iutrodez.sae501.cliandcollect.clientUtils;

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

    public Client(String entreprise, String adresse,String nomContact,String prenomContact,
                  String telephone, boolean clientPropspect,double x , double y){
        this.entreprise = entreprise;
        this.adresse = adresse;
        this.nomContact = nomContact;
        this.prenomContact = prenomContact;
        this.telephone = telephone;
        this.clientPropspect = clientPropspect;
        this.x = x;
        this.y = y;

    }

    public Client(String entreprise, String adresse, double x , double y ,boolean clientPropspect){
        this.entreprise = entreprise;
        this.adresse = adresse;
        this.clientPropspect = clientPropspect;
        this.x = x;
        this.y = y;
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
