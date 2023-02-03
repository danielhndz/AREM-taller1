package edu.escuelaing.arep.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

public class Client {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int id;

    public Client() {
        id = IDClients.nextId();
    }

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) {
        StringBuilder res = new StringBuilder();
        out.println(msg);
        Iterator<String> it = in.lines().iterator();
        while (it.hasNext()) {
            res.append(it.next());
            if (it.hasNext()) {
                res.append("\n");
            }
        }
        return res.toString();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public int getId() {
        return id;
    }
}
