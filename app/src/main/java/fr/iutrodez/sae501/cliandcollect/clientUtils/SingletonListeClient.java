/*
 * SingletonListeClient.java                                        31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.clientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton permettant de gérer la liste des clients
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class SingletonListeClient {

    private List<Client> listeClient;

    private static SingletonListeClient instance;

    /**
     * Constructeur privé du singleton
     */
    private SingletonListeClient() {
        listeClient = new ArrayList<>();
    }

    /**
     * @return L'instance du singleton
     */
    public static SingletonListeClient getInstance() {
        if (instance == null) {
            instance = new SingletonListeClient();
        }
        return instance;
    }

    /**
     * Ajoute un client à la liste des clients
     * @param client Le client à ajouter
     */
    public static void ajouterClient(Client client) {
        getInstance().listeClient.add(client);
    }

    /**
     * Supprime un client de la liste des clients
     * @param client Le client à supprimer
     */
    public static void supprimerClient(Client client) {
        getInstance().listeClient.remove(client);
    }

    /**
     * @return La liste des clients
     */
    public static List<Client> getListeClient() {
        return getInstance().listeClient;
    }

    /**
     * Récupère un client par son identifiant
     * @param id L'identifiant du client
     * @return Le client correspondant à l'identifiant
     */
    public static Client getClient(int id) {
        return getInstance().listeClient.get(id);
    }
}
