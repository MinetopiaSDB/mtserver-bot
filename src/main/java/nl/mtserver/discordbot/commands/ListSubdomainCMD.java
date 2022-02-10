package nl.mtserver.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import nl.mtserver.discordbot.data.Subdomain;
import nl.mtserver.discordbot.utils.commands.BotCommand;
import nl.mtserver.discordbot.utils.commands.Command;

import java.util.List;
import java.util.stream.Collectors;

public class ListSubdomainCMD implements BotCommand {

    @Override
    public void execute(Command cmd, SlashCommandEvent event) {
        List<Subdomain> subdomains = Subdomain.find(event.getUser().getIdLong());
        if (subdomains == null || subdomains.isEmpty()) {
            event.reply("Je hebt geen subdomeinen!").setEphemeral(true).queue();
        }else{
            String subdomainsString = subdomains.stream()
                    .map(subdomain -> "- " + subdomain.getSubdomain() + "." + subdomain.getDomainName())
                    .collect(Collectors.joining("\n"));
            event.reply("Jij hebt de volgende subdomeinen:\n" + subdomainsString).setEphemeral(true).queue();
        }
    }
}
