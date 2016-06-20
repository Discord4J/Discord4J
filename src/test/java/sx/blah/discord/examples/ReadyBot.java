package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 * An example demonstrating the need to wait for the ReadyEvent before attempting to do things in Discord.
 */
public class ReadyBot extends BaseBot implements IListener<ReadyEvent> {

	public ReadyBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this bot as an event listener

		try {
			// This will NOT work. The bot is not ready to interact with Discord because the ReadyEvent has yet to be fired.
			discordClient.changeUsername("Loser Bot");
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The ReadyEvent is fired when the bot is ready to interact with Discord. Attempting to do so (e.g. Changing account info, sending messages, etc.)
	 * before the event is fired will result in an error.
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			// This WILL work. The ReadyEvent has been fired and the bot is ready to interact with Discord.
			event.getClient().changeUsername("Awesome Bot");
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
	}
}
