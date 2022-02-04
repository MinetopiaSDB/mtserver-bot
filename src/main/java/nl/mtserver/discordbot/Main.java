package nl.mtserver.discordbot;

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
    private static List<DNSProvider> dnsProviders = new ArrayList<>();

    public static void main(String[] args) {
        YamlFile configFile = new YamlFile("config.yml");
        try {
            configFile.load();
        } catch (InvalidConfigurationException | IOException ex) {
            throw new RuntimeException("Config file not found or malformed!", ex);
        }

        // Setup database


        for (Object dnsProviderObj : configFile.getList("Domains")) {
            Map<String, String> dnsProvider = (Map<String, String>) dnsProviderObj;

            dnsProviders.add(new CloudflareProvider(dnsProvider.get("ZoneId"), dnsProvider.get("Domain"),
                    dnsProvider.get("AuthEmail"), dnsProvider.get("AuthKey")));
        }


        try {
            bot = new DiscordBot(configFile);
        } catch (LoginException ex) {
            throw new RuntimeException("Failed to login to Discord!", ex);
        }

    }
}
