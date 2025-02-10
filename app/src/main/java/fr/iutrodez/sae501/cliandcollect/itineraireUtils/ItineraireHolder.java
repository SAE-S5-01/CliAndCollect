/*
 * ItineraireHolder.java                                            06 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.itineraireUtils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Gestion de l'affichage d'un itinéraire dans une liste.
 *
 * @author Lucas DESCRIAUD
 * @author Loïc FAUGIERES
 */
public class ItineraireHolder extends RecyclerView.ViewHolder {

    private TextView nom;

    private TextView nombreEtapes;

    public ItineraireHolder(@NonNull View itemView) {
        super(itemView);
        nom = itemView.findViewById(R.id.nomItineraire);
        nombreEtapes = itemView.findViewById(R.id.nombreEtapes);
    }

    public void bind(Itineraire itineraire) {
        nom.setText(itineraire.getNom());
        nombreEtapes.setText(itineraire.getOrdreClients().size() + " étapes");
    }
}


