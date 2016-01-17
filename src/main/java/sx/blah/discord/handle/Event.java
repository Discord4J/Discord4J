package sx.blah.discord.handle;

import sx.blah.discord.api.IDiscordClient;

/**
 * Used to represent an event.
 */
public abstract class Event {
	
	protected IDiscordClient client;
	
	/**
	 * Gets the client instance this event was fired from.
	 *
	 * @return The client instance.
	 */
	public IDiscordClient getClient() {
		return client;
	}
}
