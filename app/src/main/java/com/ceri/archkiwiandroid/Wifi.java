package com.ceri.archkiwiandroid;

import android.net.wifi.WifiConfiguration;

/**
 * Created by uapv1301804 on 24/11/15.
 */
public class Wifi
{
    String networkSSID = "ArchKiWifi";
    WifiConfiguration wifiConfiguration;

    public Wifi()
    {
        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    }

    public WifiConfiguration getWifiConfiguration()
    {
        return wifiConfiguration;
    }
    public String getNetworkSSID()
    {
        return networkSSID;
    }
}
