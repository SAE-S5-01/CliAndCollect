/*
 * SnackbarCustom.java                                                    30 jan. 2025
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */

package fr.iutrodez.sae501.cliandcollect.utile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;

import fr.iutrodez.sae501.cliandcollect.R;

/**
 * Gestion des Snackbars personnalisés.
 *
 * @author Loïc FAUGIERES
 */
public class SnackbarCustom {

    public static final int DUREE_DEFAUT = Snackbar.LENGTH_LONG;

    public static final int STYLE_INFORMATION = 0;
    public static final int STYLE_VALIDATION = 1;
    public static final int STYLE_ATTENTION = 2;
    public static final int STYLE_ERREUR = 3;

    /**
     * Crée un Snackbar personnalisé en utilisant un identifiant de chaîne pour le message.
     * Utilise une valeur par défaut pour la durée (LONG).
     *
     * @param contexte Le contexte de l'activité.
     * @param idMessageErreur L'identifiant de la chaîne à afficher dans le Snackbar.
     * @param style Le style à appliquer : INFORMATION, ATTENTION, ERREUR.
     */
    public static void show(Context contexte, int idMessageErreur, int style) {
        show(contexte, contexte.getString(idMessageErreur), style, DUREE_DEFAUT);
    }

    /**
     * Crée un Snackbar personnalisé avec le message passé en paramètre.
     * Utilise une valeur par défaut pour la durée (LONG).
     *
     * @param contexte Le contexte de l'activité.
     * @param messageErreur Le message à afficher dans le Snackbar.
     * @param style Le style à appliquer : INFORMATION, ATTENTION, ERREUR.
     */
    public static void show(Context contexte, String messageErreur, int style) {
        show(contexte, messageErreur, style, DUREE_DEFAUT);
    }

    /**
     * Crée un Snackbar personnalisé avec le message passé en paramètre et la durée spécifiée.
     *
     * @param contexte Le contexte de l'activité.
     * @param messageErreur Le message à afficher dans le Snackbar.
     * @param style Le style à appliquer : INFORMATION, ATTENTION, ERREUR.
     * @param duree La durée du Snackbar (ex: SnackbarCustom.LENGTH_LONG).
     */
    public static void show(Context contexte, String messageErreur, int style, int duree) {
        View vue = ((Activity) contexte).findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(vue, "", duree);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.TRANSPARENT);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbarView.getLayoutParams();
        params.setMargins(0, 0, 0, 220);
        snackbarView.setLayoutParams(params);

        // Ajouter une vue personnalisée à l'intérieur du Snackbar
        LayoutInflater inflater = LayoutInflater.from(snackbarView.getContext());
        View customView = inflater.inflate(R.layout.snackbar_custom, null);

        TextView snackbarTextView = customView.findViewById(R.id.texte_snackbar);
        snackbarTextView.setText(messageErreur);

        CardView snackbarCustom = customView.findViewById(R.id.snackbar_custom);
        ImageView snackbarIcon = customView.findViewById(R.id.snackbar_icon);
        switch (style) {
            default:
            case STYLE_INFORMATION:
                snackbarIcon.setImageResource(android.R.drawable.ic_dialog_info);
                snackbarCustom.setCardBackgroundColor(contexte.getResources().getColor(R.color.information));
                break;
            case STYLE_VALIDATION:
                snackbarIcon.setImageResource(android.R.drawable.checkbox_on_background);
                snackbarCustom.setCardBackgroundColor(contexte.getResources().getColor(R.color.validation));
                break;
            case STYLE_ATTENTION:
                snackbarIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                snackbarCustom.setCardBackgroundColor(contexte.getResources().getColor(R.color.attention));
                break;
            case STYLE_ERREUR:
                snackbarIcon.setImageResource(android.R.drawable.stat_notify_error);
                snackbarCustom.setCardBackgroundColor(contexte.getResources().getColor(R.color.erreur));
                break;
        }

        // Insérer la vue personnalisée dans le Snackbar
        ViewGroup snackbarLayout = (ViewGroup) snackbarView;
        snackbarLayout.removeAllViews();
        snackbarLayout.addView(customView);

        snackbar.show();
    }

}
