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
