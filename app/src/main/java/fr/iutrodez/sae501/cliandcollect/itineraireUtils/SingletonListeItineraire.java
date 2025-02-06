/*
 * SingletonListeClient.java                                        31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton permettant de gérer la liste des itineraires
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class SingletonListeItineraire {

    private static List<Itineraire> listeItineraires;

    private static SingletonListeItineraire instance;

    /**
     * Constructeur privé du singleton
     */
    private SingletonListeItineraire() {
        this.listeItineraires = new ArrayList<>();
    }

    /**
     * @return L'instance du singleton
     */
    public static SingletonListeItineraire getInstance() {
        if (instance == null) {
            instance = new SingletonListeItineraire();
        }
        return instance;
    }

    /**
     * Ajoute un itineraire à la liste des itinéraires
     * @param itineraire Le itineraire à ajouter
     */
    public static void ajouterItineraire(Itineraire itineraire) {
        getInstance().listeItineraires.add(itineraire);
    }

    /**
     * Supprime un itineraire de la liste des itineraires
     * @param itineraire Le itineraire à supprimer
     */
    public static void supprimerItineraire(Itineraire itineraire) {
        getInstance().listeItineraires.remove(itineraire);
    }

    /**
     * @return La liste des itinéraires
     */
    public static List<Itineraire> getListeItineraires() {
        return getInstance().listeItineraires;
    }

    /**
     * Récupère un itinéraire par son identifiant
     * @param id L'identifiant du itineraire
     * @return Le itineraire correspondant à l'identifiant
     */
    public static Itineraire getItineraire(int id) {
        return getInstance().listeItineraires.get(id);
    }

    /**
     * Vide la liste des itineraires
     */
    public static void viderListeItineraire() {
        getInstance().listeItineraires.clear();
    }
}
