package nl.mtserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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

    public void registerCommands() {
        try {
            getJDA().awaitReady();
        } catch (InterruptedException ex) {
            throw new RuntimeException("InterruptedException received whilst waiting on JDA to be ready", ex);
        }

        getJDA().getGuilds().forEach(guild -> {
            CommandData createSubdomainCmd = new CommandData("createsubdomain",
                    "Maak een subdomein aan voor jouw Minecraft server.")
                    .addOption(OptionType.STRING, "naam", "De naam van jouw subdomein (bijvoorbeeld 'mijnserver')", true);

            // If there are multiple DNS providers, add the option to select a specific domain
            if (Main.getDNSProviders().size() > 1) {
                String domainName = Main.getDNSProviders().get(0).getDomainName();
                OptionData domainOption = new OptionData(OptionType.INTEGER, "domein",
                        "De domeinnaam waar jij jouw subdomein op aan wilt maken (bijvoorbeeld '" + domainName + "')", true);
                Main.getDNSProviders().forEach(provider -> {
                    domainOption.addChoice(provider.getDomainName(), provider.getDNSProviderId());
                });
                createSubdomainCmd.addOptions(domainOption);
            }
            // Add IP-address and port command after the subdomain and domain option
            createSubdomainCmd
                    .addOption(OptionType.STRING, "ip-adres", "Het IP-adres van jouw Minecraft server (bijvoorbeeld 12.34.56.78)", true)
                    .addOption(OptionType.INTEGER, "poort", "De poort van jouw Minecraft server (bijvoorbeeld 25565)", true);

            guild.upsertCommand(createSubdomainCmd).queue();
        });
    }
}
