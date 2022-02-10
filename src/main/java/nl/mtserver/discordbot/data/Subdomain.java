package nl.mtserver.discordbot.data;

import nl.mtserver.discordbot.Main;
import nl.mtserver.discordbot.dns.DNSProvider;
import nl.mtserver.discordbot.dns.DNSRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record Subdomain(int id, String subdomain, long userId, int dnsProviderId) {

    public static Subdomain findOrCreate(String subdomain, long userId, DNSProvider provider) {
        try (Connection connection = HikariSQL.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO `subdomains` (subdomain, user_id, dns_provider_id)" +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subdomain);
            statement.setLong(2, userId);
            statement.setInt(3, provider.getDNSProviderId());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return new Subdomain(resultSet.getInt(1), subdomain.toLowerCase(), userId, provider.getDNSProviderId());
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static Subdomain find(String subdomain, DNSProvider provider) {
        try (Connection connection = HikariSQL.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `subdomains` WHERE subdomain=? AND dns_provider_id=?")) {
            statement.setString(1, subdomain);
            statement.setInt(2, provider.getDNSProviderId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Subdomain(resultSet.getInt("id"), resultSet.getString("subdomain").toLowerCase(),
                            resultSet.getLong("user_id"), resultSet.getInt("dns_provider_id"));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static List<Subdomain> find(long userId) {
        List<Subdomain> subdomains = new ArrayList<>();
        try (Connection connection = HikariSQL.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `subdomains` WHERE `user_id`=?")) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next())
                    subdomains.add(new Subdomain(resultSet.getInt("id"), resultSet.getString("subdomain").toLowerCase(),
                            resultSet.getLong("user_id"), resultSet.getInt("dns_provider_id")));
            }
            return subdomains;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getDomainName() {
        return Main.getDNSProviders().stream().filter(dnsProvider -> dnsProvider.getDNSProviderId() == dnsProviderId)
                .map(DNSProvider::getDomainName)
                .findFirst().orElse("");
    }

    /**
     * Get the fully qualified domain name for this subdomain
     * @return The fully qualified domain name (e.g. wouter.mtserver.nl)
     */
    public String getFQDN() {
        return getSubdomain() + "." + getDomainName();
    }

    public int getDNSProviderId() {
        return dnsProviderId;
    }

    public DNSProvider getDNSProvider() {
        return Main.getDNSProviders().stream()
                .filter(dnsProvider -> dnsProvider.getDNSProviderId() == dnsProviderId)
                .findFirst().orElse(null);
    }

    public CompletableFuture<List<DNSRecord>> getDNSRecords() {
        return CompletableFuture.supplyAsync(() -> {
            List<DNSRecord> records = new ArrayList<>();
            try (Connection connection = HikariSQL.getInstance().getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM `dns_records` WHERE subdomain_id=?")) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        records.add(new DNSRecord(resultSet.getInt("id"), resultSet.getString("record_id"),
                                resultSet.getInt("subdomain_id")));
                    }
                }

                return records;
            } catch (SQLException exception) {
                throw new RuntimeException("Failed to get DNS records for subdomain " + subdomain, exception);
            }
        });
    }

    public CompletableFuture<Boolean> delete() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = HikariSQL.getInstance().getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM `subdomains` WHERE id=?")) {
                statement.setInt(1, getId());
                return statement.executeUpdate() == 1;
            } catch (SQLException exception) {
                throw new RuntimeException("Failed to delete subdomain " + subdomain, exception);
            }
        });
    }
}
