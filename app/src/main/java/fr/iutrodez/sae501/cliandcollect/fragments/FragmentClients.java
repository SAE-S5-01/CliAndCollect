/*
 * FragmentClients.java                                             28 nov. 2024
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
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailClient;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.ClientAdapter;
import fr.iutrodez.sae501.cliandcollect.clientUtils.SingletonListeClient;
import fr.iutrodez.sae501.cliandcollect.requetes.ClientApi;
import fr.iutrodez.sae501.cliandcollect.utile.Reseau;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;

/**
 * Gestion du fragment Clients.
 *
 * @author Loïc FAUGIERES
 */
public class FragmentClients extends Fragment implements View.OnClickListener {

    private Intent creationClient;

    private Intent detailClient;

    private ActivityResultLauncher<Intent> lanceurCreation;

    private ActivityResultLauncher<Intent> lanceurDetails;

    private RecyclerView listeClients;

    private ArrayList<Client> clients;

    private ClientAdapter adapter;

    /**
     * @return Une nouvelle instance de FragmentClients.
     */
    public static FragmentClients newInstance() {
        return new FragmentClients();
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
        View vueDuFragment = inflater.inflate(R.layout.fragment_clients, container, false);
        vueDuFragment.findViewById(R.id.boutonAjoutClient).setOnClickListener(this);

        detailClient = new Intent(FragmentClients.this.getContext(), ActiviteDetailClient.class);

        listeClients = vueDuFragment.findViewById(R.id.recycler_view_clients);
        clients = new ArrayList<>();

        creationClient = new Intent(FragmentClients.this.getContext(), ActiviteCreationClient.class);
        lanceurCreation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::mettreAJourListeClients);
        lanceurDetails = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::getModifClient);

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(vueDuFragment.getContext());
        listeClients.setLayoutManager(gestionnaireLineaire);

        adapter = new ClientAdapter(clients,this::onDetailClientClick, this::supprimerClient);
        listeClients.setHasFixedSize(true);
        listeClients.setAdapter(adapter);

        return vueDuFragment;
    }

    /**
     * Lorsque le fragment est affiché, récupérer les clients si la liste est vide.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (clients.isEmpty()) {
            recupererClients();
        }
    }

    /**
     * Récupère la liste des clients depuis l'API et la met à jour localement.
     */
    private void recupererClients() {
        if (Reseau.reseauDisponible(this.getContext())) {
            ClientApi.getListeClient(this.getContext(), () -> {
                mettreAJourListeClients(null);
            });
        } else {
            SnackbarCustom.show(this.getContext(), R.string.erreur_recuperation_clients, SnackbarCustom.STYLE_ERREUR);
        }
    }

    /**
     * Met à jour la liste des clients de la vue.
     * @param resultat Le résultat de l'activité de création de client
     */
    private void mettreAJourListeClients(ActivityResult resultat) {
        clients.clear();
        for (Client client : SingletonListeClient.getListeClient()) {
            clients.add(client);
        }
        mettreAJourTexteErreur();
        adapter.notifyDataSetChanged();
    }

    /**
     * Met à jour le texte d'erreur si aucun client n'est présent.
     */
    private void mettreAJourTexteErreur() {
        this.getView().findViewById(R.id.erreurPasDeClient)
                      .setVisibility(clients.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Méthode invoquée lors du clic sur le bouton d'ajout de client.
     * @param v La vue du bouton d'ajout de client
     */
    @Override
    public void onClick(View v) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            lanceurCreation.launch(creationClient);
        }
    }

    /**
     * Méthode invoquée lors du clic sur la carte d'un client / prospect
     * @param i L'identifiant du client / prospect
     */
    public void onDetailClientClick(int i) {
        if (Reseau.reseauDisponible(this.getContext(), true)) {
            detailClient.putExtra("ID", i);
            lanceurDetails.launch(detailClient);
        }
    }

    private void getModifClient(ActivityResult resultat) {
        Intent retourFille = resultat.getData();
        if (resultat.getResultCode() == Activity.RESULT_OK) {
            int id = retourFille.getIntExtra("ID",0);
            Client client = SingletonListeClient.getClient(id);
            clients.remove(id);
            clients.add(id,client);
            listeClients.setAdapter(adapter);
        }
    }

    /**
     * Supprime un client de la liste des clients
     * @param position La position du client à supprimer
     */
    private void supprimerClient(int position) {
        new AlertDialog.Builder(getContext())
            .setTitle("Supprimer ce client ?")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce client ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                if (Reseau.reseauDisponible(this.getContext(), true)) {
                    ClientApi.supprimerClient(this.getContext(),
                        clients.get(position).getID().toString(),
                        () -> {
                            Client clientASupprimer = clients.get(position);
                            SingletonListeClient.supprimerClient(clientASupprimer);
                            clients.remove(position);

                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, clients.size());

                            mettreAJourTexteErreur();
                        });

                }
            })
            .setNegativeButton("Non", null)
            .show();
    }

}