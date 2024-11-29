package fr.iutrodez.sae501.cliandcollect;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PageConnexion extends AppCompatActivity {

    private ActivityResultLauncher<Intent> lanceurInscription;
    private EditText mail;
    private EditText mdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_connexion);

        mail = findViewById(R.id.saisieMdp);
        mdp = findViewById(R.id.saisieMdp);
        Button boutonConnexion = findViewById(R.id.boutonConnexion);
        Button boutonInscription = findViewById(R.id.boutonInscription);


        boutonConnexion.setOnClickListener(this::clicConnexion);
        boutonInscription.setOnClickListener(this::clicInscription);
    }

    private void clicConnexion(View bouton) {
        Intent menuPrincipal = new Intent(PageConnexion.this, MainActivity.class);
        startActivity(menuPrincipal);
    }

    private void clicInscription(View bouton) {
        Intent incription = new Intent(PageConnexion.this, PageInscription.class);
        startActivity(incription);
    }
}