package edu.escuelaing.arep.apis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.escuelaing.arep.cache.Cache;

public class OMDbAPI {

    private static final Logger LOGGER = Logger
            .getLogger(OMDbAPI.class.getName());
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String URL_STRING = "http://www.omdbapi.com/";
    private static final String KEY_STRING = "35dbfc73";
    private static OMDbAPI instance;
    private int requestsToOMDbAPI;

    private OMDbAPI() {
        requestsToOMDbAPI = 0;
    }

    public static OMDbAPI getInstance() {
        if (instance == null) {
            instance = new OMDbAPI();
        }
        return instance;
    }

    public String requestByTitle(String title) {
        if (title != null && !title.isBlank()) {
            if (Cache.getInstance().contains(title)) {
                return Cache.getInstance().get(title);
            } else {
                return request(title);
            }
        }
        return "Title not valid";
    }

    private String request(String title) {
        try {
            URL url = new URL(
                    MessageFormat.format(
                            "{0}?t={1}&apikey={2}",
                            URL_STRING, title, KEY_STRING));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            // The following invocation perform the connection
            // implicitly before getting the code
            int responseCode = con.getResponseCode();
            LOGGER.log(Level.INFO,
                    "\n\tServer side\n\tGET Responde code {0}\n",
                    responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                requestsToOMDbAPI++;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                LOGGER.log(Level.INFO,
                        "\n\tServer side\n\tResponse\n\n{0}\n",
                        response);
                Cache.getInstance().save(title, response.toString());
                return response.toString();
            } else {
                LOGGER.log(Level.INFO, "\n\tServer side\n\tGET request not worked\n");
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO,
                    "\n\tServer side\n\tException\n\t{0}\n",
                    e.getMessage());
            e.printStackTrace();
        }
        return "Not found";
    }

    public void resetRequestsToOMDbAPI() {
        requestsToOMDbAPI = 0;
    }

    public int getRequestsToOMDbAPI() {
        return requestsToOMDbAPI;
    }
}
