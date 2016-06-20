package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;

/**
 * An example demonstrating the use of the IListener interface.
 */
public class InterfaceListenerExample extends BaseBot {

	public InterfaceListenerExample(IDiscordClient client) {
		super(client);
		EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
		dispatcher.registerListener(new IListenerExample()); // Registers the event listener
	}

	/**
	 * This class will have its handle() method called when a ReadyEvent is dispatched by Discord4J.
	 * @see ReadyBot
	 */
	public static class IListenerExample implements IListener<ReadyEvent> {

		@Override
		public void handle(ReadyEvent event) {
			IDiscordClient client = event.getClient(); // Gets the client from the event object
			IUser ourUser = client.getOurUser();// Gets the user represented by the client
			String name = ourUser.getName();// Gets the name of our user
			System.out.println("Logged in as " + name);
		}
	}
}
