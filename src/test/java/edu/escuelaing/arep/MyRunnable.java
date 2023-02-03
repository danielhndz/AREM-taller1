package edu.escuelaing.arep;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.escuelaing.arep.client.Client;

public class MyRunnable implements Runnable {

    private static final Logger LOGGER = Logger
            .getLogger(MyRunnable.class.getName());
    private String command;
    private String response;
    private final String IP;
    private final int PORT;
    private Client client;

    public MyRunnable(String command, String ip, int port, Client client) {
        this.command = command;
        this.IP = ip;
        this.PORT = port;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            client.startConnection(IP, PORT);
            LOGGER.log(Level.INFO,
                    "\n\tTest client side\n\tClient id={0} connected\n",
                    client.getId());
            response = client.sendMessage(command);
            if (response != null && !response.isBlank()) {
                String msg = MessageFormat.format("\n\tTest client side" +
                        "\n\tClient {0}\n\tcommand {1}\n\tresponse {2}\n",
                        client.getId(),
                        command,
                        response);
                LOGGER.log(Level.INFO, msg);
            } else {
                throw new IOException();
            }
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage(), e);
        }
    }

    public String getResponse() {
        return response;
    }
}
