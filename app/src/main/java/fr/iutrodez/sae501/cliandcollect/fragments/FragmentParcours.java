/*
 * FragmentParcours.java                                            28 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteParcours;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.Parcours;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.ParcoursAdapter;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Gestion du fragment Parcours.
 * @author Loïc FAUGIERES
 */
public class FragmentParcours extends Fragment implements View.OnClickListener {

    private RecyclerView listeParcoursEnCours;
    private RecyclerView listeParcoursEnPause;
    private RecyclerView listeParcoursArretes;
    private RecyclerView listeParcoursTermines;

    private ArrayList<Parcours> parcoursEnCours;
    private ArrayList<Parcours> parcoursEnPause;
    private ArrayList<Parcours> parcoursArretes;
    private ArrayList<Parcours> parcoursTermines;

    private ParcoursAdapter adapterParcoursEnCours;
    private ParcoursAdapter adapterParcoursEnPause;
    private ParcoursAdapter adapterParcoursArretes;
    private ParcoursAdapter adapterParcoursTermines;

    private ArrayList<Itineraire> itineraires;

    /**
     * @return Une nouvelle instance de FragmentParcours.
     */
    public static FragmentParcours newInstance() {
        return new FragmentParcours();
    }

    /**
     * Appel automatique lorsque le fragment est attaché à son activité parente.
     * @param contexte Initialisé automatiquement avec l'activité parente.
     */
    @Override
    public void onAttach(Context contexte) {
        super.onAttach(contexte);
    }

    /**
     * Appel automatique lorsque le fragment est créé.
     * @param savedInstanceState Si le fragment est recréé suite à une rotation de l'écran
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Appel automatique pour créer et initialiser la vue du fragment.
     * @param inflater Le LayoutInflater qui permet d'instancier le layout XML en objet Java
     * @param container Le ViewGroup parent dans lequel la vue du fragment doit être insérée
     * @param savedInstanceState Si le fragment est recréé suite à une rotation de l'écran
     * @return La vue du fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // On récupère la vue (le layout) associée au fragment affiché
        View vueDuFragment = inflater.inflate(R.layout.fragment_parcours, container, false);

        vueDuFragment.findViewById(R.id.startParcours).setOnClickListener(this);

        vueDuFragment.findViewById(R.id.parcoursEnCours).setVisibility(View.VISIBLE);
        vueDuFragment.findViewById(R.id.parcoursEnPause).setVisibility(View.VISIBLE);
        vueDuFragment.findViewById(R.id.parcoursArretes).setVisibility(View.VISIBLE);
        vueDuFragment.findViewById(R.id.parcoursTermines).setVisibility(View.VISIBLE);

        listeParcoursEnCours = vueDuFragment.findViewById(R.id.recycler_view_parcours_en_cours);
        listeParcoursEnPause = vueDuFragment.findViewById(R.id.recycler_view_parcours_en_pause);
        listeParcoursArretes = vueDuFragment.findViewById(R.id.recycler_view_parcours_arretes);
        listeParcoursTermines = vueDuFragment.findViewById(R.id.recycler_view_parcours_termines);

        listeParcoursEnCours.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));
        listeParcoursEnPause.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));
        listeParcoursArretes.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));
        listeParcoursTermines.setLayoutManager(new LinearLayoutManager(vueDuFragment.getContext()));

        parcoursEnCours = new ArrayList<>();
        parcoursEnPause = new ArrayList<>();
        parcoursArretes = new ArrayList<>();
        parcoursTermines = new ArrayList<>();

        itineraires = new ArrayList<>();

        adapterParcoursEnCours = new ParcoursAdapter(parcoursEnCours, this::onParcoursEnCoursClick);
        adapterParcoursEnPause = new ParcoursAdapter(parcoursEnPause, this::onParcoursEnPauseClick);
        adapterParcoursArretes = new ParcoursAdapter(parcoursArretes, this::onParcoursArreteClick);
        adapterParcoursTermines = new ParcoursAdapter(parcoursTermines, this::onParcoursTermineClick);

        listeParcoursEnCours.setAdapter(adapterParcoursEnCours);
        listeParcoursEnPause.setAdapter(adapterParcoursEnPause);
        listeParcoursArretes.setAdapter(adapterParcoursArretes);
        listeParcoursTermines.setAdapter(adapterParcoursTermines);

        return vueDuFragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (itineraires.isEmpty()) {
            recupererItineraires();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.getContext(), ActiviteParcours.class);
        startActivity(intent);
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un parcours en cours
     * @param i L'identifiant du parcours en cours
     */
    private void onParcoursEnCoursClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            // TODO : Afficher les détails du parcours
        }
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un parcours en pause
     * @param i L'identifiant du parcours en pause
     */
    private void onParcoursEnPauseClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            // TODO : Afficher les détails du parcours
        }
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un parcours arrêté
     * @param i L'identifiant du parcours arrêté
     */
    private void onParcoursArreteClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            // TODO : Afficher les détails du parcours
        }
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un parcours terminé
     * @param i L'identifiant du parcours terminé
     */
    private void onParcoursTermineClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            // TODO : Afficher les détails du parcours
        }
    }

    /**
     * Récupère la liste des itinéraires depuis l'API et la met à jour localement.
     */
    private void recupererItineraires() {
        if (Reseau.reseauDisponible(this.getContext())) {
            ClientApi.getListeItineraires(this.getContext(),
                () -> mettreAJourListeItineraires());
        } else {
            SnackbarCustom.show(this.getContext(),
                R.string.erreur_recuperation_itineraires,
                SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Met à jour la liste des itinéraires de la vue.
     */
    private void mettreAJourListeItineraires() {
        itineraires.clear();
        for (Itineraire itineraire : SingletonListeItineraire.getInstance().getListeItineraires()) {
            //itineraires.add(itineraire);
            /*parcoursEnCours.add(new Parcours(itineraire.getNom(), new Date(), "En cours (stub)", itineraire.getListeCoordonnees().size()));
            parcoursEnPause.add(new Parcours(itineraire.getNom(), new Date(), "En pause (stub)", itineraire.getListeCoordonnees().size()));
            parcoursEnPause.add(new Parcours(itineraire.getNom(), new Date(), "En pause (stub)", itineraire.getListeCoordonnees().size()));
            parcoursArretes.add(new Parcours(itineraire.getNom(), new Date(), "Arrêté (stub)", itineraire.getListeCoordonnees().size()));*/
        }
        mettreAJourTexteErreur();
        adapterParcoursEnCours.notifyDataSetChanged();
        adapterParcoursEnPause.notifyDataSetChanged();
        adapterParcoursArretes.notifyDataSetChanged();
    }

    /**
     * Met à jour le texte d'erreur si aucun itinéraire n'existe.
     */
    private void mettreAJourTexteErreur() {
        this.getView().findViewById(R.id.erreurPasDItineraire)
            .setVisibility(itineraires.isEmpty() ? View.VISIBLE : View.GONE);
    }
}