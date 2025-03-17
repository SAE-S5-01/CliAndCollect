/*
 * ParcoursHolder.java                                              06 mar. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.parcoursUtils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Holder pour un parcours.
 *
 * @author Loïc FAUGIERES
 */
public class ParcoursHolder extends RecyclerView.ViewHolder {

    private TextView nomItineraire;

    private TextView dateParcours;

    private TextView etatParcours;

    private TextView nombreEtapes;

    private SimpleDateFormat formatteurDates;

    public ParcoursHolder(@NonNull View itemView) {
        super(itemView);
        nomItineraire = itemView.findViewById(R.id.nomItineraire);
        dateParcours = itemView.findViewById(R.id.dateParcours);
        etatParcours = itemView.findViewById(R.id.etatParcours);
        nombreEtapes = itemView.findViewById(R.id.nombreEtapes);
        formatteurDates = new SimpleDateFormat("'Le' dd/MM/yyyy 'à' HH:mm", Locale.FRANCE);
        formatteurDates.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    }

    public void bind(Parcours parcours) {
        nomItineraire.setText(parcours.getItineraire().getNom());
        dateParcours.setText(formatteurDates.format(parcours.getDateParcours()));
        switch (parcours.getEtatParcours()) {
            default:
            case "EN_COURS":
                etatParcours.setText("En cours");
                break;
            case "EN_PAUSE":
                etatParcours.setText("En pause");
                break;
            case "ARRETE":
                etatParcours.setText("Arrêté");
                break;
            case "TERMINE":
                etatParcours.setText("Terminé");
                break;
        }
        nombreEtapes.setText(parcours.getNombreEtapes() + " étape(s)");
    }
}

