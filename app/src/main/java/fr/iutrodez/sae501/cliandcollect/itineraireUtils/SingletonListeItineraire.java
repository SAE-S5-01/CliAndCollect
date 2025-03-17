/*
 * SingletonListeItineraire.java                                    31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;

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
     * Récupère la liste des itinéraires depuis l'API
     * @param contexte Le contexte de l'application
     * @param action L'action à effectuer après la récupération
     */
    public static void recupererItineraires(Context contexte, Runnable action) {
        ClientApi.getListeItineraires(contexte, action);
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
     * Supprime un itineraire de la liste des itineraires
     * @param id L'identifiant de l'itinéraire à supprimer
     */
    public static void supprimerItineraire(String id) {
        for (Itineraire itineraire : getInstance().listeItineraires) {
            if (itineraire.getID().equals(id)) {
                getInstance().listeItineraires.remove(itineraire);
                break;
            }
        }
    }

    /**
     * @return La liste des itinéraires
     */
    public static List<Itineraire> getListeItineraires() {
        return getInstance().listeItineraires;
    }

    /**
     * Récupère un itinéraire par son identifiant dans la liste
     * @param idListe L'identifiant de l'itinéraire dans la liste
     * @return Le itineraire correspondant à l'identifiant
     */
    public static Itineraire getItineraire(int idListe) {
        return getInstance().listeItineraires.get(idListe);
    }

    /**
     * Récupère un itinéraire par son identifiant
     * @param id L'identifiant de l'itinéraire
     * @return Le itinéraire correspondant à l'identifiant
     */
    public static Itineraire getItineraire(String id) {
        for (Itineraire itineraire : getInstance().listeItineraires) {
            if (itineraire.getID().equals(id)) {
                return itineraire;
            }
        }
        return null;
    }

    /**
     * Vide la liste des itineraires
     */
    public static void viderListeItineraire() {
        getInstance().listeItineraires.clear();
    }

    /**
     * Vérifier si un contact est dans un itinéraire.
     * @param idContact L'identifiant du contact
     * @return true si le contact est dans un itinéraire, false sinon
     */
    public static boolean estContactDansItineraire(Long idContact) {
        return getInstance().getListeItineraires().stream()
            .anyMatch(itineraire -> itineraire.getOrdreClients().containsKey(idContact));
    }
}
