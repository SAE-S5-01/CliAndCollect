package fr.iutrodez.sae501.cliandcollect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;

public class PageInscription extends AppCompatActivity {

    private  EditText mail;
    private  EditText mdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_inscription);
        mail = findViewById(R.id.saisieMail);
        mdp =  findViewById(R.id.saisieMdp);
        Button boutonSubmitInscription = findViewById(R.id.boutonInscription);
        boutonSubmitInscription.setOnClickListener(this::inscription);
    }

    private void inscription(View view) {
        Intent menuPrincipal = new Intent(PageInscription.this, MainActivity.class);
        startActivity(menuPrincipal);
    }

}