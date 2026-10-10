package com.fittrack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {}

    // Prefer environment variables so credentials are not committed to source control.
    private static final String URL = env("FITTRACK_DB_URL",
            "jdbc:mysql://localhost:3306/fitness_tracking?useSSL=false&serverTimezone=UTC");
    private static final String USER = env("FITTRACK_DB_USER", "root");
    private static final String PASSWORD = env("FITTRACK_DB_PASSWORD", "");

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
