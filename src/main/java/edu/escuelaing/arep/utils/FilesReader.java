package edu.escuelaing.arep.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class FilesReader {

    private static final String RESOURCES_DIR = "src/main/resources";
    private static final String INDEX_PAGE = "/index.html";
    private static final String NOT_FOUND_PAGE = "/404/404.html";
    private static final String HEADER = "HTTP/1.1 200 OK\r\n";
    private static final String CONTENT_TYPE_PNG = "Content-Type: image/png\r\n\r\n";
    private static final String CONTENT_TYPE_JPG = "Content-Type: image/jpg\r\n\r\n";
    private static final String CONTENT_TYPE_HTML = "Content-Type: text/html\r\n\r\n";
    private static final String CONTENT_TYPE_CSS = "Content-Type: text/css\r\n\r\n";
    private static final String CONTENT_TYPE_JS = "Content-Type: application/javascript\r\n\r\n";

    private FilesReader() {
    }

    public static void img(String path, OutputStream clientOutputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(
                    ImageIO.read(Paths.get("", (RESOURCES_DIR + path)
                            .replace("/", System.getProperty("file.separator")))
                            .toFile()),
                    "png",
                    outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(clientOutputStream);
            if (path.endsWith(".png")) {
                dataOutputStream.writeBytes(HEADER + CONTENT_TYPE_PNG);
            } else if (path.endsWith(".jpg")) {
                dataOutputStream.writeBytes(HEADER + CONTENT_TYPE_JPG);
            }
            dataOutputStream.write(outputStream.toByteArray());
            clientOutputStream.close();
        } catch (IOException e) {
            text(NOT_FOUND_PAGE, new PrintWriter(clientOutputStream));
        }
    }

    public static void text(String path, PrintWriter clientPrintWriter) {
        String line;
        StringBuilder response = new StringBuilder();

        if (path.isBlank() || path.equals("/")) {
            path = INDEX_PAGE;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(
                Paths.get("", (RESOURCES_DIR + path)
                        .replace("/", System.getProperty("file.separator")))
                        .toFile()))) {
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            text(NOT_FOUND_PAGE, clientPrintWriter);
        }
        if (path.endsWith(".css")) {
            clientPrintWriter.println(HEADER + CONTENT_TYPE_CSS + response);
        } else if (path.endsWith(".js")) {
            clientPrintWriter.println(HEADER + CONTENT_TYPE_JS + response);
        } else if (path.endsWith(".html")) {
            clientPrintWriter.println(HEADER + CONTENT_TYPE_HTML + response);
        }

        clientPrintWriter.close();
    }
}
