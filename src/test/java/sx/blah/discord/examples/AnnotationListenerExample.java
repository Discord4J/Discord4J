/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.examples;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
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
		public void logout(DisconnectedEvent event) { // This is called when dDisconnectedEvent is dispatched
			System.out.println("Logged out for reason " + event.getReason() + "!");
		}
	}
}
