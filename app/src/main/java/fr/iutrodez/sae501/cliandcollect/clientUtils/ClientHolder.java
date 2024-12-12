package fr.iutrodez.sae501.cliandcollect.clientUtils;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.iutrodez.sae501.cliandcollect.R;

public class ClientHolder extends RecyclerView.ViewHolder {

    private TextView entreprise;

    private TextView adresse;

    public ClientHolder(@NonNull View itemView) {
        super(itemView);
        entreprise = (TextView) itemView.findViewById(R.id.entreprise);
        adresse = (TextView) itemView.findViewById(R.id.adresse);
    }

    public void bind(Client client){
        entreprise.setText(client.getEntreprise());
        adresse.setText(client.getAdresse());
    }
}
