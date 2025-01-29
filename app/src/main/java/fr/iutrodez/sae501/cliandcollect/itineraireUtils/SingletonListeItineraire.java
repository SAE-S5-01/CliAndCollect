package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton permettant de gérer la liste des itinéraires.
 *
 * @author GUIRAUD Simon
 */
public class SingletonListeItineraire {

    private List<Itineraire> listeItineraires;

    private static SingletonListeItineraire instance;

    private SingletonListeItineraire() {
        listeItineraires = new ArrayList<>();
    }

    /**
     * @return l'instance du singletion
     */
    public static SingletonListeItineraire getInstance(){
        if(instance == null){
            instance = new SingletonListeItineraire();
        }
        return instance;
    }

    /**
     * Ajoute un client à la liste des clients.
     * @param itineraire L'itinéraire à ajouter.
     */
    public static void ajouterItineraire(Itineraire itineraire){
        instance.listeItineraires.add(itineraire);
    }

    /**
     * Retourne la liste des clients.
     */
    public static List<Itineraire> getListeItineraire(){
        return instance.listeItineraires;
    }
}
