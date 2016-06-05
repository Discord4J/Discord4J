package sx.blah.discord.examples;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.RateLimitException;

import java.awt.*;
import java.util.EnumSet;

public class RoleBot extends BaseBot implements IListener<ReadyEvent> {

	public RoleBot(IDiscordClient discordClient) {
		super(discordClient);
		discordClient.getDispatcher().registerListener(this); // Registers this class as an event listener
	}

	/**
	 * See {@link ReadyBot}
	 *
	 * @param event The event object.
	 */
	@Override
	public void handle(ReadyEvent event) {
		try {
			// Gets the first guild the bot is a member of. NOTE: Getting guilds in this way is NOT recommended. Use IDs or events.
			IGuild guild = event.getClient().getGuilds().get(0);
			// Creates a new role in the guild
			IRole role = guild.createRole();
			role.changeName("Awesome People"); // Changes the role's name
			role.changeColor(Color.RED); // Changes the role's color
			role.changeHoist(true); // Makes the role appear separately from other roles in Discord

			EnumSet<Permissions> permissions = EnumSet.of(Permissions.READ_MESSAGE_HISTORY); // The list of permissions in this role.
			role.changePermissions(permissions); // Changes the permissions of the role to our set of permissions.

			IUser ourUser = event.getClient().getOurUser(); // Gets the user of the bot
			guild.editUserRoles(ourUser, new IRole[]{role}); // Assigns our new role to the bot. NOTE: This will make the bot's ONLY role our role.
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) { // Error occurred
			e.printStackTrace();
		}
	}
}
