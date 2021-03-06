package com.ceri.archkiwiandroid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketClient {
    private String hostName = null;
    private int portNumber =0;
    private Socket socket = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedWriter bufferedWriter = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader bufferedReader = null;
    private long lastMsg = 0;
    private boolean sending = false;

    /**
     * Initialise l'adresse ip et le port pour la socket
     * @param hostName l'adresse ip du robot
     * @param portNumber le port utilisé pour la socket
     */
    public SocketClient(String hostName, int portNumber)
    {
        //L'adresse ip de l'hote
        this.hostName = hostName;
        //Le port de l'hôte
        this.portNumber = portNumber;
    }

    /**
     * Initialise la connexion de la socket
     * @return true si la socket est bien créé
     * @return false si la socket n'est pas créé
     */
    public boolean initSocket()
    {
        try
        {
            //Initialisation de la socket
            socket = new Socket(hostName, portNumber);
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            return true;
        }catch (Exception e) {
            Log.e("Create socket", e.toString());
            return false;
        }
    }

    /**
     * Envoie la chaine de caractère représentant la requet dans la socket
     * @param command La commande a envoyer
     * @return La réponse reçu (la vitesse du robot)
     */
    public String send(String command) {
        long now = System.currentTimeMillis();
        if (now > lastMsg + 50 || command.equals("M;0;0;")) {
            try {
                bufferedWriter.write(command);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (command.equals("E;PLSGIVINFO")) {
                    String str = bufferedReader.readLine();
                    return str;
                }
            } catch (Exception e) {
                Log.e("Socket", e.toString());
                initSocket();
            }
            lastMsg = now;
        }
        return "";
    }

    public void send(final String command, final int duration) {
        if (!sending) {
            sending = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();
                    /*
                    On pourrait envoyer la commande une seule fois mais
                    le trigger du télémètre se fait à la réception d'une commande
                    Donc pour check à chaque instant s'il y a un obstacle, il faut spammer
                     */
                    while (now > System.currentTimeMillis() - duration)
                        send(command);
                    send("M;0;0;"); // On coupe les moteurs une fois l'action terminée
                    sending = false;
                }
            }).start();
        }
    }

    /**
     * Ferme proprement la socket
     */
    public void close()
    {
        try
        {
            bufferedWriter.close();
            bufferedReader.close();
            socket.close();
        }catch (Exception e) {
            Log.e("close socket", e.toString());
        }
    }
}
