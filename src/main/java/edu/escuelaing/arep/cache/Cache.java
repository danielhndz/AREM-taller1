package edu.escuelaing.arep.cache;

import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private static Cache instance;
    private static ConcurrentHashMap<String, String> hashMap;

    private Cache() {
    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
            hashMap = new ConcurrentHashMap<>();
        }
        return instance;
    }

    public void save(String key, String value) {
        hashMap.put(key, value);
    }

    public boolean contains(String key) {
        return hashMap.containsKey(key);
    }

    public String get(String key) {
        return hashMap.get(key);
    }

    public void clear() {
        hashMap.clear();
    }

    public int size() {
        return hashMap.size();
    }
}
