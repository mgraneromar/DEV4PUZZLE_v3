package com.example.dev4puzzle_v3;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Idioma extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idioma);

        Button btnEspaniol = findViewById(R.id.button_español);
        Button btnEnglish = findViewById(R.id.button_english);
        Button btnCatala = findViewById(R.id.button_catala);

        btnEspaniol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale localizacion = new Locale("es", "ES");

                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(Idioma.this, "Cambiado idioma a Español", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MenuPrincipal.class);
                startActivity(intent);
            }
        });

        btnEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale localizacion = new Locale("en", "US");
                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(Idioma.this, "Change language English", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MenuPrincipal.class);
                startActivity(intent);
            }
        });

        btnCatala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale localizacion = new Locale("ca");

                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(Idioma.this, "Canviat l'idioma a català", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),MenuPrincipal.class);
                startActivity(intent);
            }
        });

    }
}