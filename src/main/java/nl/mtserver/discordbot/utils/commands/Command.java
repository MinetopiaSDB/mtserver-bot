package nl.mtserver.discordbot.utils.commands;

public class Command {

	private final String name;

	public Command(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}