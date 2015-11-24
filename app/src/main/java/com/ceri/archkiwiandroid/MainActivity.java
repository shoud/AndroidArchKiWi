package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;
import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;

public class MainActivity extends Activity {

    private WifiManager wifiManager;
    private Timer timer;
    private TimerTask task;
    private boolean checking;
    private JoystickView joystickMotor, joystickCamera;
    private MjpegView mv;
    private SocketClient socketClient;
    private String hostname = "192.168.2.254";
    private String portStream = "8080";
    private int portSocket = 20000;

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

        //Initialisation de la socket

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    socketClient = new SocketClient(hostname,portSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Récupération des Joysticks
        joystickMotor = (JoystickView) findViewById(R.id.joystickMotor);
        joystickMotor.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction)
            {
                final String str = "M;"+angle+";"+power+";";
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        socketClient.send(str);
                    }
                }).start();

            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        //Récupération des Joysticks
        joystickCamera = (JoystickView) findViewById(R.id.joystickCamera);
        joystickCamera.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction)
            {
                final String str = "C;"+angle+";"+power+";";
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        socketClient.send(str);
                    }
                }).start();
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        mv = (MjpegView) findViewById(R.id.mjpegView);
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(false);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    MjpegInputStream stream = MjpegInputStream.read("http://"+ hostname +":" + portStream+"/?action=stream");
                    mv.setSource(stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
