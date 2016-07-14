package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;

/**
 * An example demonstrating the use of the EventSubscriber annotation.
 */
public class AnnotationListenerExample extends BaseBot {

	public AnnotationListenerExample(IDiscordClient client) {
		super(client);
		EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
		dispatcher.registerListener(new EventSubscriberExample()); // Registers the event listener
	}

	/**
	 * Methods in this class with the @EventSubscriber annotation on them will be called when an event in their params
	 * is dispatched.
	 */
	public static class EventSubscriberExample {

		@EventSubscriber
		public void onReady(ReadyEvent event) { // This is called when the ReadyEvent is dispatched
			IDiscordClient client = event.getClient(); // Gets the client from the event object
			IUser ourUser = client.getOurUser();// Gets the user represented by the client
			String name = ourUser.getName();// Gets the name of our user
			System.out.println("Logged in as " + name);
		}

		@EventSubscriber
		public void logout(DiscordDisconnectedEvent event) { // This is called when DiscordDisconnectedEvent is dispatched
			System.out.println("Logged out for reason " + event.getReason() + "!");
		}
	}
}
