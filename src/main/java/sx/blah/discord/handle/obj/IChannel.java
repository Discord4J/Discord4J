package sx.blah.discord.handle.obj;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.util.HTTP403Exception;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines a text channel in a guild/server.
 */
public interface IChannel {
	
	/**
	 * Gets the name of this channel.
	 *
	 * @return The channel name.
	 */
	String getName();
	
	/**
	 * Gets the id of this channel.
	 *
	 * @return The channel id.
	 */
	String getID();
	
	/**
	 * Gets the messages in this channel.
	 *
	 * @return The list of messages in the channel.
	 */
	List<IMessage> getMessages();
	
	/**
	 * Gets a specific message by its id.
	 *
	 * @param messageID The message id.
	 * @return The message (if found).
	 */
	IMessage getMessageByID(String messageID);
	
	/**
	 * Gets the guild this channel is a part of.
	 *
	 * @return The guild.
	 *
	 * @deprecated Use {@link #getGuild()} instead.
	 */
	@Deprecated
	IGuild getParent();
	
	/**
	 * Gets the guild this channel is a part of.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();
	
	/**
	 * Gets whether or not this channel is a private one–if it is a private one, this object is an instance of {@link PrivateChannel}.
	 *
	 * @return True if the channel is private, false if otherwise.
	 */
	boolean isPrivate();
	
	/**
	 * Gets the topic for the channel.
	 *
	 * @return The channel topic (null if not set).
	 */
	String getTopic();
	
	/**
	 * Formats a string to be able to #mention this channel.
	 *
	 * @return The formatted string.
	 */
	String mention();
	
	/**
	 * Sends a message without tts to the desired channel.
	 *
	 * @param content The content of the message.
	 * @return The message object representing the sent message
	 */
	IMessage sendMessage(String content);
	
	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use tts or not.
	 * @return The message object representing the sent message
	 */
	IMessage sendMessage(String content, boolean tts);
	
	/**
	 * Sends a file to the channel.
	 *
	 * @param file The file to send.
	 * @return The message sent.
	 *
	 * @throws HTTP403Exception
	 * @throws IOException
	 */
	IMessage sendFile(File file) throws HTTP403Exception, IOException;
	
	/**
	 * Generates an invite for this channel.
	 *
	 * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
	 * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
	 * @param temporary Whether users admitted with this invite are temporary.
	 * @param useXkcdPass Whether to generate a human-readable code, maxAge cannot be 0 for this to work.
	 * @return The newly generated invite.
	 */
	IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass);
	
	/**
	 * Toggles whether the bot is "typing".
	 */
	void toggleTypingStatus();
	
	/**
	 * Gets whether the bot is "typing".
	 *
	 * @return True if the bot is typing, false if otherwise.
	 */
	boolean getTypingStatus();
	
	/**
	 * Gets the last read message id.
	 *
	 * @return The message id.
	 */
	String getLastReadMessageID();
	
	/**
	 * Gets the last read message.
	 *
	 * @return The message.
	 */
	IMessage getLastReadMessage();
	
	/**
	 * Edits the channel.
	 *
	 * @param name The new name of the channel.
	 * @param position The new position of the channel.
	 * @param topic The new topic of the channel.
	 * @throws DiscordException
	 * @throws HTTP403Exception
	 */
	void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, HTTP403Exception;
	
	/**
	 * Gets the position of the channel on the channel list.
	 *
	 * @return The position.
	 */
	int getPosition();
	
	/**
	 * Deletes this channel.
	 *
	 * @throws HTTP403Exception
	 */
	void delete() throws HTTP403Exception;
	
	/**
	 * Gets the permissions overrides for users. (Key = User id).
	 *
	 * @return The user permissions overrides for this channel.
	 */
	Map<String, PermissionOverride> getUserOverrides();
	
	/**
	 * Gets the permissions overrides for users. (Key = User id).
	 *
	 * @return The user permissions overrides for this channel.
	 */
	Map<String, PermissionOverride> getRoleOverrides();
	
	/**
	 * Gets the permissions available for a user with all permission overrides taken into account.
	 * 
	 * @param user The user to get the permissions for.
	 * @return The set of permissions.
	 */
	EnumSet<Permissions> getModifiedPermissions(IUser user);
	
	/**
	 * Gets the permissions available for a role with all permission overrides taken into account.
	 *
	 * @param role The role to get the permissions for.
	 * @return The set of permissions.
	 */
	EnumSet<Permissions> getModifiedPermissions(IRole role);
	
	/**
	 * Removes a permissions override on this channel.
	 * 
	 * @param id The id of the override to remove, this is either a user id or role id.
	 * 
	 * @throws HTTP403Exception
	 */
	void removePermissionsOverride(String id) throws HTTP403Exception;
	
	/**
	 * Creates/edits permission overrides for this channel.
	 * 
	 * @param roleID The role id of the role to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 * 
	 * @throws HTTP403Exception
	 */
	void overrideRolePermissions(String roleID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws HTTP403Exception;
	
	/**
	 * Creates/edits permission overrides for this channel.
	 *
	 * @param userID The user id of the user to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 *
	 * @throws HTTP403Exception
	 */
	void overrideUserPermissions(String userID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws HTTP403Exception;
	
	/**
	 * Represents specific permission overrides for a user/role in the channel.
	 */
	class PermissionOverride {
		
		/**
		 * Permissions to add.
		 */
		protected final EnumSet<Permissions> allow;
		
		/**
		 * Permissions to remove.
		 */
		protected final EnumSet<Permissions> deny;
		
		public PermissionOverride(EnumSet<Permissions> allow, EnumSet<Permissions> deny) {
			this.allow = allow;
			this.deny = deny;
		}
		
		/**
		 * Gets the permissions to add to the user/role.
		 *
		 * @return The permissions.
		 */
		public EnumSet<Permissions> allow() {
			return allow;
		}
		
		/**
		 * Gets the permissions to remove from the user/role.
		 *
		 * @return The permissions.
		 */
		public EnumSet<Permissions> deny() {
			return deny;
		}
	}
}
