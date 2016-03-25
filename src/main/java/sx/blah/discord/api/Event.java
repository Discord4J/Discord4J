package sx.blah.discord.api;

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
