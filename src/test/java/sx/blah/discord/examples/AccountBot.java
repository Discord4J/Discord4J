package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;

/**
 * An example demonstrating a few of the methods to change the bot's information.
 */
public class AccountBot extends BaseBot implements IListener<ReadyEvent> {

	public AccountBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this bot as an event listener
	}

	/**
	 * Fired when the bot is ready to interact with Discord.
	 * @see ReadyBot
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			this.client.changeUsername("Awesome Bot"); // Changes the bot's username
			this.client.changeAvatar(Image.forFile(new File("picture.png"))); // Changes the bot's profile picture
			this.client.changePresence(true); // Changes the bot's presence to idle
			this.client.changeStatus(Status.game("Awesome Game")); // Changes the bot's status
		} catch (RateLimitException | DiscordException e) { // An error occurred
			e.printStackTrace();
		}
	}
}
