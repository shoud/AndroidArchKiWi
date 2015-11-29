package com.ceri.archkiwiandroid;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;

class SocketClient {
    private final String hostName;
    private final int portNumber;

    public SocketClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public void send(String command) {
        try {
            Socket socket = new Socket(hostName, portNumber);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(command);
            bufferedWriter.close();
        } catch (Exception e) {
            Log.e("Socket", e.toString());
        }
    }
}
