package nl.mtserver.discordbot;

import nl.mtserver.discordbot.commands.CreateSubdomainCMD;
import nl.mtserver.discordbot.commands.ListSubdomainCMD;
import nl.mtserver.discordbot.data.HikariSQL;
import nl.mtserver.discordbot.dns.CloudflareProvider;
import nl.mtserver.discordbot.dns.DNSProvider;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    private static DiscordBot bot;
    private static final List<DNSProvider> dnsProviders = new ArrayList<>();

    public static void main(String[] args) {
        YamlFile configFile = new YamlFile("config.yml");
        try {
            configFile.load();
        } catch (InvalidConfigurationException | IOException ex) {
            throw new RuntimeException("Config file not found or malformed!", ex);
        }

        HikariSQL.getInstance().setup(configFile.getString("Database.host"), configFile.getInt("Database.port"),
                configFile.getString("Database.user"), configFile.getString("Database.password"), configFile.getString("Database.database"));


        for (Object dnsProviderObj : configFile.getList("Domains")) {
            Map<String, String> dnsProvider = (Map<String, String>) dnsProviderObj;

            int id = DNSProvider.firstOrCreate(dnsProvider.get("Domain"));
            dnsProviders.add(new CloudflareProvider(id, dnsProvider.get("ZoneId"), dnsProvider.get("Domain"),
                    dnsProvider.get("AuthEmail"), dnsProvider.get("AuthKey")));
        }

        try {
            bot = new DiscordBot(configFile);
            bot.registerCommands();
            bot.getJDA().addEventListener(new CreateSubdomainCMD());
            bot.getJDA().addEventListener(new ListSubdomainCMD());
        } catch (LoginException ex) {
            throw new RuntimeException("Failed to login to Discord!", ex);
        }
    }

    public static List<DNSProvider> getDNSProviders() {
        return dnsProviders;
    }
}
