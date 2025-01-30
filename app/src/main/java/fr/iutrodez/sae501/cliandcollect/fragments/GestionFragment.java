/*
 * ActivitePrincipale.java                                                27 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect.fragments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import fr.iutrodez.sae501.cliandcollect.R;
import fr.iutrodez.sae501.cliandcollect.activites.ActiviteConnexion;
import fr.iutrodez.sae501.cliandcollect.utile.Preferences;
import fr.iutrodez.sae501.cliandcollect.utile.SnackbarCustom;


/**
 * Activité principale et point d'entrée de l'application.
 * @author Loïc FAUGIERES
 */
public class GestionFragment extends AppCompatActivity {

    public static String CLE_EXTRA_MSG_BIENVENUE = "welcome_message";

    TabLayout gestionnaireOnglet;

    int[] navigationButtonsIds;

    /**
     * Méthode invoquée lors de la création de l'activité.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_principale);

        ImageButton boutonOptionMenu = findViewById(R.id.boutonOptionMenu);
        registerForContextMenu(boutonOptionMenu);

        boutonOptionMenu.setOnClickListener(view -> afficherMenuContextuel(view));

        /*
         * on récupère un accès sur le ViewPager et sur le TabLayout qui gèrera les onglets
         */
        ViewPager2 gestionnairePagination = findViewById(R.id.activity_main_viewpager);
        gestionnaireOnglet = findViewById(R.id.footer_layout);

        /*
         * on associe au ViewPager un adaptateur pour gérer le défilement entre les fragments
         */
        gestionnairePagination.setAdapter(new AdaptateurFragments(this));

        navigationButtonsIds = new int[] {
            R.id.pied_page_icone_accueil,
            R.id.pied_page_icone_client,
            R.id.pied_page_icone_parcours,
            R.id.pied_page_icone_itineraire
        };

        /*
         * On fait le lien entre
         * le gestionnaire de pagination et le gestionnaire des onglets
         */
        new TabLayoutMediator(gestionnaireOnglet, gestionnairePagination,
            (tab, position) -> {
                // Associer manuellement des vues personnalisées
                View customView = getLayoutInflater().inflate(R.layout.pied_page_patron_icone, null);
                ImageView icon = customView.findViewById(R.id.footer_icon);

                // Configurer l'icône
                setResourceByPosition(icon, position);
                // Configurer l'ID de l'image
                icon.setId(navigationButtonsIds[position]);

                // Associer la vue personnalisée à l'onglet
                tab.setCustomView(customView);
            }).attach();

        // Mise à jour de la couleur lors du changement de page
        gestionnairePagination.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                TabLayout.Tab ongletActif = gestionnaireOnglet.getTabAt(position);

                if (ongletActif != null && ongletActif.getCustomView() != null) {
                    // Récupérer le bouton de l'onglet actif
                    ImageView boutonActif
                        = ongletActif.getCustomView()
                                     .findViewById(navigationButtonsIds[position]);

                    if (boutonActif != null) {
                        setButtonState(boutonActif, true);
                        resetOtherButtons(position);
                    }
                }
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(CLE_EXTRA_MSG_BIENVENUE, false)) {
            SnackbarCustom.show(this, R.string.inscription_reussie, SnackbarCustom.STYLE_VALIDATION);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_contextuel_entete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = gestionItemMenuSelectionne(item);

        // Si l'élément a été traité, retourne true. Sinon, passe au comportement par défaut
        return handled || super.onContextItemSelected(item);
    }

    /**
     * Affiche un menu contextuel et gère les éléments sélectionnés
     * @param view Vue sur laquelle le menu contextuel s'affiche
     */
    private void afficherMenuContextuel(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_contextuel_entete, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> gestionItemMenuSelectionne(item));

        popupMenu.show();
    }

    /**
     * Gère les actions lors de la sélection d'un item dans le menu
     * @param item L'élément du menu sélectionné
     * @return true si l'action est traitée, false sinon
     */
    private boolean gestionItemMenuSelectionne(MenuItem item) {
        boolean resultat = false;

        if (item.getItemId() == R.id.menu_deconnecter) {
            Preferences.effacerInfosConnexion(this);
            Intent pageConnexion = new Intent(this, ActiviteConnexion.class);
            startActivity(pageConnexion);
            finish();
            resultat = true;
        } else if (item.getItemId() == R.id.menu_compte) {
            // TODO vue mon compte
            resultat = true;
        }

        return resultat;
    }

    /**
     * Modifie l'image d'un bouton en fonction de sa position.
     * @param button Bouton à modifier
     * @param position Position du bouton
     * @throws IllegalStateException Si la position est inattendue
     */
    private void setResourceByPosition(ImageView button, int position)
    throws IllegalStateException {
        switch (position) {
            case 0:
                button.setImageResource(R.drawable.accueil);
                break;
            case 1:
                button.setImageResource(R.drawable.client);
                break;
            case 2:
                button.setImageResource(R.drawable.parcours);
                break;
            case 3:
                button.setImageResource(R.drawable.itineraire);
                break;
            default:
                throw new IllegalStateException("Position inattendue : " + position);
        }
    }

    /**
     * Modifie la couleur d'un bouton en fonction de son état
     * @param button Bouton à modifier
     * @param isActive Etat du bouton
     */
    private void setButtonState(ImageView button, boolean isActive) {
        button.setColorFilter(
            getResources().getColor(isActive
                                    ? R.color.pied_page_icone_active
                                    : R.color.pied_page_icone_inactive,
                              null));
    }

    /**
     * Réinitialise les boutons des onglets non actifs
     * @param activePosition Position de l'onglet actif
     */
    private void resetOtherButtons(int activePosition) {
        for (int i = 0; i < gestionnaireOnglet.getTabCount(); i++) {
            if (i == activePosition) continue;

            TabLayout.Tab tab = gestionnaireOnglet.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                ImageView button
                        = tab.getCustomView().findViewById(navigationButtonsIds[i]);
                if (button != null) {
                    setButtonState(button, false);
                }
            }
        }
    }
}