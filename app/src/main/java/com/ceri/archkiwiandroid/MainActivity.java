package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private final String hostname = "192.168.2.254";
    private final String portStream = "8080";
    private final int portSocket = 20000;
    private Wifi wifi;
    private boolean running;
    private MjpegView mv;
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get WifiManager
        wifi = new Wifi(this);

        // Prepare a scheduled timer to check Wifi state periodically
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() { // AlertDialog must be run on UI thread
                    @Override
                    public void run() {
                        if (wifi.isConnected()) {
                            if (!running)
                                start();
                        } else {
                            running = false;
                        }
                        if (!wifi.isChecking())
                            wifi.check();
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 1000); // Check Wifi state every second
    }

    private void start() {
        running = true;
        //Initialisation de la socket
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient = new SocketClient(hostname, portSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Récupération des Joysticks
        JoystickView joystickMotor = (JoystickView) findViewById(R.id.joystickMotor);
        joystickMotor.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                final String str = "M;" + angle + ";" + power + ";";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketClient.send(str);
                    }
                }).start();

            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        //Récupération des Joysticks
        JoystickView joystickCamera = (JoystickView) findViewById(R.id.joystickCamera);
        joystickCamera.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                final String str = "C;" + angle + ";" + power + ";";
                new Thread(new Runnable() {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MjpegInputStream stream = MjpegInputStream.read("http://" + hostname + ":" + portStream + "/?action=stream");
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
