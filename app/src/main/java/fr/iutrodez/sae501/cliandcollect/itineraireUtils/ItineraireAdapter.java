package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.clientUtils.ClientHolder;

public class ItineraireAdapter extends RecyclerView.Adapter<ItineraireHolder>{

    private List<Itineraire> itineraires;

    public ItineraireAdapter(List<Itineraire> donnees){
        itineraires = donnees;
    }

    @Override
    public ItineraireHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.liste_client,viewGroup,false);
        return new ItineraireHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraireHolder holder, int position) {
        Itineraire myItineraire = itineraires.get(position);
        holder.bind(myItineraire);
    }

    @Override
    public int getItemCount() {
        return itineraires.size();
    }
}
