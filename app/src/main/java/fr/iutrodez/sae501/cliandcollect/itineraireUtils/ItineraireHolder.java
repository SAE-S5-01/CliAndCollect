package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.sae501.cliandcollect.R;

public class ItineraireHolder extends RecyclerView.ViewHolder{

    private TextView nom;

    private TextView nombreEtapes;

    public ItineraireHolder(@NonNull View itemView) {
        super(itemView);
        nom = itemView.findViewById(R.id.nomItineraire);
        nombreEtapes = (TextView) itemView.findViewById(R.id.nombreEtapes);
    }

    public void bind(Itineraire itineraire){
        nom.setText(itineraire.getNom());
        nombreEtapes.setText("" + itineraire.getOrdreClients().size());
    }
}


