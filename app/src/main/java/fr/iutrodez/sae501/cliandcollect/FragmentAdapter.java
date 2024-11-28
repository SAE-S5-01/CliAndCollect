/*
 * FragmentAdapter.java                                             28 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adaptateur gérant les fragments qui seront associés au ViewPager.
 * Dans sa version minimale, la classe contient un constructeur auquel
 * on passera en argument l'activité qui gère le ViewPager, une méthode
 * createFragment et une méthode getItemCount
 * @author Loïc FAUGIERES
 */
public class FragmentAdapter extends FragmentStateAdapter {

    /** Nombre de fragments gérés par cet adaptateur, ou nombre d'onglets de la vue */
   private static final int NB_FRAGMENT = 4;

    /**
     * Constructeur de base
     * @param activite Activité qui contient le ViewPager qui gèrera les fragments
     */
    public FragmentAdapter(FragmentActivity activite) {
        super(activite);
    }

    @Override
    public Fragment createFragment(int position) {
        /*
         * Le ViewPager auquel on associera cet adaptateur devra afficher successivement
         * un fragment de type : FragmentAfficher, puis FragmentAjouter.
         * C'est dans cette méthode que l'on décide dans quel ordre sont affichés les
         * fragments, et quel fragment doit précisément être affiché
         */
        switch (position) {
            case 0:
                return FragmentHome.newInstance();
            case 1:
                return FragmentClients.newInstance();
            case 2:
                return FragmentParcours.newInstance();
            case 3:
                return FragmentItineraires.newInstance();
            default :
                return null;
        }
    }

    @Override
    public int getItemCount() {
        // renvoyer le nombre de fragments gérés par l'adaptateur
        return NB_FRAGMENT;
    }
}