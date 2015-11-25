package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class Wifi
{
    private Activity activity;
    private String networkSSID = "ArchKiWifi";
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private boolean waitingEnable, waitingConnect, checking;
    private ProgressDialog dialog;

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE: // Enable Wifi
                    if (!wifiManager.isWifiEnabled()) {
                        enable();
                    } else {
                        connect();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE: // Quit app
                    activity.finish();
                    break;
            }
            checking = false;
        }
    };

    public Wifi(Activity activity)
    {
        waitingEnable = false;
        checking = false;
        this.activity = activity;
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void enable() {
        waitingEnable = true;
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Activating...");
        dialog.setMessage("Please wait while\nwe activate the Wifi");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();
        wifiManager.setWifiEnabled(true);
    }

    public void connect() {
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
        boolean b = wifiManager.enableNetwork(networkId, true);
    }

    public int getConfig() {
        for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
            if (config.SSID.equals("\"ArchKiWifi\""))
                return config.networkId;
        }
        return -1;
    }

    public int addConfig() {
        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        return wifiManager.addNetwork(wifiConfiguration);
    }

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
