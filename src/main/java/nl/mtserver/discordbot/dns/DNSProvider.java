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

    public static CompletableFuture<String> createSubdomain(DNSProvider dnsProvider, long userId, String subdomainStr, String ip, int port) {
        return dnsProvider.createSubdomain(subdomainStr.toLowerCase(), ip, port).thenApply(recordIds -> {
            if (recordIds.size() != 2) {
                return "Er is iets misgegaan tijdens het aanmaken van dit subdomein.";
            }

            // Create database rows
            Subdomain subdomain = Subdomain.findOrCreate(subdomainStr.toLowerCase(), userId, dnsProvider);
            if (subdomain == null) {
                return "Er is iets misgegaan tijdens het aanmaken van dit subdomein.";
            }
            recordIds.forEach(recordId -> DNSRecord.create(recordId, subdomain));

            return "Het subdomein " + subdomainStr.toLowerCase() + "." + dnsProvider.getDomainName() + " is succesvol aangemaakt!";
        });
    }

    public static CompletableFuture<String> deleteSubdomain(DNSProvider dnsProvider, Subdomain subdomain) {
        return dnsProvider.deleteSubdomain(subdomain).thenApply(success -> {
            if (success) {
                subdomain.delete();
                return subdomain.getSubdomain() + "." + dnsProvider.getDomainName() + " is succesvol verwijderd.";
            }
            return "Er is iets misgegaan tijdens het verwijderen van dit subdomein.";
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

    /**
     * Create a subdomain on the current DNS provider
     * @param subdomain the subdomain to create
     * @param ip the IP address to associate with the subdomain
     * @param port the port to associate with the subdomain
     * @return a list of the DNS record IDs created
     */
    protected abstract CompletableFuture<List<String>> createSubdomain(String subdomain, String ip, int port);

    /**
     * Delete the DNS records associated with the provided subdomain
     * @param subdomain the subdomain to delete
     * @return true if deletion was successful, false otherwise
     */
    protected abstract CompletableFuture<Boolean> deleteSubdomain(Subdomain subdomain);
}
