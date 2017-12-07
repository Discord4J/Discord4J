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
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RoleBuilder;

import java.awt.Color;
import java.util.EnumSet;

/**
 * An example demonstrating the use of the RoleBuilder utility class to create and assign roles.
 */
public class RoleBot extends BaseBot implements IListener<ReadyEvent> {

	public RoleBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this bot as an event listener
	}

	/**
	 * Client is ready to interact with Discord.
	 * @see ReadyBot
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			// Gets the first guild the bot is a member of. (NOTE: This is only for demonstration. Getting guilds in this way is NOT recommended. Use IDs or events instead.)
			IGuild guild = event.getClient().getGuilds().get(0);

			RoleBuilder roleBuilder = new RoleBuilder(guild); // Instantiate a RoleBuilder which will aide in the creation of the role.
			roleBuilder.withName("Awesome Role"); // Set the new role's name
			roleBuilder.withColor(Color.GREEN); // Set the new role's color
			roleBuilder.setHoist(true); // Make the new role display separately from others in Discord.
			roleBuilder.setMentionable(true); // Allow this role to be mentionable in chat.
			roleBuilder.withPermissions(EnumSet.of(Permissions.ADMINISTRATOR)); // Assign the Administrator permission to this role.
			IRole role = roleBuilder.build(); // Add the role to the guild in Discord.

			IUser ourUser = event.getClient().getOurUser(); // Gets the user of the bot
			guild.editUserRoles(ourUser, new IRole[] {role}); // Assigns our new role to the bot. NOTE: This will make the bot's ONLY role our role.
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) { // Error occurred
			e.printStackTrace();
		}
	}
}
