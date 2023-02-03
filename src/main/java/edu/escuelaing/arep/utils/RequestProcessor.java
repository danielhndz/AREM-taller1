package edu.escuelaing.arep.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.escuelaing.arep.apis.OMDbAPI;
import edu.escuelaing.arep.cache.Cache;
import edu.escuelaing.arep.server.HttpServer;
import edu.escuelaing.arep.services.MovieSearchService;

public class RequestProcessor implements Runnable {

    private static final Logger LOGGER = Logger
            .getLogger(RequestProcessor.class.getName());
    private final Socket clientSocket;

    public RequestProcessor(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            String inputLine;
            StringBuilder request = new StringBuilder();
            BufferedReader out = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            while ((inputLine = out.readLine()) != null) {
                request = process(inputLine, request);
                if (!out.ready()) {
                    break;
                }
            }
            LOGGER.log(Level.INFO,
                    "\n\tServer side\n\tReceived: {0}\n", request);
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "\n\tServer side\n\tInterrupted!\n", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    private StringBuilder process(String inputLine, StringBuilder request)
            throws IOException, InterruptedException {
        String path;
        String method = parseMethod(inputLine);
        request.append(inputLine + "\n\t");
        if (method != null) {
            path = inputLine.replace(method + " ", "");
        } else {
            path = inputLine;
        }
        path = path.replace(" HTTP/1.0", "")
                .replace(" HTTP/1.1", "");
        if (path.startsWith("/")) {
            LOGGER.log(Level.INFO, "\n\tPath: {0}\n", path);
        }
        if (path.toLowerCase().startsWith("/exit")) {
            exit();
        } else if (path.startsWith("/api/movies")) {
            moviesAPI(path);
        } else if (path.endsWith(".png") || path.endsWith(".jpg")) {
            FilesReader.img(path, clientSocket.getOutputStream());
        } else {
            if (!clientSocket.isClosed()) {
                FilesReader.text(path,
                        new PrintWriter(
                                clientSocket.getOutputStream(),
                                true));
            }
        }
        return request;
    }

    private void moviesAPI(String path) throws IOException {
        PrintWriter in = new PrintWriter(clientSocket.getOutputStream(), true);
        if (path.startsWith("/api/movies/restart_all")) {
            Cache.getInstance().clear();
            in.println("\n\tClient side\n\tCache cleared ...");
            OMDbAPI.getInstance().resetRequestsToOMDbAPI();
            in.println("\n\tClient side" +
                    "\n\tOMDbAPI request counter restarted ...");
            in.close();
        } else if (path.startsWith("/api/movies/reqs_OMDbAPI")) {
            in.println(OMDbAPI.getInstance().getRequestsToOMDbAPI());
            in.close();
        } else if (path.startsWith("/api/movies/cache_size")) {
            in.println(Cache.getInstance().size());
            in.close();
        } else {
            MovieSearchService.search(path, in);
        }
    }

    private void exit() throws IOException, InterruptedException {
        PrintWriter in = new PrintWriter(clientSocket.getOutputStream(), true);
        in.println("HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n\r\n" +
                "\tClient side\n\tStopping server ...");
        in.close();
        HttpServer.getInstance().stop();
    }

    private String parseMethod(String inputLine) {
        if (inputLine.startsWith("GET")) {
            return "GET";
        } else if (inputLine.startsWith("POST")) {
            return "POST";
        }
        return null;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}