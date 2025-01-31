/*
 * ClientAdapter.java                                               31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.clientUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Adapter pour la liste des clients.
 *
 * @author Loïc FAUGIERES
 * @author Noah MIQUEL
 */
public class ClientAdapter extends RecyclerView.Adapter<ClientHolder> {

    private List<Client> clients;

    private OnViewClickListener onViewClickListener;

    private OnLongClickListener onLongClickListener;

    public interface OnViewClickListener {
        void onViewClick(int festivalId);
    }

    public interface OnLongClickListener {
        void onLongClick(int festivalId);
    }

    /**
     * Constructeur de l'adapter
     * @param donnees La liste des clients
     * @param onViewClickListener L'écouteur de clic sur un élément de la liste
     */
    public ClientAdapter(List<Client> donnees, OnViewClickListener onViewClickListener, OnLongClickListener supprimerClient) {
        this.clients = donnees;
        this.onViewClickListener = onViewClickListener;
        this.onLongClickListener = supprimerClient;
    }

    /**
     * Gère la création d'un holder pour une vue
     * @param viewGroup Le groupe de vues parent
     * @param viewType Le type de vue
     * @return Le holder de la vue
     */
    @Override
    public ClientHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.liste_client, viewGroup, false);
        return new ClientHolder(view);
    }

    @Override
    public void onBindViewHolder(ClientHolder holder, int position) {
        Client client = clients.get(position);
        holder.bind(client);

        holder.itemView.setOnClickListener(v ->
            onViewClickListener.onViewClick(holder.getBindingAdapterPosition()));

        holder.itemView.setOnLongClickListener(v -> {
            onLongClickListener.onLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void add(Client client) {
        clients.add(client);
    }
}
