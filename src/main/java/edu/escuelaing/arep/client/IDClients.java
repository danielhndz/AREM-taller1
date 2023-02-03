package edu.escuelaing.arep.client;

public class IDClients {
    private static int nextId = 0;

    private IDClients() {
    }

    public static int nextId() {
        return nextId++;
    }
}
