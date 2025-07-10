package com.market.database;

// DatabaseContextHolder.java
public class DatabaseContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setCurrentDatabase(String database) {
        CONTEXT.set(database);
    }

    public static String getCurrentDatabase() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}