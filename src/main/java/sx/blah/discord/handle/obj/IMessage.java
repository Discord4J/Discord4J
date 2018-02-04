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

package sx.blah.discord.handle.obj;

import com.vdurmont.emoji.Emoji;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageTokenizer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A Discord message in a text channel.
 */
public interface IMessage extends IDiscordObject<IMessage> {

	/**
	 * The maximum length of a Discord message.
	 */
	int MAX_MESSAGE_LENGTH = 2000;

	/**
	 * Gets the raw content of the message.
	 *
	 * <p>Use {@link #getFormattedContent()} to get the content with human-readable mentions.
	 *
	 * @return The raw content of the message.
	 */
	String getContent();

	/**
	 * Gets the channel the message was sent in.
	 *
	 * @return The channel the message was sent in.
	 */
	IChannel getChannel();

	/**
	 * Gets the author of the message.
	 *
	 * @return The author of the message.
	 */
	IUser getAuthor();

	/**
	 * Gets the timestamp of when the message was sent.
	 *
	 * @return The timestamp of when the message was sent.
	 */
	Instant getTimestamp();

	/**
	 * Gets the users mentioned in the message.
	 *
	 * @return The users mentioned in the message.
	 */
	List<IUser> getMentions();

	/**
	 * Gets the roles mentioned in the message.
	 *
	 * @return The roles mentioned in the message.
	 */
	List<IRole> getRoleMentions();

	/**
	 * Gets the channels mentioned in the message.
	 *
	 * @return The channels mentioned in the message.
	 */
	List<IChannel> getChannelMentions();

	/**
	 * Gets the attachments in the message.
	 *
	 * @return The attachments in the message.
	 */
	List<Attachment> getAttachments();

	/**
	 * Gets the embeds in the message.
	 *
	 * @return The embeds in the message.
	 */
	List<IEmbed> getEmbeds();

	/**
	 * Sends a message in the message's channel replying to the author of the message. This is indicated by prefixing
	 * the message with "@author, ".
	 *
	 * @param content The content of the message.
	 * @return The sent message object.
	 */
	IMessage reply(String content);

	/**
	 * Sends a message in the message's channel replying to the author of the message. This is indicated by prefixing
	 * the message with "@author, ".
	 *
	 * @param content The content of the message.
	 * @param embed The embed in the message.
	 * @return The sent message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage reply(String content, EmbedObject embed);

	/**
	 * Edits the message.
	 *
	 * <p>The bot may only edit <b>its own</b> messages.
	 *
	 * @param content The new content of the message.
	 * @return The new message object.
	 */
	IMessage edit(String content);

	/**
	 * Edits the message.
	 *
	 * <p>The bot may only edit <b>its own</b> messages.
	 *
	 * @param content The new content of the message.
	 * @param embed The new embed in the message.
	 * @return The new message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(String content, EmbedObject embed);

	/**
	 * Edits the message.
	 *
	 * <p>The bot may only edit <b>its own</b> messages.
	 *
	 * @param embed The new embed in the message.
	 * @return The new message object.
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(EmbedObject embed);

	/**
	 * Gets whether the message mentions everyone through @everyone.
	 *
	 * @return Whether the message mentions everyone.
	 */
	boolean mentionsEveryone();

	/**
	 * Gets whether the message mentions all online users through @here.
	 *
	 * @return Gets whether the message mentions all online users.
	 */
	boolean mentionsHere();

	/**
	 * Deletes the message.
	 */
	void delete();

	/**
	 * Gets the timestamp of when the message was last edited.
	 *
	 * @return The timestamp of when the message was last edited.
	 */
	Optional<Instant> getEditedTimestamp();

	/**
	 * Gets whether the message is pinned in its channel.
	 *
	 * @return Whether the message is pinned in its channel.
	 */
	boolean isPinned();

	/**
	 * Gets the guild the message is in.
	 *
	 * @return The guild the message is in.
	 */
	IGuild getGuild();

	/**
	 * Gets the message's content with human-readable mentions.
	 *
	 * @return The message's content with human-readable mentions.
	 */
	String getFormattedContent();

	/**
	 * Gets the reactions on the message.
	 *
	 * @return The reactions on the message.
	 */
	List<IReaction> getReactions();

	/**
	 * Gets a reaction by its custom guild emoji.
	 *
	 * @param emoji The emoji of the desired reaction.
	 * @return The reaction with the provided emoji (or null if one was not found).
	 */
	IReaction getReactionByEmoji(IEmoji emoji);

	/**
	 * Gets a reaction by the ID of its custom guild emoji.
	 *
	 * @param id The ID of the emoji of the desired reaction.
	 * @return The reaction with the provided ID (or null if one was not found).
	 */
	IReaction getReactionByID(long id);

	/**
	 * Gets a reaction by its unicode character emoji.
	 *
	 * @param unicode The unicode character of the desired reaction.
	 * @return The reaction with the provided emoji (or null if one was not found).
	 */
	IReaction getReactionByUnicode(Emoji unicode);

	/**
	 * Gets a reaction by its unicode character emoji.
	 *
	 * @param unicode The unicode character of the desired reaction.
	 * @return The reaction with the provided emoji (or null if one was not found).
	 */
	IReaction getReactionByUnicode(String unicode);

	/**
	 * Gets a reaction by its emoji.
	 *
	 * @param emoji The emoji of the desired reaction.
	 * @return The reaction with the provided emoji (or null if one was not found).
	 */
	IReaction getReactionByEmoji(ReactionEmoji emoji);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param reaction The reaction to get the emoji to react with from.
	 */
	void addReaction(IReaction reaction);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(IEmoji emoji);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(Emoji emoji);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(ReactionEmoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param reaction The reaction to remove.
	 */
	void removeReaction(IUser user, IReaction reaction);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, ReactionEmoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, IEmoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, Emoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, String emoji);

	/**
	 * Removes all of the reactions on the message.
	 */
	void removeAllReactions();

	/**
	 * Creates a message tokenizer for the message.
	 *
	 * @return A message tokenizer for the message.
	 */
	MessageTokenizer tokenize();

	/**
	 * Gets whether the message was deleted.
	 *
	 * @return Whether the message was deleted.
	 */
	boolean isDeleted();

	/**
	 * Gets the ID of the webhook that sent the message.
	 *
	 * @return The ID of the webhook that sent the message. This is <code>0</code> if the message was not sent by
	 * a webhook.
	 */
	long getWebhookLongID();

	/**
	 * Gets the message type.
	 *
	 * @return The message type.
	 */
	Type getType();

	/**
	 * Gets whether the message is a system message.
	 *
	 * @return Whether the message is a system message.
	 */
	boolean isSystemMessage();

	/**
	 * An attachment included in a message.
	 */
	class Attachment implements IIDLinkedObject {

		/**
		 * The file name of the attachment.
		 */
		protected final String filename;

		/**
		 * The size, in bytes, of the attachment.
		 */
		protected final int filesize;

		/**
		 * The ID of the attachment.
		 */
		protected final long id;

		/**
		 * The download link of the attachment.
		 */
		protected final String url;

		public Attachment(String filename, int filesize, long id, String url) {
			this.filename = filename;
			this.filesize = filesize;
			this.id = id;
			this.url = url;
		}

		/**
		 * Gets the file name of the attachment.
		 *
		 * @return The file name of the attachment.
		 */
		public String getFilename() {
			return filename;
		}

		/**
		 * Gets the size, in bytes, of the attachment.
		 *
		 * @return The size, in bytes, of the attachment.
		 */
		public int getFilesize() {
			return filesize;
		}

		@Override
		public long getLongID() {
			return id;
		}

		/**
		 * Gets the download link of the attachment.
		 *
		 * @return The download link of the attachment.
		 */
		public String getUrl() {
			return url;
		}
	}

	enum Type {
		DEFAULT(0),
		RECIPIENT_ADD(1),
		RECIPIENT_REMOVE(2),
		CALL(3),
		CHANNEL_NAME_CHANGE(4),
		CHANNEL_ICON_CHANGE(5),
		CHANEL_PINNED_MESSAGE(6),
		GUILD_MEMBER_JOIN(7),
		UNKNOWN(Integer.MIN_VALUE);

		private final int value;

		Type(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
