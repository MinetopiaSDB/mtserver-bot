package nl.mtserver.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.mtserver.discordbot.Main;
import nl.mtserver.discordbot.data.Subdomain;
import nl.mtserver.discordbot.dns.DNSProvider;
import org.jetbrains.annotations.NotNull;

public class CreateSubdomainCMD extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!event.getName().equalsIgnoreCase("createsubdomain")) {
            return;
        }
        String subdomainName = event.getOption("naam").getAsString();
        String ipAddress = event.getOption("ip-adres").getAsString();
        if (event.getOption("poort").getAsLong() > 65535 || event.getOption("poort").getAsLong() < 1) {
            event.reply("Je hebt een ongeldige poort opgegeven!").setEphemeral(true).queue();
        }
        int port = (int) event.getOption("poort").getAsLong();

        DNSProvider provider = Main.getDNSProviders().get(0);
        if (Main.getDNSProviders().size() > 1) {
            provider = Main.getDNSProviders().stream()
                    .filter(dnsProvider -> dnsProvider.getDNSProviderId() == event.getOption("domein").getAsLong())
                    .findFirst().orElse(null);

            // This should never happen, but just in case
            if (provider == null) {
                event.reply("De opgegeven domeinnaam is niet gevonden!").setEphemeral(true).queue();
                return;
            }
        }

        Subdomain subdomain = Subdomain.find(subdomainName, provider);
        if (subdomainName.length() > 63 || !subdomainName.matches("[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]")) {
            event.reply("De opgegeven subdomein is ongeldig!").setEphemeral(true).queue();
            return;
        }
        if (subdomain != null) {
            event.reply(subdomainName + " " + provider.getDomainName() + " bestaat al!").setEphemeral(true).queue();
            return;
        }

        DNSProvider finalProvider = provider;
        event.deferReply(true).queue(message -> {
            DNSProvider.createSubdomain(finalProvider, event.getUser().getIdLong(), subdomainName, ipAddress, port)
                    .thenAccept(statusMessage -> {
                        message.editOriginal(statusMessage).queue();
                    });
        });
    }
}
