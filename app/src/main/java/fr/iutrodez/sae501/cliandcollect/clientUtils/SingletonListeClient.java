/*
 * SingletonListeClient.java                                        31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.clientUtils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;

/**
 * Singleton permettant de gérer la liste des clients
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class SingletonListeClient {

    private static List<Client> listeClients;

    private static SingletonListeClient instance;

    /**
     * Constructeur privé du singleton
     */
    private SingletonListeClient() {
        this.listeClients = new ArrayList<>();
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
     * Récupère la liste des clients depuis l'API
     * @param contexte Le contexte de l'application
     * @param action L'action à effectuer après la récupération
     */
    public static void recupererClients(Context contexte, Runnable action) {
        ClientApi.getListeClient(contexte, action);
    }

    /**
     * Ajoute un client à la liste des clients
     * @param client Le client à ajouter
     */
    public static void ajouterClient(Client client) {
        getInstance().listeClients.add(client);
    }

    /**
     * Supprime un client de la liste des clients
     * @param client Le client à supprimer
     */
    public static void supprimerClient(Client client) {
        getInstance().listeClients.remove(client);
    }

    /**
     * @return La liste des clients
     */
    public static List<Client> getListeClient() {
        return getInstance().listeClients;
    }

    /**
     * Récupère un client par sa position dans la liste
     * @param position La position du client dans la liste
     * @return
     */
    public static Client getClient(int position) {
        return getInstance().listeClients.get(position);
    }

    /**
     * Récupère un client par son identifiant
     * @param id L'identifiant du client
     * @return Le client correspondant à l'identifiant
     */
    public static Client getClient(Long id) {
        Client clientTrouve = null;

        for (int i = 0;
             i < getInstance().listeClients.size()
             && clientTrouve == null;
             i++) {
            Client client = getInstance().listeClients.get(i);
            if (client.getID().equals(id)) {
                clientTrouve = client;
            }
        }

        return clientTrouve;
    }

    /**
     * Vide la liste des clients
     */
    public static void viderListeClient() {
        getInstance().listeClients.clear();
    }
}
