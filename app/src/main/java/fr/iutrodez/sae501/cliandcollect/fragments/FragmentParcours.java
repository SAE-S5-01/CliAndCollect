/*
 * FragmentParcours.java                                            28 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteParcours;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.Parcours;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.ParcoursAdapter;
import fr.iutrodez.sae501.cliandcollect.parcoursUtils.SingletonListeParcours;
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

        adapterParcoursEnCours
        = new ParcoursAdapter(parcoursEnCours, this::onParcoursEnCoursClick,
                              (position) -> supprimerParcours(position, parcoursEnCours));
        adapterParcoursEnPause
        = new ParcoursAdapter(parcoursEnPause, this::onParcoursEnPauseClick,
                              (position) -> supprimerParcours(position, parcoursEnPause));
        adapterParcoursArretes
        = new ParcoursAdapter(parcoursArretes, this::onParcoursArreteClick,
                              (position) -> supprimerParcours(position, parcoursArretes));
        adapterParcoursTermines
        = new ParcoursAdapter(parcoursTermines, this::onParcoursTermineClick,
                              (position) -> supprimerParcours(position, parcoursTermines));

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
        if (parcoursEnCours.isEmpty()) {
            recupererParcours();
        }
    }

    @Override
    public void onClick(View v) {
        //Intent intent = new Intent(this.getContext(), ActiviteParcours.class);
        //startActivity(intent);
        afficherListeDialog(this.getContext());

    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un parcours en cours
     * @param parcoursId L'identifiant du parcours en cours
     */
    private void onParcoursEnCoursClick(int parcoursId) {
        //Intent intent = new Intent(this.getContext(), ActiviteParcours.class);
        //intent.putExtra("PARCOURS_ID", parcoursId);
        //startActivity(intent);
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
     * Méthode invoquée lors de l'appui long sur un parcours
     * @param position La position du parcours à supprimer
     * @param liste La liste de parcours à laquelle appartient le parcours à supprimer
     */
    private void supprimerParcours(int position, ArrayList<Parcours> liste) {
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.supprimer_parcours)
            .setMessage(R.string.confirmation_suppression_parcours)
            .setPositiveButton("Oui", (dialog, which) -> {
                if (Reseau.reseauDisponible(this.getContext(), true)) {
                    Parcours parcours = liste.get(position);
                    ClientApi.supprimerParcours(this.getContext(), parcours.getId(),
                        () -> {
                            SnackbarCustom.show(this.getContext(),
                                    R.string.parcours_supprime,
                                    SnackbarCustom.STYLE_VALIDATION);
                            SingletonListeParcours.supprimerParcours(parcours);
                            mettreAJourListesParcours(null);
                        });
                }
            })
            .setNegativeButton("Non", null)
            .show();
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
     * Récupère la liste des parcours depuis l'API et la met à jour localement.
     */
    private void recupererParcours() {
        if (Reseau.reseauDisponible(this.getContext())) {
            SingletonListeParcours.recupererParcours(this.getContext(), () -> {
                mettreAJourListesParcours(null);
            });
        } else {
            SnackbarCustom.show(this.getContext(),
                R.string.erreur_recuperation_parcours,
                SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Met à jour la liste des itinéraires de la vue.
     */
    private void mettreAJourListeItineraires() {
        itineraires.clear();
        for (Itineraire itineraire : SingletonListeItineraire.getInstance().getListeItineraires()) {
            itineraires.add(itineraire);
        }
        mettreAJourAffichageEtErreur();
    }

    /**
     * Met à jour les listes des parcours de la vue.
     * @param resultat Le résultat de l'activité de création de parcours
     */
    private void mettreAJourListesParcours(ActivityResult resultat) {
        parcoursEnCours.clear();
        parcoursEnPause.clear();
        parcoursArretes.clear();
        parcoursTermines.clear();

        for (Parcours parcours : SingletonListeParcours.getInstance().getListeParcours()) {
            switch (parcours.getEtatParcours()) {
                default:
                case "EN_COURS":
                    parcoursEnCours.add(parcours);
                    break;
                case "EN_PAUSE":
                    parcoursEnPause.add(parcours);
                    break;
                case "ARRETE":
                    parcoursArretes.add(parcours);
                    break;
                case "TERMINE":
                    parcoursTermines.add(parcours);
                    break;
            }
        }

        mettreAJourAffichageEtErreur();
        adapterParcoursEnCours.notifyDataSetChanged();
        adapterParcoursEnPause.notifyDataSetChanged();
        adapterParcoursArretes.notifyDataSetChanged();
        adapterParcoursTermines.notifyDataSetChanged();
    }

    /**
     * Met à jour le texte d'erreur si aucun itinéraire n'existe et l'affichage
     * des différents types de parcours.
     */
    private void mettreAJourAffichageEtErreur() {
        boolean aucunItineraire = parcoursEnCours.isEmpty() && parcoursEnPause.isEmpty()
            && parcoursArretes.isEmpty() && parcoursTermines.isEmpty();

        this.getView().findViewById(R.id.erreurPasDItineraire)
            .setVisibility(aucunItineraire ? View.VISIBLE : View.GONE);
        this.getView().findViewById(R.id.parcoursEnCours)
            .setVisibility(parcoursEnCours.isEmpty() ? View.GONE : View.VISIBLE);
        this.getView().findViewById(R.id.parcoursEnPause)
            .setVisibility(parcoursEnPause.isEmpty() ? View.GONE : View.VISIBLE);
        this.getView().findViewById(R.id.parcoursArretes)
            .setVisibility(parcoursArretes.isEmpty() ? View.GONE : View.VISIBLE);
        this.getView().findViewById(R.id.parcoursTermines)
            .setVisibility(parcoursTermines.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void afficherListeDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sélectionnez un itinéraire");

        // Récupérer la liste des itinéraires
        List<Itineraire> items = SingletonListeItineraire.getListeItineraires();

        // Extraire les noms des itinéraires pour l'affichage
        String[] nomsItineraires = new String[items.size()];
        String[] idsItineraires = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
            nomsItineraires[i] = items.get(i).getNom();  // Affiché dans le dialogue
            idsItineraires[i] = items.get(i).getID();   // Transmis à l'activité fille
        }

        builder.setItems(nomsItineraires, (dialog, which) -> {
            String selectedId = idsItineraires[which];

            // Lancer l'activité fille avec l'ID sélectionné
            Intent intent = new Intent(context, ActiviteParcours.class);
            intent.putExtra("SELECTED_ITINERAIRE_ID", selectedId);
            context.startActivity(intent);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}


