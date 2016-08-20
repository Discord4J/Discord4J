package sx.blah.discord.api.events;

/**
 * Used to represent a class that handles only one event.
 * <b>WARNING: Due to an issue in TypeTools, using this class through a lambda expression *may* slow your bot down.</b>
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
