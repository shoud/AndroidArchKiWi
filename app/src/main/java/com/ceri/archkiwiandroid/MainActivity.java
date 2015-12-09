package com.ceri.archkiwiandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
    private long lastSpeedRequest;
    private VocalRecognizer vocalRecognizer;
    public static final int GAUCHE = 0, HAUT = 0;
    public static final int DROITE = 180, BAS = 180;
    public static final int STOP = -1;

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
        vocalRecognizer = new VocalRecognizer(this);
    }

    private void getSpeed() {
        long now = System.currentTimeMillis();
        final String response;
        if (now - lastSpeedRequest > 1000) {
            response = socketClient.send("E;PLSGIVINFO");
            lastSpeedRequest = now;
            final TextView speedView = (TextView) findViewById(R.id.speedText);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speedView.setText(response + " m/s");
                }
            });
        }
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

        //Récupération du Joysticks du control
        JoystickView joystickMotor = (JoystickView) findViewById(R.id.joystickMotor);
        joystickMotor.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                final String str = "M;" + angle + ";" + power + ";";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketClient.send(str);
                        getSpeed();
                    }
                }).start();

            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        //Récupération du de la camera
        JoystickView joystickCamera = (JoystickView) findViewById(R.id.joystickCamera);
        joystickCamera.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                final String str;
                switch (direction) {
                    case JoystickView.FRONT:
                        str = "C;" + STOP + ";" + HAUT + ";";
                        break;

                    case JoystickView.FRONT_RIGHT:
                        str = "C;" + DROITE + ";" + HAUT + ";";
                        break;

                    case JoystickView.RIGHT:
                        str = "C;" + DROITE + ";" + STOP + ";";
                        break;

                    case JoystickView.RIGHT_BOTTOM:
                        str = "C;" + DROITE + ";" + BAS + ";";
                        break;

                    case JoystickView.BOTTOM:
                        str = "C;" + STOP + ";" + BAS + ";";
                        break;

                    case JoystickView.BOTTOM_LEFT:
                        str = "C;" + GAUCHE + ";" + BAS + ";";
                        break;

                    case JoystickView.LEFT:
                        str = "C;" + GAUCHE + ";" + STOP + ";";
                        break;

                    case JoystickView.LEFT_FRONT:
                        str = "C;" + GAUCHE + ";" + HAUT + ";";
                        break;

                    default:
                        str = "C;" + STOP + ";" + STOP + ";";
                }
                Log.e("CMD cam : ", str);
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

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ArrayList<String> results;
            results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(this, results.get(0), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Méthode d'utiliser la reconaissance vocal
     * @param controlView
     */
    public void btVocalRecognizer(View controlView)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, 0);
    }
}
