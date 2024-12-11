package fr.iutrodez.sae501.cliandcollect.clientUtils;

public class Client {

    private String entreprise;

    private String adresse;

    private String description;

    private String coordonnees;

    private String nomContact;

    private String prenomContact;

    private String telephone;

    private boolean clientPropspect;

    public Client(String entreprise, String adresse,String nomContact,String prenomContact,
                  String telephone, boolean clientPropspect){
        this.entreprise = entreprise;
        this.adresse = adresse;
        this.nomContact = nomContact;
        this.prenomContact = prenomContact;
        this.telephone = telephone;
        this.clientPropspect = clientPropspect;

    }

    public Client(String entreprise, String adresse,String coordonnees,boolean clientPropspect){
        this.entreprise = entreprise;
        this.adresse = adresse;
        this.clientPropspect = clientPropspect;
        this.coordonnees = coordonnees;
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

}
