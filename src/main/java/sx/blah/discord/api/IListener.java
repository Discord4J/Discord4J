package sx.blah.discord.api;

/**
 * Used to represent a class that handles only one event.
 * <b>WARNING: Due to an issue in TypeTools, it is not recommended to use this class through lambda expressions.</b>
 *
 * @param <T> The event type to handle.
 */
@FunctionalInterface
public interface IListener <T extends Event> {

	/**
	 * Called when the event is sent.
	 *
	 * @param event The event object.
	 */
	void handle(T event);
}
