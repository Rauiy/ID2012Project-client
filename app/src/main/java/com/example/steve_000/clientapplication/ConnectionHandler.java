package com.example.steve_000.clientapplication;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * Created by steve_000 on 2017-05-06.
 */
public class ConnectionHandler{
    private final String host;
    private final int port;
    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ConnectionHandler() {
        this("192.168.1.10", 4444);
    }

    public ConnectionHandler(String host){
        this(host,4444);
    }

    public ConnectionHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void initiateStreams() throws IOException{
        clientSocket = new Socket(host, port);

        reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

        writer = new BufferedWriter(
                new OutputStreamWriter(clientSocket.getOutputStream()));
    }

    public JSONObject request(String request) throws JSONException {
        JSONObject jo = new JSONObject();
        try {
            initiateStreams();
        } catch (IOException e) {
            jo.put("status", "error");
            jo.put("result", "init");
            return jo;
        }

        try {
            write(request);
        } catch (IOException e) {
            jo.put("status", "error");
            jo.put("result", "write");
            return jo;
        }

        String res = null;
        try {
            res = getNext();
        } catch (IOException e) {
            jo.put("status", "error");
            jo.put("result", "read");
            return jo;
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            jo.put("socket", "fail");
        }

        jo.put("status", "success");
        jo.put("result", res);
        return jo;
    }

    public String getNext() throws IOException{
        String part;
        StringBuilder sb = new StringBuilder();
        while((part = reader.readLine()) != null){
            if(part.equals(""))
                break;

            sb.append(part + "\r\n");
        }

        return sb.toString();
    }

    public void write(String out) throws IOException{
        out = out + "\r\n\r\n";
        writer.write(out, 0, out.length());
        writer.flush();
    }
}
