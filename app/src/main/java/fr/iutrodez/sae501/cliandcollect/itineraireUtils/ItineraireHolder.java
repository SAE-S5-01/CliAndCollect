package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.sae501.cliandcollect.R;

public class ItineraireHolder extends RecyclerView.ViewHolder{

    private TextView nom;

    private TextView listeClient;

    public ItineraireHolder(@NonNull View itemView) {
        super(itemView);
        nom = (TextView) itemView.findViewById(R.id.entreprise);
        listeClient = (TextView) itemView.findViewById(R.id.adresse);
    }

    public void bind(Itineraire itineraire){
        nom.setText(itineraire.getNom());
        //listeClient.setText(itineraire.getItineraire());
    }
}


