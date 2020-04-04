package me.lyphium.pageupdatechecker.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    private final HikariDataSource source;

    public DatabaseConnection(String host, int port, String database, String username, String password) {
        this.source = new HikariDataSource();

        source.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        source.setUsername(username);
        source.setPassword(password);

        source.addDataSourceProperty("serverTimezone", "Europe/Berlin");
        source.addDataSourceProperty("connectionTimeout", 5000);
        source.addDataSourceProperty("cachePrepStmts", true);
        source.addDataSourceProperty("prepStmtCacheSize", 250);
        source.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        source.addDataSourceProperty("useServerPrepStmts", true);
        source.addDataSourceProperty("useLocalSessionState", true);
        source.addDataSourceProperty("rewriteBatchedStatements", true);
        source.addDataSourceProperty("cacheResultSetMetadata", true);
        source.addDataSourceProperty("cacheServerConfiguration", true);
        source.addDataSourceProperty("elideSetAutoCommits", true);
        source.addDataSourceProperty("maintainTimeStats", false);
    }

    public void stop() {
        source.close();

        System.out.println("Shut down Database Manager");
    }

    public boolean isConnected() {
        try (Connection con = source.getConnection()) {
            return con.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

}