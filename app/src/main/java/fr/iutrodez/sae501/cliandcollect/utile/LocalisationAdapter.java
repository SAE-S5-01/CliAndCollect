/*
 * LocalisationAdapter.java                                         27 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

import fr.iutrodez.sae501.cliandcollect.R;


/**
 * Adaptateur pour l'affichage des localisations.
 *
 * @author Loïc FAUGIERES
 */
public class LocalisationAdapter extends ArrayAdapter<String> {

    private final List<String> localisations;
    private final LayoutInflater inflater;

    public LocalisationAdapter(Context context, List<String> localisations) {
        super(context, R.layout.item_localisation, localisations);
        this.localisations = localisations;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_localisation, parent, false);
        }

        TextView locationName = convertView.findViewById(R.id.texte_localisation);
        locationName.setText(localisations.get(position));

        return convertView;
    }
}
