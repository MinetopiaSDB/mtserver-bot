package nl.mtserver.discordbot.dns;

import nl.mtserver.discordbot.data.HikariSQL;
import nl.mtserver.discordbot.data.Subdomain;

import java.sql.*;

public record DNSRecord(int id, String recordId, int subdomainId) {

    public static DNSRecord create(String recordId, Subdomain subdomain) {
        try (Connection connection = HikariSQL.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO dns_records (record_id, subdomain_id) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, recordId);
            statement.setInt(2, subdomain.getId());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return new DNSRecord(resultSet.getInt(1), recordId, subdomain.getId());
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to store DNS record in database", exception);
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getRecordId() {
        return recordId;
    }
}
