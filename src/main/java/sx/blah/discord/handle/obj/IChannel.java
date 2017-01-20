package sx.blah.discord.handle.obj;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Defines a text channel in a guild/server.
 */
public interface IChannel extends IDiscordObject<IChannel> {

	/**
	 * Gets the name of this channel.
	 *
	 * @return The channel name.
	 */
	String getName();

	/**
	 * Gets the messages in this channel.
	 *
	 * @return The list of messages in the channel.
	 */
	MessageList getMessages();

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
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage sendMessage(String content) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param embed The embed object
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param tts Whether the message should use tts or not.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage sendMessage(String content, boolean tts) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed object
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Sends a message to the desired channel.
	 *
	 * @param content The content of the message.
	 * @param embed The embed object
	 * @param tts Whether the message should use tts or not.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendMessage(String content, EmbedObject embed, boolean tts) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads a file to the channel.
	 *
	 * @param file The file to upload.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads a file to the channel with a message attached.
	 *
	 * @param content The content of the attached message.
	 * @param file The file to upload.
	 * @return The message created by this action.
	 *
	 * @throws FileNotFoundException
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads a file to the channel with an attached message and option for tts.
	 *
	 * @param file The input stream to upload.
	 * @param embed An optional embed object to send with the file.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed An optional embed object to send with the file.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendFile(EmbedObject embed, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param content The content of the attached message.
	 * @param tts Whether the message should use tts or not.
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @param embed An optional embed object to send with the file.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 *
	 * @see EmbedBuilder
	 */
	IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Uploads an InputStream to the channel with an attached message and option for tts.
	 *
	 * @param builder The MessageBuilder to use
	 * @param file The input stream to upload.
	 * @param fileName The name of the file that should be shown in Discord.
	 * @return The message created by this action.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	IMessage sendFile(MessageBuilder builder, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Generates an invite for this channel.
	 *
	 * @param maxAge How long the invite should be valid, setting it to 0 makes it last forever.
	 * @param maxUses The maximum uses for the invite, setting it to 0 makes the invite have unlimited uses.
	 * @param temporary Whether users admitted with this invite are temporary.
	 * @return The newly generated invite.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Toggles whether the bot is "typing".
	 */
	void toggleTypingStatus();

	/**
	 * Sets whether the bot is "typing".
	 *
	 * @param typing True if the bot is typing.
	 */
	void setTypingStatus(boolean typing);

	/**
	 * Gets whether the bot is "typing".
	 *
	 * @return True if the bot is typing, false if otherwise.
	 */
	boolean getTypingStatus();

	/**
	 * Edits all properties of this channel.
	 *
	 * @param name The new name of the channel.
	 * @param position The new position of the channel.
	 * @param topic The new topic of the channel.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void edit(String name, int position, String topic) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Changes the name of the channel
	 *
	 * @param name The new name of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeName(String name) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Changes the position of the channel
	 *
	 * @param position The new position of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changePosition(int position) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Changes the topic of the channel
	 *
	 * @param topic The new topic of the channel.
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void changeTopic(String topic) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Gets the position of the channel on the channel list.
	 *
	 * @return The position.
	 */
	int getPosition();

	/**
	 * Deletes this channel.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete() throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Gets the permissions overrides for users. (Key = User id).
	 *
	 * @return The user permissions overrides for this channel.
	 */
	Map<String, PermissionOverride> getUserOverrides();

	/**
	 * Gets the permissions overrides for roles. (Key = Role id).
	 *
	 * @return The role permissions overrides for this channel.
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
	 * @param user The user whose override should be removed.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removePermissionsOverride(IUser user) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Removes a permissions override on this channel.
	 *
	 * @param role The role whose override should be removed.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removePermissionsOverride(IRole role) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Creates/edits permission overrides for this channel.
	 *
	 * @param role The role to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Creates/edits permission overrides for this channel.
	 *
	 * @param user The user to create/edit the permission overrides for.
	 * @param toAdd The permissions to add.
	 * @param toRemove The permissions to remove.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This gets all the currently available invites for this channel.
	 *
	 * @return The list of all available invites.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	List<IInvite> getInvites() throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This gets the users with the ability to read this channel.
	 *
	 * @return The users in this channel.
	 */
	List<IUser> getUsersHere();

	/**
	 * Gets the pinned messages in this channel.
	 *
	 * @return The pinned messages.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	List<IMessage> getPinnedMessages() throws DiscordException, RateLimitException;

	/**
	 * This pins the provided message to this channel.
	 *
	 * @param message The message to pin.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void pin(IMessage message) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This unpins the provided message from this channel.
	 *
	 * @param message The message to unpin.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	void unpin(IMessage message) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Gets the webhooks for this channel.
	 *
	 * @return The webhooks.
	 */
	List<IWebhook> getWebhooks();

	/**
	 * This gets a webhook by its id.
	 *
	 * @param id The webhook id.
	 * @return The webhook or null if not found.
	 */
	IWebhook getWebhookByID(String id);

	/**
	 * This finds all the webhooks which have the same name as the provided one.
	 *
	 * @param name The name to search for.
	 * @return The webhooks with a matching name.
	 */
	List<IWebhook> getWebhooksByName(String name);

	/**
	 * This creates a webhook for this channel with the provided name and the default avatar
	 *
	 * @param name The default name for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This creates a webhook for this channel with the provided name and the provided avatar
	 *
	 * @param name   The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, Image avatar) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * This creates a webhook for this channel with the provided name and the provided avatar
	 *
	 * @param name   The default name for the webhook.
	 * @param avatar The default avatar for the webhook.
	 * @return The created webhook.
	 */
	IWebhook createWebhook(String name, String avatar) throws DiscordException, RateLimitException, MissingPermissionsException;

	/**
	 * Checks to see if the this channel is deleted.
	 *
	 * @return True if this channel is deleted.
	 */
	boolean isDeleted();

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
