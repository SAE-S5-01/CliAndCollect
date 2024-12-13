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
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteCreationClient;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteDetailClient;
import fr.iutrodez.sae501.cliandcollect.clientUtils.Client;
import fr.iutrodez.sae501.cliandcollect.clientUtils.ClientAdapter;

/**
 * Gestion du fragment Clients.
 * @author Loïc FAUGIERES
 */
public class FragmentClients extends Fragment implements View.OnClickListener {

    private Intent intent;

    private Intent detailClient;

    private ActivityResultLauncher<Intent> lanceurFille;

    private RecyclerView listeClients;

    private ArrayList<Client> clients;

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

        listeClients = vueDuFragment.findViewById(R.id.recycler_view_clients);
        clients = new ArrayList<>();
        clients.add(new Client("test","test","20:45",false));
        clients.add(new Client("test2","test2","20:45",true));
        clients.add(new Client("test3","test3","20:45",true));

        LinearLayoutManager gestionnaireLineaire = new LinearLayoutManager(vueDuFragment.getContext());
        listeClients.setLayoutManager(gestionnaireLineaire);

        ClientAdapter adapter = new ClientAdapter(clients);
        listeClients.setHasFixedSize(true);
        listeClients.setAdapter(adapter);

        intent = new Intent(FragmentClients.this.getContext(), ActiviteCreationClient.class);
        lanceurFille = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::getNouveauClient);
        return vueDuFragment;
    }

    public void onClientClik(){
        detailClient = new Intent(FragmentClients.this.getContext(), ActiviteDetailClient.class);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        lanceurFille.launch(intent);
    }

    private void initialiseClients(){

    }

    private void getNouveauClient(ActivityResult resultat) {
        Intent retourFille = resultat.getData();

        if(resultat.getResultCode() == Activity.RESULT_OK){

        }
    }

}