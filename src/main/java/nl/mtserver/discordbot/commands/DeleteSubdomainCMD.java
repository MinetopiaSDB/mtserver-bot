package nl.mtserver.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import nl.mtserver.discordbot.Main;
import nl.mtserver.discordbot.data.Subdomain;
import nl.mtserver.discordbot.dns.DNSProvider;
import nl.mtserver.discordbot.utils.commands.BotCommand;
import nl.mtserver.discordbot.utils.commands.Command;

import java.util.List;

public class DeleteSubdomainCMD implements BotCommand {

    @Override
    public void execute(Command cmd, SlashCommandEvent event) {
        int providerId = (event.getOption("domein") == null)
                ? Main.getDNSProviders().get(0).getDNSProviderId()
                : (int) event.getOption("domein").getAsLong();

        List<Subdomain> subdomains = Subdomain.find(event.getUser().getIdLong());
        Subdomain subdomain = subdomains.stream()
                .filter(randomSubdomain -> randomSubdomain.getSubdomain().equalsIgnoreCase(event.getOption("subdomein").getAsString())
                        && randomSubdomain.getDNSProviderId() == providerId).findFirst().orElse(null);

        if (subdomains == null || subdomain == null) {
            event.getChannel().sendMessage("Je hebt geen toestemming om dit subdomein te verwijderen! " +
                    "Om een lijst van jouw subdomeinen te krijgen kan je **/listsubdomains** gebruiken.").queue();
            return;
        }


        event.deferReply(true).queue(message -> {
            DNSProvider.deleteSubdomain(subdomain.getDNSProvider(), subdomain)
                    .thenAccept(statusMessage -> {
                        message.editOriginal(statusMessage).queue();
                    });
        });
    }
}
