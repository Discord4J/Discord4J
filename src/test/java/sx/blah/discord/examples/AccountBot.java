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
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;

/**
 * An example demonstrating a few of the methods to change the bot's information.
 */
public class AccountBot extends BaseBot implements IListener<ReadyEvent> {

	public AccountBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this bot as an event listener
	}

	/**
	 * Fired when the bot is ready to interact with Discord.
	 * @see ReadyBot
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			this.client.changeUsername("Awesome Bot"); // Changes the bot's username
			this.client.changeAvatar(Image.forFile(new File("picture.png"))); // Changes the bot's profile picture
			this.client.changePresence(StatusType.IDLE, ActivityType.PLAYING, "playing text");
		} catch (RateLimitException | DiscordException e) { // An error occurred
			e.printStackTrace();
		}
	}
}
