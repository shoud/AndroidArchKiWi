package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    Wifi wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifi = new Wifi(this);
        if(!wifi.getEtatWifi())
        {
            //Création d'un créateur de dialogue
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //On lui dit qu'il doit utiliser le xml wifi_dialog
            final View alertDialogView = inflater.inflate(R.layout.wifi_dialog, null);
            //Le titre de la fenêtre
            alertDialogBuilder.setTitle("Le wifi est désactivé");
            //On peut sortir de la fenêtre
            alertDialogBuilder.setCancelable(false);
            //On rajoute la vue au dialogue
            alertDialogBuilder.setView(alertDialogView);
            //Le bouton positif permet d'activer le wifi
            alertDialogBuilder.setPositiveButton("Activer le Wifi !", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Activation du wifi
                    wifi.setEtatWifi();
                }
            });
            alertDialogBuilder.setNegativeButton("Quitter ...", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Fermeture de l'application
                    finish();
                    System.exit(0);
                }
            });
            //Création de la boite de dialogue
            AlertDialog alertDialog = alertDialogBuilder.create();
            //Afficher la boite de dialogue
            alertDialog.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
