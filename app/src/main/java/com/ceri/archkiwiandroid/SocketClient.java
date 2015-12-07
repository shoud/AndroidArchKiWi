package com.ceri.archkiwiandroid;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

class SocketClient {
    private final String hostName;
    private final int portNumber;

    public SocketClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public String send(String command) {
        try {
            Socket socket = new Socket(hostName, portNumber);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            bufferedWriter.write(command);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if (command.equals("E;PLSGIVINFO")) {
                String str = bufferedReader.readLine();
                return str;
            }

            bufferedWriter.close();
            bufferedReader.close();
        } catch (Exception e) {
            Log.e("Socket", e.toString());
        }
        return "";
    }
}
