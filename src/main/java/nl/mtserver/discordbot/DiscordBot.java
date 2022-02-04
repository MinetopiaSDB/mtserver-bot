package nl.mtserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.simpleyaml.configuration.file.YamlFile;

import javax.security.auth.login.LoginException;

public class DiscordBot {

    private final JDA jda;

    public DiscordBot(YamlFile file) throws LoginException {
        JDABuilder builder = JDABuilder.createLight(file.getString("Discord.BotToken"));
        this.jda = builder.build();
    }

    public JDA getJDA() {
        return jda;
    }
}
