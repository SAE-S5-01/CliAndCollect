package fr.iutrodez.sae501.cliandcollect.utile;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

/**
 * Classe utilitaire permettant d'activer ou de désactiver un bouton
 * en fonction du texte saisi dans un EditText.
 */
public class GestionBouton {

    /**
     * Active ou désactive un bouton en fonction du texte du EditText.
     * Le bouton est activé si le texte n'est pas vide.
     *
     * @param editText Le EditText à surveiller.
     * @param button Le bouton à activer/désactiver.
     */
    public static void activerBoutonSiTexteEntre(EditText editText, Button button) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().isEmpty()) {
                    button.setEnabled(false); // Désactive le bouton si le champ est vide
                } else {
                    button.setEnabled(true); // Active le bouton si l'adresse contient du texte
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
}
