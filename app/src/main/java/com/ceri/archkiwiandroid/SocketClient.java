package com.ceri.archkiwiandroid;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketClient
{
    private String hostName;
    private int portNumber;
    private Socket socket;
    private OutputStreamWriter outputStreamWriter;
    private BufferedWriter bufferedWriter;


    public SocketClient(String hostName, int portNumber)
    {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public void send(String command)
    {
        try
        {
            socket = new Socket(hostName,portNumber);
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(command);
            bufferedWriter.close();
        }catch (Exception e)
        {
            Log.e("Erreur write Socket = ",e.toString());
        }
    }
}
