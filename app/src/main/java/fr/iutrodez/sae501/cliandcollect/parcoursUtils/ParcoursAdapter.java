/*
 * ParcoursAdapter.java                                             06 mar. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Adapter pour la liste des parcours.
 *
 * @author Loïc FAUGIERES
 * @author Noah MIQUEL
 */
public class ParcoursAdapter extends RecyclerView.Adapter<ParcoursHolder> {

    private List<Parcours> parcours;

    private OnViewClickListener onViewClickListener;

    public interface OnViewClickListener {
        void onViewClick(int festivalId);
    }

    /**
     * Constructeur de l'adapter
     * @param donnees La liste des parcours
     * @param onViewClickListener L'écouteur de clic sur un élément de la liste
     */
    public ParcoursAdapter(List<Parcours> donnees, OnViewClickListener onViewClickListener) {
        this.parcours = donnees;
        this.onViewClickListener = onViewClickListener;
    }

    /**
     * Gère la création d'un holder pour une vue
     * @param viewGroup Le groupe de vues parent
     * @param viewType Le type de vue
     * @return Le holder de la vue
     */
    @Override
    public ParcoursHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.liste_parcours, viewGroup, false);
        return new ParcoursHolder(view);
    }

    @Override
    public void onBindViewHolder(ParcoursHolder holder, int position) {
        Parcours client = parcours.get(position);
        holder.bind(client);

        holder.itemView.setOnClickListener(v ->
            onViewClickListener.onViewClick(holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return parcours.size();
    }
}
