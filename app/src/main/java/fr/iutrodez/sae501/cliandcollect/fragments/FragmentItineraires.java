/*
 * FragmentItineraires.java                                         03 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationItineraire;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailItineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.Itineraire;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.ItineraireAdapter;
import fr.iutrodez.sae501.cliandcollect.itineraireUtils.SingletonListeItineraire;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Gestion du fragment Itinéraires.
 *
 * @author Loïc FAUGIERES
 * @author Simon GUIRAUD
 */
public class FragmentItineraires extends Fragment implements View.OnClickListener {

    private Intent creationItineraire;

    private Intent detailItineraire;

    private ActivityResultLauncher<Intent> lanceurCreation;

    private ActivityResultLauncher<Intent> lanceurDetails;

    private RecyclerView listeItineraires;

    private ArrayList<Itineraire> itineraires;

    private ItineraireAdapter adapter;

    /**
     * @return Une nouvelle instance de FragmentItineraires.
     */
    public static FragmentItineraires newInstance() {
        return new FragmentItineraires();
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
        View vueDuFragment = inflater.inflate(R.layout.fragment_itineraires, container, false);
        vueDuFragment.findViewById(R.id.boutonAjoutItineraire).setOnClickListener(this);
        detailItineraire = new Intent(FragmentItineraires.this.getContext(), ActiviteDetailItineraire.class);

        listeItineraires = vueDuFragment.findViewById(R.id.recycler_view_itineraires);
        itineraires = new ArrayList<>();

        creationItineraire = new Intent(FragmentItineraires.this.getContext(), ActiviteCreationItineraire.class);
        lanceurCreation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::mettreAJourListeItineraires);
        lanceurDetails = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::gestionModificationItineraire);

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(vueDuFragment.getContext());
        listeItineraires.setLayoutManager(gestionnaireLineaire);

        adapter = new ItineraireAdapter(itineraires, this::onDetailItineraireClick, this::supprimerItineraire);
        listeItineraires.setHasFixedSize(true);
        listeItineraires.setAdapter(adapter);

        return vueDuFragment;
    }

    /**
     * Lorsque le fragment est affiché, récupérer les itineraires si la liste est vide.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (itineraires.isEmpty()) {
            recupererItineraires();
        }
    }

    /**
     * Récupère la liste des itineraires depuis l'API et la met à jour localement.
     */
    private void recupererItineraires() {
        if (Reseau.reseauDisponible(this.getContext())) {
            ClientApi.getListeItineraire(this.getContext(),
                () -> mettreAJourListeItineraires(null));
        } else {
            SnackbarCustom.show(this.getContext(),
                                R.string.erreur_recuperation_itineraires,
                                SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'ajout de itineraire.
     * @param v La vue du bouton d'ajout de itineraire
     */
    @Override
    public void onClick(View v) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            lanceurCreation.launch(creationItineraire);
        }
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un itineraire / prospect
     * @param i L'identifiant du itineraire / prospect
     */
    public void onDetailItineraireClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            detailItineraire.putExtra("ID", i);
            lanceurDetails.launch(detailItineraire);
        }
    }

    private void gestionModificationItineraire(ActivityResult resultat) {
        Intent retourFille = resultat.getData();
        if (resultat.getResultCode() == Activity.RESULT_OK) {
            int id = retourFille.getIntExtra("ID",0);
            Itineraire itineraire = SingletonListeItineraire.getItineraire(id);
            itineraires.remove(id);
            itineraires.add(id, itineraire);
            adapter.notifyItemChanged(id);
        }
    }

    /**
     * Met à jour la liste des itinéraires de la vue.
     * @param resultat Le résultat de l'activité de création d'itinéraire
     */
    private void mettreAJourListeItineraires(ActivityResult resultat) {
        itineraires.clear();
        for (Itineraire itineraire : SingletonListeItineraire.getInstance().getListeItineraires()) {
            itineraires.add(itineraire);
        }
        mettreAJourTexteErreur();
        adapter.notifyDataSetChanged();
    }

    /**
     * Met à jour le texte d'erreur si aucun itinéraire n'est présent.
     */
    private void mettreAJourTexteErreur() {
        this.getView().findViewById(R.id.erreurPasDItineraire)
            .setVisibility(itineraires.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Supprime un itineraire de la liste des itineraires
     * @param position La position du itineraire à supprimer
     */
    private void supprimerItineraire(int position) {
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.supprimer_itineraire)
            .setMessage(R.string.confirmation_suppression_itineraire)
            .setPositiveButton("Oui", (dialog, which) -> {
                if (Reseau.reseauDisponible(this.getContext(), true)) {
                    ClientApi.supprimerItineraire(this.getContext(),
                        itineraires.get(position).getID().toString(),
                        () -> {
                            Itineraire itineraireASupprimer = itineraires.get(position);

                            SingletonListeItineraire.supprimerItineraire(itineraireASupprimer);
                            itineraires.remove(position);

                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, itineraires.size());

                            mettreAJourTexteErreur();
                        });

                }
            })
            .setNegativeButton("Non", null)
            .show();
    }

}