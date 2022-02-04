package nl.mtserver.discordbot;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Main {

    private static DiscordBot bot;

    public static void main(String[] args) {
        YamlFile configFile = new YamlFile("config.yml");
        try {
            configFile.load();
        } catch (InvalidConfigurationException | IOException ex) {
            throw new RuntimeException("Config file not found or malformed!", ex);
        }

        // Setup database


        try {
            bot = new DiscordBot(configFile);
        } catch (LoginException ex) {
            throw new RuntimeException("Failed to login to Discord!", ex);
        }

    }
}
