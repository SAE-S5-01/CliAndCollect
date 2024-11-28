/*
 * MainActivity.java                                                27 nov. 2024
 * IUT de Rodez, pas de copyright ni de "copyleft".
 */
package fr.iutrodez.sae501.cliandcollect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Activité principale et point d'entrée de l'application.
 * @author Loïc FAUGIERES
 */
public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);

        /*
         * on récupère un accès sur le ViewPager et sur le TabLayout qui gèrera les onglets
         */
        ViewPager2 gestionnairePagination = findViewById(R.id.activity_main_viewpager);
        gestionnaireOnglet = findViewById(R.id.footer_layout);

        /*
         * on associe au ViewPager un adaptateur pour gérer le défilement entre les fragments
         */
        gestionnairePagination.setAdapter(new FragmentAdapter(this));

        navigationButtonsIds = new int[] {
            R.id.footer_icon_home,
            R.id.footer_icon_client,
            R.id.footer_icon_parcours,
            R.id.footer_icon_itineraire
        };

        /*
         * On fait le lien entre
         * le gestionnaire de pagination et le gestionnaire des onglets
         */
        new TabLayoutMediator(gestionnaireOnglet, gestionnairePagination,
            (tab, position) -> {
                // Associer manuellement des vues personnalisées
                View customView = getLayoutInflater().inflate(R.layout.footer_icon_template, null);
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
                button.setImageResource(R.drawable.home);
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
                                    ? R.color.footer_icon_active
                                    : R.color.footer_icon_inactive,
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