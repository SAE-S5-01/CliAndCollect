/*
 * ItineraireAdapter.java                                           31 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Adapter pour la liste des itinéraires.
 *
 * @author Loïc FAUGIERES
 * @author Simon GUIRAUD
 */
public class ItineraireAdapter extends RecyclerView.Adapter<ItineraireHolder> {

    private List<Itineraire> itineraires;

    private OnViewClickListener onViewClickListener;

    private OnLongClickListener onLongClickListener;

    public interface OnViewClickListener {
        void onViewClick(int festivalId);
    }

    public interface OnLongClickListener {
        void onLongClick(int festivalId);
    }

    public ItineraireAdapter(List<Itineraire> donnees, OnViewClickListener onViewClickListener) {
        this.itineraires = donnees;
        this.onViewClickListener = onViewClickListener;
    }

    /**
     * Constructeur de l'adapter
     * @param donnees La liste des itineraires
     * @param onViewClickListener L'écouteur de clic sur un élément de la liste
     */
    public ItineraireAdapter(List<Itineraire> donnees, OnViewClickListener onViewClickListener, OnLongClickListener supprimerItineraire) {
        this.itineraires = donnees;
        this.onViewClickListener = onViewClickListener;
        this.onLongClickListener = supprimerItineraire;
    }

    /**
     * Gère la création d'un holder pour une vue
     * @param viewGroup Le groupe de vues parent
     * @param viewType Le type de vue
     * @return Le holder de la vue
     */
    @Override
    public ItineraireHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.liste_itineraire, viewGroup, false);
        return new ItineraireHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraireHolder holder, int position) {
        Itineraire itineraire = itineraires.get(position);
        holder.bind(itineraire);

        holder.itemView.setOnClickListener(v ->
            onViewClickListener.onViewClick(holder.getBindingAdapterPosition()));

        holder.itemView.setOnLongClickListener(v -> {
            onLongClickListener.onLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itineraires.size();
    }
}