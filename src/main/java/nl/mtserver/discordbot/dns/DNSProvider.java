package nl.mtserver.discordbot.dns;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.mtserver.discordbot.data.HikariSQL;

import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DNSProvider {

    /**
     * Create a domain provider if it doesn't already exist
     * @param domain the domain name associated with this DNSProvider
     * @return ID of DNS provider
     */
    static int firstOrCreate(String domain) {
        try(Connection connection = HikariSQL.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `dns_providers` (`domain`) VALUES (?)" +
                    "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, domain);
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();

            int dnsProviderId = -1;
            if(rs.next()) {
                dnsProviderId = rs.getInt(1);
            }

            rs.close();
            statement.close();
            return dnsProviderId;
        }catch(SQLException exception) {
            throw new RuntimeException("Failed to create DNS provider", exception);
        }
    }

    /**
     * Get the ID of this DNS provider
     * @return ID of this DNS provider
     */
    int getDNSProviderId();

    CompletableFuture<List<String>> createSubdomain(String subdomain, String ip, int port);

    /**
     * Create a DNS record
     * @param type The type of the record
     * @param name The name of the record
     * @param content The IP address or domain name
     * @param proxied Whether the record should be proxied
     * @return
     */
    CompletableFuture<String> createRecord(String type, String name, String content, boolean proxied);

    /**
     * Create a DNS record
     * @param type The type of the record
     * @param name The name of the record
     * @param content The IP address or domain name
     * @param data (optional)
     * @param proxied Whether the record should be proxied
     * @return ID of created DNS record
     * @throws RuntimeException if the request failed
     */
    CompletableFuture<String> createRecord(String type, String name, String content, JsonObject data, boolean proxied);

}
