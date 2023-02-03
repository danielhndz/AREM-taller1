package edu.escuelaing.arep;

import edu.escuelaing.arep.server.HttpServer;

public class Launcher {
    public static void main(String[] args) {
        HttpServer server = HttpServer.getInstance();
        if (args != null && args.length > 0) {
            try {
                server.setMaxThreads(Integer.valueOf(args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        server.start();
    }
}
