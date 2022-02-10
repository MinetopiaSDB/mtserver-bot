package nl.mtserver.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import nl.mtserver.discordbot.Main;
import nl.mtserver.discordbot.data.Subdomain;
import nl.mtserver.discordbot.dns.DNSProvider;
import nl.mtserver.discordbot.utils.commands.BotCommand;
import nl.mtserver.discordbot.utils.commands.Command;

import java.util.Arrays;

public class CreateSubdomainCMD implements BotCommand {

    @Override
    public void execute(Command cmd, SlashCommandEvent event) {
        if (!Subdomain.find(event.getUser().getIdLong()).isEmpty()) {
            event.reply("Je kan maximaal 1 subdomein hebben! Je kunt je huidige subdomeinen zien met **/listsubdomains**.")
                    .setEphemeral(true).queue();
            return;
        }

        String subdomainName = event.getOption("subdomein").getAsString();
        String ipAddress = event.getOption("ip-adres").getAsString();
        if (Arrays.asList("www", "mail", "ftp", "beheer", "dashboard", "dash", "play", "mc").contains(subdomainName.toLowerCase())) {
            event.reply("Je hebt een ongeldig subdomein opgegeven!").setEphemeral(true).queue();
            return;
        }

        if (event.getOption("poort").getAsLong() > 65535 || event.getOption("poort").getAsLong() < 1) {
            event.reply("Je hebt een ongeldige poort opgegeven!").setEphemeral(true).queue();
            return;
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
            event.reply(subdomain.getFQDN() + " bestaat al!").setEphemeral(true).queue();
            return;
        }

        DNSProvider finalProvider = provider;
        event.deferReply(true).queue(message -> {
            DNSProvider.createSubdomain(finalProvider, event.getUser().getIdLong(), subdomainName, ipAddress, port)
                    .thenAccept(statusMessage -> message.editOriginal(statusMessage).queue());
        });
    }
}
