package nl.mtserver.discordbot.data;

import nl.mtserver.discordbot.dns.DNSProvider;

import java.sql.*;
import java.util.Locale;

public class Subdomain {

    private int id, dnsProviderId;
    private String subdomain;
    private long userId;

    private Subdomain(int id, String subdomain, long userId, int dnsProviderId) {
        this.id = id;
        this.subdomain = subdomain;
        this.userId = userId;
        this.dnsProviderId = dnsProviderId;
    }

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
        }catch (SQLException exception) {
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
        }catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
