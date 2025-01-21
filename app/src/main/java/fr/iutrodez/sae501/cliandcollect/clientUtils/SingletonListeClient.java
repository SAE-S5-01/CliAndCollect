package fr.iutrodez.sae501.cliandcollect.clientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton permettant de gérer la liste des clients.
 *
 * @author descriaud lucas
 */
public class SingletonListeClient {

    private List<Client> listeClient;

    private static SingletonListeClient instance;

    private SingletonListeClient() {
        listeClient = new ArrayList<>();
    }

    /**
     * @return l'instance du singletion
     */
    public static SingletonListeClient getInstance(){
        if(instance == null){
            instance = new SingletonListeClient();
        }
        return instance;
    }

    /**
     * Ajoute un client à la liste des clients.
     * @param client Le client à ajouter.
     */
    public static void ajouterClient(Client client){
        instance.listeClient.add(client);
    }

    /**
     * Retourne la liste des clients.
     */
    public static List<Client> getListeClient(){ return instance.listeClient; }
}
