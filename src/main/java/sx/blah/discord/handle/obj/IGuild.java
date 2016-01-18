package sx.blah.discord.handle.obj;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.HTTP403Exception;

import java.util.List;
import java.util.Optional;

/**
 * This class defines a guild/server/clan/whatever it's called.
 */
public interface IGuild {
	
	/**
	 * Gets the user id for the owner of this guild.
	 *
	 * @return The owner id.
	 */
	String getOwnerID();
	
	/**
	 * Gets the user object for the owner of this guild.
	 *
	 * @return The owner.
	 */
	IUser getOwner();
	
	/**
	 * Gets the icon id for this guild.
	 *
	 * @return The icon id.
	 */
	String getIcon();
	
	/**
	 * Gets the direct link to the guild's icon.
	 *
	 * @return The icon url.
	 */
	String getIconURL();
	
	/**
	 * Gets all the channels on the server.
	 *
	 * @return All channels on the server.
	 */
	List<IChannel> getChannels();
	
	/**
	 * Gets a channel on the guild by a specific channel id.
	 *
	 * @param id The ID of the channel you want to find.
	 * @return The channel with given ID.
	 */
	IChannel getChannelByID(String id);
	
	/**
	 * Gets all the users connected to the guild.
	 *
	 * @return All users connected to the guild.
	 */
	List<IUser> getUsers();
	
	/**
	 * Gets a user by its id in the guild.
	 *
	 * @param id ID of the user you want to find.
	 * @return The user with given ID.
	 */
	IUser getUserByID(String id);
	
	/**
	 * Gets the name of the guild.
	 *
	 * @return The name of the guild
	 */
	String getName();
	
	/**
	 * Gets the id of the guild.
	 *
	 * @return The ID of this guild.
	 */
	String getID();
	
	/**
	 * Gets the roles contained in this guild.
	 *
	 * @return The list of roles in the guild.
	 */
	List<IRole> getRoles();
	
	/**
	 * Gets a role object for its unique id.
	 *
	 * @param id The role id of the desired role.
	 * @return The role, or null if not found.
	 */
	IRole getRoleForID(String id);
	
	/**
	 * Gets the voice channels in this guild.
	 * 
	 * @return The voice channels.
	 */
	List<IVoiceChannel> getVoiceChannels();
	
	/**
	 * Gets a voice channel for a give id.
	 * 
	 * @param id The channel id.
	 * @return The voice channel (or null if not found).
	 */
	IVoiceChannel getVoiceChannelForID(String id);
	
	/**
	 * Gets the channel where afk users are placed.
	 * 
	 * @return The voice channel (or null if nonexistant).
	 */
	IVoiceChannel getAFKChannel();
	
	/**
	 * Gets the timeout (in seconds) before a user is placed in the AFK channel.
	 * 
	 * @return The timeout.
	 */
	int getAFKTimeout();
	
	/**
	 * Creates a new role in this guild.
	 * 
	 * @return The new role.
	 * 
	 * @throws HTTP403Exception
	 */
	IRole createRole() throws HTTP403Exception;
	
	/**
	 * Retrieves the list of banned users from this guild.
	 * 
	 * @return The list of banned users.
	 * 
	 * @throws HTTP403Exception
	 */
	List<IUser> getBannedUsers() throws HTTP403Exception;
	
	/**
	 * Bans a user from this guild.
	 * 
	 * @param userID The user to ban.
	 * 
	 * @throws HTTP403Exception
	 */
	void banUser(String userID) throws HTTP403Exception;
	
	/**
	 * Bans a user from this guild.
	 *
	 * @param userID The user to ban.
	 * @param deleteMessagesForDays The number of days to delete messages from this user for.
	 *
	 * @throws HTTP403Exception
	 */
	void banUser(String userID, int deleteMessagesForDays) throws HTTP403Exception;
	
	/**
	 * This removes a ban on a user.
	 * 
	 * @param userID The user to unban.
	 * 
	 * @throws HTTP403Exception
	 */
	void pardonUser(String userID) throws HTTP403Exception;
	
	/**
	 * Kicks a user from the guild.
	 * 
	 * @param userID The user to kick.
	 * 
	 * @throws HTTP403Exception
	 */
	void kickUser(String userID) throws HTTP403Exception;
	
	/**
	 * Edits the roles a user is a part of.
	 * 
	 * @param userID The user to edit the roles for.
	 * @param roleIDs The roles for the user to have.
	 * 
	 * @throws HTTP403Exception
	 */
	void editUserRoles(String userID, String[] roleIDs) throws HTTP403Exception;
	
	/**
	 * Edits the guild.
	 * 
	 * @param name The name of the guild.
	 * @param regionID The region id for the guild.
	 * @param icon The icon for the guild.
	 * @param afkChannelID The afk channel for the guild. NOTE: if not present there will be no afk channel.
	 * @param afkTimeout The afk timeout for the guild.
	 * 
	 * @throws HTTP403Exception
	 */
	void edit(Optional<String> name, Optional<String> regionID, Optional<IDiscordClient.Image> icon, Optional<String> afkChannelID, Optional<Integer> afkTimeout) throws HTTP403Exception;
	
	/**
	 * Deletes the channel if you are its owner or leaves it if not.
	 * 
	 * @throws HTTP403Exception
	 */
	void deleteOrLeaveGuild() throws HTTP403Exception;
	
	/**
	 * Creates a new channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 */
	IChannel createChannel(String name) throws DiscordException, HTTP403Exception;
	
	/**
	 * Creates a new voice channel.
	 *
	 * @param name The name of the new channel. MUST be between 2-100 characters long.
	 * @return The new channel.
	 *
	 * @throws DiscordException
	 */
	IVoiceChannel createVoiceChannel(String name) throws DiscordException, HTTP403Exception;
	
	/**
	 * Gets the region this guild is located in.
	 * 
	 * @return The region.
	 */
	IRegion getRegion();
}
