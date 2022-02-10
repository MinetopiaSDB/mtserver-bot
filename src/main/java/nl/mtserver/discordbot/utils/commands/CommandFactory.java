package nl.mtserver.discordbot.utils.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import nl.mtserver.discordbot.DiscordBot;

import java.util.HashMap;

public class CommandFactory {

	private final HashMap<Command, BotCommand> commands = new HashMap<>();
	private final DiscordBot bot;

	public CommandFactory(DiscordBot bot) {
		this.bot = bot;
	}

	public Command registerCommand(String commandName, String description, BotCommand executor, OptionData... options) {
		if (commands.keySet().stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(commandName))) {
			return null;
		}
		bot.getJDA().getGuilds().forEach(guild -> guild.upsertCommand(new CommandData(commandName, description).addOptions(options)).queue());
		Command command = new Command(commandName);
		commands.put(command, executor);
		return command;
	}

	public void execute(String command, SlashCommandEvent event) {
		commands.keySet().stream()
				.filter(cmd -> cmd.getName().equals(command))
				.forEach(cmd -> commands.get(cmd).execute(cmd, event));
	}
}