package nl.mtserver.discordbot.listeners;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.mtserver.discordbot.Main;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		Main.getCommandFactory().execute(event.getName(), event);
	}
}