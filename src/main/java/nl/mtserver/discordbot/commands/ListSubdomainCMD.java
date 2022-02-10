package nl.mtserver.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.mtserver.discordbot.data.Subdomain;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ListSubdomainCMD extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!event.getName().equalsIgnoreCase("listsubdomains")) {
            return;
        }
        List<Subdomain> subdomains = Subdomain.find(event.getUser().getIdLong());
        if (subdomains == null || subdomains.isEmpty()) {
            event.reply("Je hebt geen subdomeinen!").setEphemeral(true).queue();
        }else{
            String subdomainsString = subdomains.stream().map(subdomain -> "- " + subdomain.getSubdomain() + "." + subdomain.getDomainName()).collect(Collectors.joining("\n"));
            event.reply("Jij hebt de volgende subdomeinen:\n" + subdomainsString).setEphemeral(true).queue();
        }
    }
}
