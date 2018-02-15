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

package sx.blah.discord.handle.impl.obj;

import com.vdurmont.emoji.Emoji;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.api.internal.json.requests.MessageRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Message implements IMessage {

	/**
	 * The unique snowflake ID of the object.
	 */
	protected final long id;

	/**
	 * The raw content of the message.
	 */
	protected volatile String content;

	/**
	 * The author of the message.
	 */
	protected final User author;

	/**
	 * The channel the message was sent in.
	 */
	protected final Channel channel;

	/**
	 * The timestamp of when the message was sent.
	 */
	protected volatile Instant timestamp;

	/**
	 * The timestamp of when the message was last edited.
	 */
	protected volatile Instant editedTimestamp;

	/**
	 * The users mentioned in the message.
	 */
	protected volatile List<Long> mentions;

	/**
	 * The roles mentioned in the message.
	 */
	protected volatile List<Long> roleMentions;

	/**
	 * The attachments in the message.
	 */
	protected volatile List<Attachment> attachments;

	/**
	 * The embeds in the message.
	 */
	protected volatile List<Embed> embeds;

	/**
	 * Whether the message mentions everyone.
	 */
	protected volatile boolean mentionsEveryone;

	/**
	 * Gets whether the message mentions all online users.
	 */
	protected volatile boolean mentionsHere;

	/**
	 * Whether an @everyone mention in the message would be valid. (If the author of the message has permission to
	 * mention everyone)
	 */
	protected volatile boolean everyoneMentionIsValid;

	/**
	 * Whether the message is pinned in its channel.
	 */
	protected volatile boolean isPinned;

	/**
	 * The channels mentioned in the message.
	 */
	protected final List<IChannel> channelMentions;

	/**
	 * The client the message belongs to.
	 */
	protected final IDiscordClient client;

	/**
	 * The message's content with human-readable mentions. This is lazily evaluated.
	 */
	protected volatile String formattedContent = null;

	/**
	 * The reactions on the message.
	 */
	protected volatile List<IReaction> reactions;

	/**
	 * The ID of the webhook that sent the message. This is <code>0</code> if the message was not sent by a webhook.
	 */
	protected final long webhookID;

	/**
	 * The type of the message.
	 */
	protected final Type type;

	/**
	 * Pattern for Discord's channel mentions.
	 */
	private static final Pattern CHANNEL_PATTERN = Pattern.compile("<#([0-9]{1,19})>");

	/**
	 * Whether the message was deleted.
	 */
	private volatile boolean deleted = false;

	public Message(IDiscordClient client, long id, String content, IUser user, IChannel channel,
				   Instant timestamp, Instant editedTimestamp, boolean mentionsEveryone,
				   List<Long> mentions, List<Long> roleMentions, List<Attachment> attachments, boolean pinned,
				   List<Embed> embeds, List<IReaction> reactions, long webhookID, Type type) {
		this.client = client;
		this.id = id;
		setContent(content);
		this.author = (User) user;
		this.channel = (Channel) channel;
		this.timestamp = timestamp;
		this.editedTimestamp = editedTimestamp;
		this.mentions = mentions;
		this.roleMentions = roleMentions;
		this.attachments = attachments;
		this.isPinned = pinned;
		this.channelMentions = new ArrayList<>();
		this.embeds = embeds;
		this.everyoneMentionIsValid = mentionsEveryone;
		this.reactions = reactions;
		this.webhookID = webhookID;
		this.type = type;

		setChannelMentions();
	}

	public Message(IDiscordClient client, long id, String content, IUser user, IChannel channel,
				   Instant timestamp, Instant editedTimestamp, boolean mentionsEveryone,
				   List<Long> mentions, List<Long> roleMentions, List<Attachment> attachments, boolean pinned,
				   List<Embed> embeds, long webhookID, Type type) {
		this(client, id, content, user, channel, timestamp, editedTimestamp, mentionsEveryone, mentions, roleMentions,
				attachments, pinned, embeds, new CopyOnWriteArrayList<>(), webhookID, type);
	}

	@Override
	public String getContent() {
		return content;
	}

	/**
	 * Sets the CACHED content of the message.
	 *
	 * @param content The content of the message.
	 */
	public void setContent(String content) {
		this.content = content;
		this.formattedContent = null; // Force re-update later

		if (content != null) {
			this.mentionsEveryone = content.contains("@everyone");
			this.mentionsHere = content.contains("@here");
		}
	}

	/**
	 * Sets the CACHED mentions of the message.
	 *
	 * @param mentions The user mentions of the message.
	 * @param roleMentions The role mentions of the message.
	 */
	public void setMentions(List<Long> mentions, List<Long> roleMentions) {
		this.mentions = mentions;
		this.roleMentions = roleMentions;
	}

	/**
	 * Populates the channel mentions list.
	 */
	public void setChannelMentions() {
		if (content != null) {
			channelMentions.clear();
			Matcher matcher = CHANNEL_PATTERN.matcher(content);

			while (matcher.find()) {
				long mentionedID = Long.parseUnsignedLong(matcher.group(1));
				IChannel mentioned = client.getChannelByID(mentionedID);

				if (mentioned != null) {
					channelMentions.add(mentioned);
				}
			}
		}
	}

	/**
	 * Sets the CACHED attachments of the message.
	 *
	 * @param attachments The attachments of the message.
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * Sets the CACHED embeds of the message.
	 *
	 * @param embeds The embeds of the message.
	 */
	public void setEmbeds(List<Embed> embeds) {
		this.embeds = embeds;
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public IUser getAuthor() {
		return author;
	}

	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Sets the CACHED timestamp of the message.
	 *
	 * @param timestamp The timestamp of the message.
	 */
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	@Override
	public List<IUser> getMentions() {
		if (mentionsEveryone) {
			return channel.isPrivate() ? channel.getUsersHere() : channel.getGuild().getUsers();
		}

		return mentions.stream()
				.map(client::getUserByID)
				.collect(Collectors.toList());
	}

	@Override
	public List<IRole> getRoleMentions() {
		return roleMentions.stream()
				.map(m -> getGuild().getRoleByID(m))
				.collect(Collectors.toList());
	}

	@Override
	public List<IChannel> getChannelMentions() {
		return channelMentions;
	}

	@Override
	public List<Attachment> getAttachments() {
		return attachments;
	}

	@Override
	public List<IEmbed> getEmbeds() {
		List<IEmbed> copy = new ArrayList<>(embeds.size());
		copy.addAll(embeds);
		return copy;
	}

	@Override
	public IMessage reply(String content) {
		return reply(content, null);
	}

	@Override
	public IMessage reply(String content, EmbedObject embed) {
		return getChannel().sendMessage(String.format("%s, %s", this.getAuthor(), content), embed, false);
	}

	@Override
	public IMessage edit(String content) {
		return edit(content, null);
	}

	@Override
	public IMessage edit(EmbedObject embed) {
		return edit(null, embed);
	}

	@Override
	public IMessage edit(String content, EmbedObject embed) {
		getShard().checkReady("edit message");
		if (!this.getAuthor().equals(client.getOurUser()))
			throw new MissingPermissionsException("Cannot edit other users' messages!", EnumSet.noneOf(Permissions.class));
		if (isDeleted())
			throw new DiscordException("Cannot edit deleted messages!");

		if (embed != null) {
			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.EMBED_LINKS);
		}

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/" + id,
				new MessageRequest(content, embed, false));

		return this;
	}

	/**
	 * Gets a list of the unique snowflake IDs of the users mentioned in the message.
	 *
	 * @return A list of the unique snowflake IDs of the users mentioned in the message.
	 */
	public List<Long> getRawMentionsLong() {
		return mentions;
	}

	/**
	 * Gets a list of the unique snowflake IDs of the roles mentioned in the message.
	 *
	 * @return A list of the unique snowflake IDs of the roles mentioned in the message.
	 */
	public List<Long> getRawRoleMentionsLong() {
		return roleMentions;
	}

	@Override
	public boolean mentionsEveryone() {
		return everyoneMentionIsValid && mentionsEveryone;
	}

	@Override
	public boolean mentionsHere() {
		return everyoneMentionIsValid && mentionsHere;
	}

	/**
	 * Sets the CACHED mentions everyone value.
	 *
	 * @param mentionsEveryone The mentions everyone value.
	 */
	public void setMentionsEveryone(boolean mentionsEveryone) {
		this.mentionsEveryone = mentionsEveryone;
	}

	@Override
	public void delete() {
		getShard().checkReady("delete message");
		if (!getAuthor().equals(client.getOurUser())) {
			if (channel.isPrivate())
				throw new DiscordException("Cannot delete the other person's message in a private channel!");

			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);
		}

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/" + id);
	}

	@Override
	public Optional<Instant> getEditedTimestamp() {
		return Optional.ofNullable(editedTimestamp);
	}

	/**
	 * Sets the CACHED edited timestamp.
	 *
	 * @param editedTimestamp The edited timestamp.
	 */
	public void setEditedTimestamp(Instant editedTimestamp) {
		this.editedTimestamp = editedTimestamp;
	}

	@Override
	public boolean isPinned() {
		return isPinned;
	}

	/**
	 * Sets the CACHED pinned value.
	 *
	 * @param pinned The pinned value.
	 */
	public void setPinned(boolean pinned) {
		isPinned = pinned;
	}

	@Override
	public IMessage copy() {
		return new Message(client, id, content, author, channel, timestamp, editedTimestamp, everyoneMentionIsValid,
				mentions, roleMentions, attachments, isPinned, embeds, reactions, webhookID, type);
	}

	@Override
	public IGuild getGuild() {
		return getChannel().isPrivate() ? null : getChannel().getGuild();
	}

	@Override
	public String getFormattedContent() {
		if (content == null)
			return null;

		if (formattedContent == null) {
			String currentContent = content;

			for (IUser u : getMentions())
				currentContent = currentContent.replace(u.mention(false), "@" + u.getName())
						.replace(u.mention(true), "@" + u.getDisplayName(getGuild()));

			for (IChannel ch : getChannelMentions())
				currentContent = currentContent.replace(ch.mention(), "#" + ch.getName());

			for (IRole r : getRoleMentions())
				currentContent = currentContent.replace(r.mention(), "@" + r.getName());

			formattedContent = currentContent;
		}

		return formattedContent;
	}

	/**
	 * Sets the CACHED reactions on the message.
	 *
	 * @param reactions The reactions on the message.
	 */
	public void setReactions(List<IReaction> reactions) {
		this.reactions = reactions;
	}

	@Override
	public List<IReaction> getReactions() {
		return reactions;
	}

	@Override
	public IReaction getReactionByEmoji(IEmoji emoji) {
		return getReactionByID(emoji.getLongID());
	}

	@Override
	public IReaction getReactionByEmoji(ReactionEmoji emoji) {
		return getReactions().stream()
				.filter(r -> r.getEmoji().equals(emoji))
				.findFirst().orElse(null);
	}

	@Override
	public IReaction getReactionByID(long id) {
		return getReactions().stream()
				.filter(r -> r.getEmoji().getLongID() == id)
				.findFirst().orElse(null);
	}

	@Override
	public IReaction getReactionByUnicode(Emoji unicode) {
		return getReactionByUnicode(unicode.getUnicode());
	}

	@Override
	public IReaction getReactionByUnicode(String unicode) {
		return getReactions().stream()
				.filter(r -> r.getEmoji().isUnicode() && r.getEmoji().getName().equals(unicode))
				.findFirst().orElse(null);
	}

	@Override
	public void addReaction(IReaction reaction) {
		addReaction(reaction.getEmoji());
	}

	@Override
	public void addReaction(IEmoji emoji) {
		addReaction(ReactionEmoji.of(emoji));
	}

	@Override
	public void addReaction(Emoji emoji) {
		addReaction(ReactionEmoji.of(emoji.getUnicode()));
	}

	@Override
	public void addReaction(ReactionEmoji reactionEmoji) {
		Reaction reaction = (Reaction) getReactionByEmoji(reactionEmoji);
		if (reaction == null) { // only need perms when adding a new emoji
			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.ADD_REACTIONS);
		}

		String emoji = reactionEmoji.isUnicode()
				? reactionEmoji.getName()
				: reactionEmoji.getName() + ":" + reactionEmoji.getStringID();

		try {
			((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
					String.format(DiscordEndpoints.REACTIONS_USER, getChannel().getStringID(), getStringID(),
							URLEncoder.encode(emoji, "UTF-8"), "@me"));

			if (reaction == null) {
				reactions.add(new Reaction(this, 1, reactionEmoji));
			} else {
				reaction.setCount(reaction.getCount()+1);
			}
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void removeReaction(IUser user, IReaction reaction) {
		removeReaction(user, reaction.getEmoji());
	}

	@Override
	public void removeReaction(IUser user, IEmoji emoji) {
		removeReaction(user, ReactionEmoji.of(emoji));
	}

	@Override
	public void removeReaction(IUser user, Emoji emoji) {
		removeReaction(user, emoji.getUnicode());
	}

	@Override
	public void removeReaction(IUser user, ReactionEmoji reactionEmoji) {
		String emoji = reactionEmoji.isUnicode()
				? reactionEmoji.getName()
				: reactionEmoji.getName() + ":" + reactionEmoji.getStringID();
		removeReaction(user, emoji, (Reaction) getReactionByEmoji(reactionEmoji));
	}

	@Override
	public void removeReaction(IUser user, String emoji) {
		Reaction reaction = (Reaction) getReactions().stream().filter(it -> it.getUserReacted(user)
				&& it.getEmoji().toString().contains(emoji)).findFirst().orElse(null);
		removeReaction(user, emoji, reaction);
	}

	private void removeReaction(IUser user, String emoji, Reaction reaction) {
		if (!user.equals(client.getOurUser())) { // no perms for deleting our own reaction
			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);
		}

		try {
			((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(
					String.format(DiscordEndpoints.REACTIONS_USER, getChannel().getStringID(), getStringID(),
							URLEncoder.encode(emoji, "UTF-8"), user.getStringID()));

			if (reaction != null) {
				reaction.setCount(reaction.getCount()-1);
				if (reaction.getCount() <= 0)
					reactions.remove(reaction);
			}
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void removeAllReactions() {
		PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(
				String.format(DiscordEndpoints.REACTIONS, getChannel().getStringID(), getStringID()));

		reactions.clear();
	}

	@Override
	public long getWebhookLongID(){
		return webhookID;
	}

	@Override
	public MessageTokenizer tokenize() {
		return new MessageTokenizer(this);
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean isSystemMessage() {
		return !getType().equals(Type.DEFAULT);
	}

	/**
	 * Sets the CACHED deleted value.
	 *
	 * @param deleted The deleted value.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		return getChannel().getShard();
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		return DiscordUtils.equals(this, other);
	}
}
