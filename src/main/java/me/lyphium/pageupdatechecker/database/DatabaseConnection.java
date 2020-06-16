package me.lyphium.pageupdatechecker.database;

import com.zaxxer.hikari.HikariDataSource;
import me.lyphium.pageupdatechecker.checker.PageUpdate;
import me.lyphium.pageupdatechecker.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<PageUpdate> getPages() {
        final String sql = "SELECT id, name, url, lastUpdate, content, mail from pages;";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet set = statement.executeQuery()) {

            final List<PageUpdate> data = new ArrayList<>();

            while (set.next()) {
                final int id = set.getInt("id");
                final String name = set.getString("name");
                final String url = set.getString("url");
                final long lastUpdate = set.getTimestamp("lastUpdate").getTime();
                final String content = set.getString("content");
                final String mail = set.getString("mail");

                final PageUpdate priceData = new PageUpdate(id, name, url, lastUpdate, content, mail);
                data.add(priceData);
            }

            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PageUpdate getPageByID(int id) {
        final String sql = "SELECT name, url, lastUpdate, content, mail FROM pages WHERE id = ? LIMIT 1;";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                final String name = set.getString("name");
                final String url = set.getString("url");
                final long lastUpdate = set.getTimestamp("lastUpdate").getTime();
                final String content = set.getString("content");
                final String mail = set.getString("mail");

                return new PageUpdate(id, name, url, lastUpdate, content, mail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PageUpdate getPageByName(String name) {
        final String sql = "SELECT id, url, lastUpdate, content, mail FROM pages WHERE LOWER(name) = LOWER(?) LIMIT 1;";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                final int id = set.getInt("id");
                final String url = set.getString("url");
                final long lastUpdate = set.getTimestamp("lastUpdate").getTime();
                final String content = set.getString("content");
                final String mail = set.getString("mail");

                return new PageUpdate(id, name, url, lastUpdate, content, mail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PageUpdate getMostSimilarPage(String name) {
        final List<PageUpdate> pages = getPages();

        if (pages == null || pages.isEmpty()) {
            return null;
        }

        name = name.toLowerCase();

        int minDis = Integer.MAX_VALUE;
        PageUpdate data = null;
        for (PageUpdate page : pages) {
            final int dis = Utils.distance(name, page.getName().toLowerCase());

            if (dis < minDis) {
                data = page;
                minDis = dis;
            }
        }

        return data;
    }

    public PageUpdate getPageByUrl(String url) {
        final String sql = "SELECT id, name, lastUpdate, content, mail FROM pages WHERE LOWER(url) = LOWER(?) LIMIT 1;";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, url);

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return null;
                }

                final int id = set.getInt("id");
                final String name = set.getString("name");
                final long lastUpdate = set.getTimestamp("lastUpdate").getTime();
                final String content = set.getString("content");
                final String mail = set.getString("mail");

                return new PageUpdate(id, name, url, lastUpdate, content, mail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean savePages(List<PageUpdate> pages) {
        if (pages.size() == 0) {
            return true;
        }

        final String sql = "UPDATE pages SET lastUpdate = ?, content = ? WHERE id = ?;";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            long i = 0;
            final Timestamp time = new Timestamp(0);
            for (PageUpdate page : pages) {
                time.setTime(page.getLastUpdate());

                statement.setTimestamp(1, time);
                statement.setString(2, page.getContent());
                statement.setInt(3, page.getId());

                statement.addBatch();
                i++;

                if (i % 1000 == 0 || i == pages.size()) {
                    statement.executeBatch();
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPage(PageUpdate page) {
        if (getPageByName(page.getName()) != null) {
            return false;
        }

        final String sql = "INSERT INTO pages (name, url, mail) VALUES (?, ?, ?);";

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, page.getName());
            statement.setString(2, page.getUrl());
            statement.setString(3, page.getMail());

            return statement.executeUpdate() != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removePage(PageUpdate page) {
        final String sql;
        if (page.getId() > -1) {
            if (page.getName() != null) {
                sql = "DELETE FROM pages WHERE id = ? AND LOWER(name) = LOWER(?);";
            } else {
                sql = "DELETE FROM pages WHERE id = ?;";
            }
        } else if (page.getName() != null) {
            sql = "DELETE FROM pages WHERE LOWER(name) = LOWER(?);";
        } else {
            return false;
        }

        try (Connection con = source.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            int i = 1;
            if (page.getId() > -1) {
                statement.setInt(i++, page.getId());
            }
            if (page.getName() != null) {
                statement.setString(i, page.getName());
            }

            return statement.executeUpdate() != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        try (Connection con = source.getConnection()) {
            return con.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

}