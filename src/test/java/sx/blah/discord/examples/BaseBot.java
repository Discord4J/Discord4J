package sx.blah.discord.examples;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;

/**
 * This represents a SUPER basic bot (literally all it does is login).
 * This is used as a base for all example bots.
 */
public class BaseBot {

	public static BaseBot INSTANCE; //Singleton instance of the bot.
	public IDiscordClient client; //The instance of the discord client.

	public static void main(String[] args) { //Main method
		if (args.length < 2) //Needs an email and password provided
			throw new IllegalArgumentException("This bot needs at least 2 arguments!");

		INSTANCE = login(args[0], args[1]); //Creates the bot instance and logs it in.
	}

	public BaseBot(IDiscordClient client) {
		this.client = client; //Sets the client instance to the one provided
	}

	public static BaseBot login(String email, String password) {
		BaseBot bot = null; //Initializing the bot variable

		ClientBuilder builder = new ClientBuilder(); //Creates a new client builder instance
		builder.withLogin(email, password); //Sets the email and password for the client
		try {
			IDiscordClient client = builder.login(); //Builds the IDiscordClient instance and logs it in
			bot = new BaseBot(client); //Creating the bot instance
		} catch (DiscordException e) { //Error occurred logging in
			System.err.println("Error occurred while logging in!");
			e.printStackTrace();
		}

		return bot;
	}
}
