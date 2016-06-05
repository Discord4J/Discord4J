package sx.blah.discord.examples;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.Image;

import java.io.File;

public class AccountBot extends BaseBot implements IListener<ReadyEvent> {

	public AccountBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this class as an event listener
	}

	/**
	 * Fired when the bot is ready to interact with Discord. See {@link ReadyBot}
	 *
	 * @param event The event object.
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			this.client.changeUsername("Awesome Bot"); // Changes the bot's username
			this.client.changeEmail("newEmail@domain.com"); // Changes the bot's account email
			this.client.changePassword("12345"); // Changes the bot's account password
			this.client.changeAvatar(Image.forFile(new File("picture.png"))); // Changes the bots' profile picture
		} catch (RateLimitException | DiscordException e) { // An error occurred
			e.printStackTrace();
		}
	}
}
