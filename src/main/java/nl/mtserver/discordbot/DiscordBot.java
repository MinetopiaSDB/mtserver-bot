package nl.mtserver.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import nl.mtserver.discordbot.commands.CreateSubdomainCMD;
import nl.mtserver.discordbot.commands.ListSubdomainCMD;
import nl.mtserver.discordbot.utils.commands.CommandFactory;
import org.simpleyaml.configuration.file.YamlFile;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class DiscordBot {

    private final JDA jda;

    public DiscordBot(YamlFile file) throws LoginException {
        JDABuilder builder = JDABuilder.createLight(file.getString("Discord.BotToken"));
        this.jda = builder.build();
    }

    public JDA getJDA() {
        return jda;
    }

    public void registerCommands(CommandFactory factory) {
        try {
            getJDA().awaitReady();
        } catch (InterruptedException ex) {
            throw new RuntimeException("InterruptedException received whilst waiting on JDA to be ready", ex);
        }

        // If there are multiple DNS providers, add the option to select a specific domain
        List<OptionData> createSubdomainCmdOptions = new ArrayList<>();
        createSubdomainCmdOptions.add(new OptionData(OptionType.STRING, "naam", "De naam van jouw subdomein (bijvoorbeeld 'mijnserver')", true));

        if (Main.getDNSProviders().size() > 1) {
            String domainName = Main.getDNSProviders().get(0).getDomainName();
            OptionData domainOption = new OptionData(OptionType.INTEGER, "domein",
                    "De domeinnaam waar jij jouw subdomein op aan wilt maken (bijvoorbeeld '" + domainName + "')", true);
            Main.getDNSProviders().forEach(provider -> domainOption.addChoice(provider.getDomainName(), provider.getDNSProviderId()));
            createSubdomainCmdOptions.add(domainOption);
        }

        // Add IP-address and port options after the subdomain and domain option
        createSubdomainCmdOptions.add(new OptionData(OptionType.STRING, "ip-adres",
                "Het IP-adres van jouw Minecraft server (bijvoorbeeld 12.34.56.78)", true));
        createSubdomainCmdOptions.add(new OptionData(OptionType.INTEGER, "poort",
                "De poort van jouw Minecraft server (bijvoorbeeld 25565)", true));

        factory.registerCommand("createsubdomain", "Maak een subdomein aan voor jouw Minecraft server.",
                new CreateSubdomainCMD(), createSubdomainCmdOptions.toArray(new OptionData[0]));

        factory.registerCommand("listsubdomains", "Bekijk alle subdomeinen die jij hebt.",
                new ListSubdomainCMD());
    }
}
