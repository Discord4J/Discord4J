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

import java.awt.*;
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
