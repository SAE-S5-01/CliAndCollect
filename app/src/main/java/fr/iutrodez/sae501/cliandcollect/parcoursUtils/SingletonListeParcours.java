/*
 * SingletonListeParcours.java                                      17 mar. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;

/**
 * Singleton permettant de gérer la liste des parcours
 *
 * @author Loïc FAUGIERES
 */
public class SingletonListeParcours {

    private static List<Parcours> listeParcours;

    private static SingletonListeParcours instance;

    /**
     * Constructeur privé du singleton
     */
    private SingletonListeParcours() {
        this.listeParcours = new ArrayList<>();
    }

    /**
     * @return L'instance du singleton
     */
    public static SingletonListeParcours getInstance() {
        if (instance == null) {
            instance = new SingletonListeParcours();
        }
        return instance;
    }

    /**
     * Récupère la liste des parcours depuis l'API
     * @param contexte Le contexte de l'application
     * @param action L'action à effectuer après la récupération
     */
    public static void recupererParcours(Context contexte, Runnable action) {
        ClientApi.getListeParcours(contexte, action);
    }

    /**
     * Ajoute un parcours à la liste des parcours
     * @param parcours Le parcours à ajouter
     */
    public static void ajouterParcours(Parcours parcours) {
        getInstance().listeParcours.add(parcours);
    }

    /**
     * Supprime un parcours de la liste des parcours
     * @param id L'identifiant du parcours à supprimer
     */
    public static void supprimerParcours(String id) {
        for (Parcours parcours : getInstance().listeParcours) {
            if (parcours.getId().equals(id)) {
                getInstance().listeParcours.remove(parcours);
                break;
            }
        }
    }

    /**
     * Supprime un parcours de la liste des parcours
     * @param parcours Le parcours à supprimer
     */
    public static void supprimerParcours(Parcours parcours) {
        getInstance().listeParcours.remove(parcours);
    }

    /**
     * @return La liste des parcourss
     */
    public static List<Parcours> getListeParcours() {
        return getInstance().listeParcours;
    }

    /**
     * Récupère un parcours par son identifiant dans la liste
     * @param idListe L'identifiant du parcours dans la liste
     * @return Le parcours correspondant à l'identifiant
     */
    public static Parcours getParcours(int idListe) {
        return getInstance().listeParcours.get(idListe);
    }

    /**
     * Vide la liste des parcours
     */
    public static void viderListeParcours() {
        getInstance().listeParcours.clear();
    }
}
