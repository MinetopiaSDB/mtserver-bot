package nl.mtserver.discordbot.utils.commands;

public record Command(String name) {

	public String getName() {
		return name;
	}

}