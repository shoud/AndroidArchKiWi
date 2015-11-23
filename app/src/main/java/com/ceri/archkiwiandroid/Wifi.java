package com.ceri.archkiwiandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;

/**
 * Classe permettant de gérer le wifi de l'application
 * Created by thomas on 23/11/2015.
 */
public class Wifi
{
    //Le wifimanager permettant de gérer le wifi
    final WifiManager wifiManager;
    //Le context de l'application permettant de créer le wifi manager
    Context context;

    /**
     * Constructeur de la classe wifi
     * Il permet de créer le wifimanger
     * @param context Le context de l'application
     */
    public Wifi(Context context)
    {
        //Récupération du context
        this.context = context;
        //Création du wifi manager
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Méthode permettant de renvoyer si le wifi et activé ou non
     * @return true Si le wifi est activé
     * @return false Si le wifi est désactivé
     */
    public boolean getEtatWifi()
    {
        //Récupération de l'état du wifi
        if(wifiManager.isWifiEnabled())
            //Wifi activé
            return true;
        //Wifi désactivé
        return false;
    }

    /**
     * Méthode permettant de changer l'état du wifi
     * Si le wifi était désactivé alors il s'active.
     */
    public void setEtatWifi()
    {
        //Récupération de l'état du wifi
        if(getEtatWifi())
            //Désactivé si activé
            wifiManager.setWifiEnabled(false);
        else
            //Activé si désactivé
            wifiManager.setWifiEnabled(true);
    }
}
