/*
 * ClientHolder.java                                                06 fev. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.clientUtils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Holder pour un client.
 *
 * @author Loïc FAUGIERES
 * @author Lucas DESCRIAUD
 */
public class ClientHolder extends RecyclerView.ViewHolder {

    private TextView entreprise;

    private TextView adresse;

    public ClientHolder(@NonNull View itemView) {
        super(itemView);
        entreprise = itemView.findViewById(R.id.entreprise);
        adresse = itemView.findViewById(R.id.adresse);
    }

    public void bind(Client client) {
        entreprise.setText(client.getEntreprise());
        adresse.setText(client.getAdresse());
    }
}
