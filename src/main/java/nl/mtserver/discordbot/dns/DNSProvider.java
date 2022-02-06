package nl.mtserver.discordbot.dns;

import nl.mtserver.discordbot.data.HikariSQL;
import nl.mtserver.discordbot.data.Subdomain;

import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DNSProvider {

    /**
     * Create a domain provider if it doesn't already exist
     * @param domain the domain name associated with this DNSProvider
     * @return ID of DNS provider
     */
    public static int firstOrCreate(String domain) {
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

    public CompletableFuture<String> createSubdomain(DNSProvider dnsProvider, long userId, String subdomain, String ip, int port) {
        return dnsProvider.createSubdomain(subdomain, ip, port).thenApply(list -> {
            if (list.size() != 2) {
                return "Er is iets misgegaan tijdens het aanmaken van dit subdomein.";
            }

            // Create database rows
            if (Subdomain.findOrCreate(subdomain, userId, dnsProvider) == null) {
                return "Er is iets misgegaan tijdens het aanmaken van dit subdomein.";
            }
            return subdomain.toLowerCase() + "." + dnsProvider.getDomainName() + " succesvol aangemaakt.";
        });
    }

    /**
     * Get the ID associated with this DNS provider
     * @return ID of this DNS provider
     */
    public abstract int getDNSProviderId();

    /**
     * Get the domain name associated with this DNS provider
     * @return domain name of this DNS provider
     */
    public abstract String getDomainName();

    protected abstract CompletableFuture<List<String>> createSubdomain(String subdomain, String ip, int port);
}
