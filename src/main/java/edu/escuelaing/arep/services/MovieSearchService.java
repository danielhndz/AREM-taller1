package edu.escuelaing.arep.services;

import java.io.PrintWriter;

import edu.escuelaing.arep.apis.OMDbAPI;

public class MovieSearchService {

    private static final String HEADER = "HTTP/1.1 200 OK\r\n";
    private static final String CONTENT_TYPE_JSON = "Content-Type: application/json\r\n\r\n";

    private MovieSearchService() {
    }

    public static void search(String path, PrintWriter clientPrintWriter) {
        if (path.startsWith("/api/movies?t=")) {
            StringBuilder response = new StringBuilder(HEADER);
            response.append(CONTENT_TYPE_JSON);
            response.append(OMDbAPI.getInstance().requestByTitle(path.replace("/api/movies?t=", "")));
            clientPrintWriter.println(response);
            clientPrintWriter.close();
        }
    }
}
