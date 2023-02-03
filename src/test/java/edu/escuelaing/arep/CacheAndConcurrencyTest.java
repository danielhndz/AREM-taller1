package edu.escuelaing.arep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import edu.escuelaing.arep.client.Client;
import edu.escuelaing.arep.server.HttpServer;

/**
 * Unit test for caché and concurrency.
 */
public class CacheAndConcurrencyTest {

    private static final Logger LOGGER = Logger
            .getLogger(CacheAndConcurrencyTest.class.getName());
    private static final String IP = "127.0.0.1";
    private static final int PORT = 35000;
    private static final String[] movies = {
            "The Shawshank Redemption",
            "The Godfather",
            "The Dark Knight",
            "The Godfather Part II"
    };
    private Thread serverThread;
    private HttpServer server;

    /**
     * Concurrent test with:
     * Max server threads = 1
     * Max client threads = 1
     */
    @Test
    public void concurrentTest1with1() {
        int cache = 1;
        int[] data = concurrentTest(1, 1, cache);
        if (data != null) {
            assertEquals(
                    "El número de solicitudes a OMDbAPI no es 1",
                    cache, data[0]);
            assertEquals(
                    "El tamaño de la caché no es 1",
                    cache, data[1]);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Concurrent test with:
     * Max server threads = 1
     * Max client threads = 100
     */
    @Test
    public void concurrentTest1with100() {
        int cache = 4;
        int[] data = concurrentTest(1, 100, cache);
        if (data != null) {
            assertEquals(
                    "El número de solicitudes a OMDbAPI no es 1",
                    cache, data[0]);
            assertEquals(
                    "El tamaño de la caché no es 1",
                    cache, data[1]);
        } else {
            assertTrue(false);
        }
    }

    private int[] concurrentTest(int maxServerThreads, int maxClientThreads, int cache) {
        int[] response = null;
        setupServer(maxServerThreads);
        setupOMDbAPI();
        ExecutorService pool = Executors.newFixedThreadPool(maxClientThreads);
        if (maxClientThreads >= cache) {
            Client[] clients = new Client[maxClientThreads];
            int num_movie = 0;
            for (int i = 0; i < maxClientThreads; i++) {
                if (num_movie == cache) {
                    num_movie = 0;
                }
                String movie = movies[num_movie].replace(" ", "+");
                clients[i] = new Client();
                Client client = clients[i];
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.startConnection(IP, PORT);
                            LOGGER.log(Level.INFO,
                                    "\n\tTest client side\n\tClient id={0} connected\n",
                                    client.getId());
                            String response = client.sendMessage(
                                    "GET /api/movies?t=" + movie + " HTTP/1.1");
                            if (response != null && !response.isBlank()) {
                                String msg = MessageFormat.format(
                                        "\n\tTest client side" +
                                                "\n\tClient {0}\n\tresponse\n\n{1}\n",
                                        client.getId(),
                                        response);
                                LOGGER.log(Level.INFO, msg);
                            }
                            client.stopConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                num_movie++;
            }
        }
        try {
            response = requestDataFromServer(pool);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopPool(pool);
        return response;
    }

    private int[] requestDataFromServer(ExecutorService pool) throws InterruptedException {
        MyRunnable runnable;
        int[] res = new int[2];
        runnable = new MyRunnable(
                "/api/movies/reqs_OMDbAPI",
                "127.0.0.1", 35000, new Client());
        pool.execute(runnable);
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\tWaiting for executor service ...\n");
        pool.awaitTermination(15, TimeUnit.SECONDS);
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\tExecutor service ends\n");
        res[0] = Integer.valueOf(runnable.getResponse().trim());
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\trequestsToOMDbAPI {0}\n", res[0]);

        runnable = new MyRunnable(
                "/api/movies/cache_size",
                "127.0.0.1", 35000, new Client());
        pool.execute(runnable);
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\tWaiting for executor service ...\n");
        pool.awaitTermination(15, TimeUnit.SECONDS);
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\tExecutor service ends\n");
        res[1] = Integer.valueOf(runnable.getResponse().trim());
        LOGGER.log(Level.INFO,
                "\n\tTest client side\n\tcacheSize {0}\n", res[1]);
        return res;
    }

    private void stopPool(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.WARNING, "\n\tPool did not terminate\n");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        serverThread.interrupt();
        try {
            server.getServerSocket().close();
            LOGGER.log(Level.INFO, "\n\tServer socket closed\n");
            server.stopWithoutExit();
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "\n\tClose server failed\n");
            e.printStackTrace();
        }
    }

    private void setupOMDbAPI() {
        Client client = new Client();
        try {
            client.startConnection(IP, PORT);
            LOGGER.log(Level.INFO,
                    "\n\tTest client side\n\trestart_all\n{0}\n",
                    client.sendMessage("/api/movies/restart_all"));
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupServer(int maxServerThreads) {
        server = HttpServer.getInstance();
        server.setMaxThreads(maxServerThreads);
        serverThread = new Thread(server);
        serverThread.start();
    }
}
