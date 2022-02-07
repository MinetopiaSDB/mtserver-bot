package nl.mtserver.discordbot.data;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HikariSQL {

    public static HikariSQL instance = null;
    private HikariDataSource hikari;

    public static HikariSQL getInstance() {
        if (instance == null) {
            instance = new HikariSQL();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return getHikari().getConnection();
    }

    public HikariDataSource getHikari() {
        return hikari;
    }

    public void setup(String ip, int port, String username, String password, String dbname) {
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");

        hikari.addDataSourceProperty("serverName", ip);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", dbname);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.setMaxLifetime(480000);

        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setPoolName("MTServerPool");

        createTables();
    }


    private void createTables() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dns_providers " +
                    "(id INTEGER NOT NULL AUTO_INCREMENT, " +
                    "domain VARCHAR(63) NOT NULL UNIQUE, " +
                    "PRIMARY KEY(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS subdomains " +
                    "(id INTEGER NOT NULL AUTO_INCREMENT, " +
                    "subdomain VARCHAR(63) NOT NULL UNIQUE, " +
                    "user_id BIGINT(18) NOT NULL, " +
                    "dns_provider_id INTEGER NOT NULL," +
                    "FOREIGN KEY (dns_provider_id) REFERENCES dns_providers(id)," +
                    "PRIMARY KEY(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users " +
                    "(id BIGINT(18) NOT NULL, " +
                    "max_subdomains INT NOT NULL, " +
                    "PRIMARY KEY(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dns_records " +
                    "(id INTEGER NOT NULL AUTO_INCREMENT, " +
                    "record_id VARCHAR(32) NOT NULL," +
                    "subdomain_id INTEGER NOT NULL," +
                    "FOREIGN KEY (subdomain_id) REFERENCES subdomains(id) ON DELETE CASCADE," +
                    "PRIMARY KEY(id))");

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create tables", exception);
        }
    }
}
