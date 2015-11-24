package com.ceri.archkiwiandroid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by uapv1301804 on 24/11/15.
 */
public class SocketClient
{
    private String hostName;
    private int portNumber;
    private Socket socket;
    private OutputStreamWriter outputStreamWriter;
    private BufferedWriter bufferedWriter;


    public SocketClient(String hostName,int portNumber)
    {
        this.hostName = hostName;
        this.portNumber = portNumber;
        try
        {
            socket = new Socket(hostName,portNumber);
        }catch (Exception e)
        {
            Log.e("Erreur create Socket = ",e.toString());
        }

    }

    public void send(String command)
    {
        try
        {
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(command);
        }catch (Exception e)
        {
            Log.e("Erreur write Socket = ",e.toString());
        }
    }

    public void destroy()
    {
        try
        {
            bufferedWriter.close();
        }catch (Exception e)
        {
            Log.e("Erreur close Socket = ",e.toString());
        }

    }
}
