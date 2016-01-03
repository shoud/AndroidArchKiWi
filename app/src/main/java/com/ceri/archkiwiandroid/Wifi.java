package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

/**
 * Classe permettant de gérer le WIFi
 * Le wifi est utilisé pour communiquer avec le robot qui possède un hotpost
 */
public class Wifi
{
    //L'activité principale
    private final Activity activity;
    //Le nom de la connexion wifi
    private final String networkSSID = "ArchKiWifi";
    //Le mote de passe de la connexion wifi
    private final String networkPW = "archkiwi";
    //Permet de gérer la connexion wifi
    private final WifiManager wifiManager;
    //Pour connaitre le status du wifi
    private boolean waitingEnable, waitingConnect, checking;
    //Afficher des boites de dialogues pour le wifi
    private ProgressDialog dialog;

    //Demande à l'utilisateur d'activer le wifi s'il n'est pas activé
    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which)
            {
                //L'utilisateur veut activer son wifi
                case DialogInterface.BUTTON_POSITIVE:
                    //Si le wifi n'est pas activé
                    if (!wifiManager.isWifiEnabled()) {
                        //Activation du wifi
                        enable();
                    } else {
                        //Connexion du wifi
                        connect();
                    }
                    break;
                //Si l'utilisateur ne veut pas activer son wifi
                case DialogInterface.BUTTON_NEGATIVE:
                    //L'application se ferme
                    activity.finish();
                    break;
            }
            checking = false;
        }
    };

    /**
     * Permet d'initialiser le wifi manager
     * @param activity L'activity principale du programme
     */
    public Wifi(Activity activity) {
        //Initialisation des états à false
        waitingEnable = false;
        checking = false;
        //Récupération de l'activity principale
        this.activity = activity;
        //Initialisation du wifi manager
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * Permet d'activer le wifi
     */
    private void enable() {
        //Boite de dialogue qui permet d'afficher que le wifi s'active
        waitingEnable = true;
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Activating...");
        dialog.setMessage("Please wait while\nwe activate the Wifi");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
        wifiManager.setWifiEnabled(true);
    }

    /**
     * Permet de connecter le wifi au hotpost du robot
     */
    private void connect() {
        waitingConnect = true;
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Connecting...");
        dialog.setMessage("Please wait while\nwe connect to 'ArchKiWifi'");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
        int networkId = getConfig();
        if (networkId == -1)
            networkId = addConfig();
        wifiManager.enableNetwork(networkId, true);
    }

    /**
     * Récupération de la config du wifi
     * @return la config du wifi ArchKiWifi
     */
    private int getConfig() {
        //Parcourt de tout les wifi
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            //Récupération du bon wifi
            if(config.SSID.equals("\"ArchKiWifi\""))
                //Retourn la bonne config
                return config.networkId;
        }
        //Si le wifi n'est pas présent dans la liste
        return -1;
    }

    /**
     * Permet de rajouter la config wifi du robot dans le périphérique android
     * @return Si le wifi a bien été rajouté
     */
    private int addConfig()
    {
        //Création d'une nouvelle configuration de wifi
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        //Le nom du wifi
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        //Le mot de passe du wifi
        wifiConfiguration.preSharedKey = "\"" + networkPW + "\"";
        //Le type de mot de passe du wifi
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        //Mettre le wifi disponible
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        //Rajout de la nouvelle configuration
        return wifiManager.addNetwork(wifiConfiguration);
    }

    /**
     * Test si le pérophérique c'est connecté au wifi
     */
    public void check() {
        if (waitingConnect && wifiManager.getConnectionInfo().getSSID().equals("\"ArchKiWifi\"") && wifiManager.getConnectionInfo().getIpAddress() != 0) {
            dialog.dismiss();
            waitingConnect = false;
        }
        if (waitingEnable && wifiManager.isWifiEnabled()) {
            dialog.dismiss();
            waitingEnable = false;
            connect();
        }
        if (!waitingConnect && !waitingEnable && (!wifiManager.isWifiEnabled() || !wifiManager.getConnectionInfo().getSSID().equals("\"ArchKiWifi\""))) {
            checking = true;
            // Create DialogBuilder to let the user set Wifi state or quit the app
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("L'accès au réseau 'ArchKiWifi' est nécessaire\n" +
                    "pour utiliser cette application.")
                    .setPositiveButton("Activer", dialogClickListener)
                    .setNegativeButton("Quitter", dialogClickListener)
                    .setCancelable(false)
                    .show();
        }
    }

    public boolean isChecking() {
        return checking;
    }

    public boolean isConnected() {
        return wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo().getSSID().equals("\"ArchKiWifi\"") && wifiManager.getConnectionInfo().getIpAddress() != 0;
    }
}
