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

    //L'adresse ip wifi du robot
    private final String hostname = "192.168.2.254";
    //Le port pour obtenir le stream
    private final String portStream = "8080";
    //Le port pour envoyer les sockets
    private final int portSocket = 20000;
    //Pour gérer la connexion wifi avec le robot
    private Wifi wifi;
    private boolean running;
    //Pour afficher le stream vidéo
    private MjpegView mv;
    //Socket pour lancer les commandes moteur et camera
    private SocketClient socketClient;
    private long lastSpeedRequest;
    //Pour utiliser la reconaissance vocale
    private VocalRecognizer vocalRecognizer;
    //Les commandes pour la camera
    private static final int MAX_POWER = 100;
    private static final int H_CENTER = 1300;
    private static final int H_RIGHT = 1600;
    private static final int H_LEFT = 1000;
    private static final int V_CENTER = 1500;
    private static final int V_RIGHT = 1100;
    private static final int V_LEFT = 1900;
    private static final int H_RATIO1 = (H_CENTER - H_LEFT) / MAX_POWER;
    private static final int H_RATIO2 = (H_RIGHT - H_CENTER) / MAX_POWER;
    private static final int V_RATIO1 = (H_CENTER - H_LEFT) / MAX_POWER;
    private static final int V_RATIO2 = (H_RIGHT - H_CENTER) / MAX_POWER;
    private static final int STOP = -1;

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

    /**
     * Pour utiliser les sockets dans un thread
     */
    private void start() {
        running = true;
        //Initialisation de la socket
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient = new SocketClient(hostname, portSocket);
                    if(!socketClient.initSocket())
                    {
                        Log.e("Error socket","socket not create");
                    }
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
                //Creation de la commande on fonction de l'angle et de la puissance du joystik
                final String str = "M;" + angle + ";" + power + ";";
                //Creation d'une Thread pour lancer la commande et récupérer la vitesse
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Envoie de la commande
                        socketClient.send(str);
                        //Mise à jour de la vitesse
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
                //La commande a envoyer
                final String str;
                //Permet de définir où la camera doit se déplacer en fonction du joytstik
                switch (direction) {
                    case JoystickView.FRONT:
                        str = "C;" + STOP + ";" + (V_CENTER + V_RATIO1*power) + ";";
                        break;

                    case JoystickView.FRONT_RIGHT:
                        str = "C;" + (H_CENTER + H_RATIO1*power) + ";" + (V_CENTER + V_RATIO1*power) + ";";
                        break;

                    case JoystickView.RIGHT:
                        str = "C;" + (H_CENTER + H_RATIO1*power) + ";" + STOP + ";";
                        break;

                    case JoystickView.RIGHT_BOTTOM:
                        str = "C;" + (H_CENTER + H_RATIO1*power) + ";" + (V_CENTER - V_RATIO2*power) + ";";
                        break;

                    case JoystickView.BOTTOM:
                        str = "C;" + STOP + ";" + (V_CENTER - V_RATIO2*power) + ";";
                        break;

                    case JoystickView.BOTTOM_LEFT:
                        str = "C;" + (H_CENTER - H_RATIO2*power) + ";" + (V_CENTER - V_RATIO2*power) + ";";
                        break;

                    case JoystickView.LEFT:
                        str = "C;" + (H_CENTER - H_RATIO2*power) + ";" + STOP + ";";
                        break;

                    case JoystickView.LEFT_FRONT:
                        str = "C;" + (H_CENTER - H_RATIO2*power) + ";" + (V_CENTER + V_RATIO1*power) + ";";
                        break;

                    default:
                        str = "C;" + STOP + ";" + STOP + ";";
                }
                //Envoie de la commande dans un Thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketClient.send(str);
                    }
                }).start();
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        //Récupération de la view pour afficher le stream
        mv = (MjpegView) findViewById(R.id.mjpegView);
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        //Cacher le nombre d'images par seconde
        mv.showFps(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Récupération du stream
                    MjpegInputStream stream = MjpegInputStream.read("http://" + hostname + ":" + portStream + "/?action=stream");
                    //Affichage du stream
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
