package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private WifiManager wifiManager;
    private MediaPlayer mp;
    private SurfaceView view;
    private SurfaceHolder holder;
    private Timer timer;
    private TimerTask task;
    private boolean checking;

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE: // Enable Wifi
                    wifiManager.setWifiEnabled(true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE: // Quit app
                    finish();
                    break;
            }
            checking = false;
        }
    };

    private void checkWifiState() {
        checking = true;
        if(!wifiManager.isWifiEnabled())
        {
            // Create DialogBuilder to let the user set Wifi state or quit the app
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Le Wifi est nécessaire pour utiliser cette application." +
                    "\nQue souhaitez-vous faire ?")
                    .setPositiveButton("Activer", dialogClickListener)
                    .setNegativeButton("Quitter", dialogClickListener)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get WifiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Prepare a scheduled timer to check Wifi state periodically
        checking = false;
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() { // AlertDialog must be run on UI thread
                    @Override
                    public void run() {
                        if (!checking)
                            checkWifiState();
                    }
                });
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 5000); // Check Wifi state every 5 seconds

        // Get SurfaceView from layout, and get holder which will display video
        view = (SurfaceView) findViewById(R.id.surfaceView);
        holder = view.getHolder();
        holder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // Create MediaPlayer with the Rapsberry Camera Module streamin URI
            mp = MediaPlayer.create(this, Uri.parse("rtsp://192.168.1.14:8554/"));
            // Set display with the surface holder
            mp.setDisplay(holder);
            // Buffer and start video
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            Log.e("Player", e.getMessage(), e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
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
