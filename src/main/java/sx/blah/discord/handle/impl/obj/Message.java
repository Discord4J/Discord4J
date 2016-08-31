package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.requests.MessageRequest;
import sx.blah.discord.api.internal.json.responses.MessageResponse;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Message implements IMessage {

	/**
	 * The ID of the message. Used for message updating.
	 */
	protected final String id;

	/**
	 * The actual message (what you see
	 * on your screen, the content).
	 */
	protected volatile String content;

	/**
	 * The User who sent the message.
	 */
	protected final User author;

	/**
	 * The ID of the channel the message was sent in.
	 */
	protected final Channel channel;

	/**
	 * The time the message was received.
	 */
	protected volatile LocalDateTime timestamp;

	/**
	 * The time (if it exists) that the message was edited.
	 */
	protected volatile LocalDateTime editedTimestamp;

	/**
	 * The list of users mentioned by this message.
	 */
	protected volatile List<String> mentions;

	/**
	 * The list of roles mentioned by this message.
	 */
	protected volatile List<String> roleMentions;

	/**
	 * The attachments, if any, on the message.
	 */
	protected volatile List<Attachment> attachments;

	/**
	 * Whether the message mentions everyone.
	 */
	protected volatile boolean mentionsEveryone;

	/**
	 * Whether the message has been pinned to its channel or not.
	 */
	protected volatile boolean isPinned;

	/**
	 * The list of channels mentioned by this message.
	 */
	protected final List<IChannel> channelMentions;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	/**
	 * The pattern for matching channel mentions.
	 */
	private static final Pattern CHANNEL_PATTERN = Pattern.compile("<#([0-9]+)>");

	public Message(IDiscordClient client, String id, String content, IUser user, IChannel channel,
				   LocalDateTime timestamp, LocalDateTime editedTimestamp, boolean mentionsEveryone,
				   List<String> mentions, List<String> roleMentions, List<Attachment> attachments,
				   boolean pinned) {
		this.client = client;
		this.id = id;
		this.content = content;
		this.author = (User) user;
		this.channel = (Channel) channel;
		this.timestamp = timestamp;
		this.editedTimestamp = editedTimestamp;
		this.mentions = mentions;
		this.roleMentions = roleMentions;
		this.attachments = attachments;
		this.mentionsEveryone = mentionsEveryone;
		this.isPinned = pinned;
		this.channelMentions = new ArrayList<>();

		setChannelMentions();
	}

	@Override
	public String getContent() {
		return content;
	}

	/**
	 * Sets the CACHED content of the message.
	 *
	 * @param content The new message content.
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Sets the CACHED mentions in this message.
	 *
	 * @param mentions The new user mentions.
	 * @param roleMentions The new role mentions.
	 */
	public void setMentions(List<String> mentions, List<String> roleMentions) {
		this.mentions = mentions;
		this.roleMentions = roleMentions;
	}

	/**
	 * Populates the channel mention list.
	 */
	public void setChannelMentions() {
		if (content != null) {
			channelMentions.clear();
			Matcher matcher = CHANNEL_PATTERN.matcher(content);

			while (matcher.find()) {
				String mentionedID = matcher.group(1);
				IChannel mentioned = channel.getGuild().getChannelByID(mentionedID);

				if (mentioned != null) {
					channelMentions.add(mentioned);
				}
			}
		}
	}

	/**
	 * Sets the CACHED attachments in this message.
	 *
	 * @param attachments The new attachements.
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
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
	public String getID() {
		return id;
	}

	/**
	 * Sets the CACHED version of the message timestamp.
	 *
	 * @param timestamp The timestamp.
	 */
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public List<IUser> getMentions() {
		if (mentionsEveryone)
			return channel.getGuild().getUsers();
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
	public void reply(String content) throws MissingPermissionsException, RateLimitException, DiscordException {
		getChannel().sendMessage(String.format("%s, %s", this.getAuthor(), content));
	}

	@Override
	public IMessage edit(String content) throws MissingPermissionsException, RateLimitException, DiscordException {
		if (!this.getAuthor().equals(client.getOurUser()))
			throw new MissingPermissionsException("Cannot edit other users' messages!");
		if (client.isReady()) {
//			content = DiscordUtils.escapeString(content);

			MessageResponse response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages/"+id,
					new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0], false)), "UTF-8"),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);

			IMessage oldMessage = copy();
			DiscordUtils.getMessageFromJSON(client, channel, response);
			//Event dispatched here because otherwise there'll be an NPE as for some reason when the bot edits a message,
			// the event chain goes like this:
			//Original message edited to null, then the null message edited to the new content
			client.getDispatcher().dispatch(new MessageUpdateEvent(oldMessage, this));

		} else {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Bot has not signed in yet!");
		}
		return this;
	}

	/**
	 * Gets the raw list of mentioned user ids.
	 *
	 * @return Mentioned user list.
	 */
	public List<String> getRawMentions() {
		return mentions;
	}

	/**
	 * Gets the raw list of mentioned role ids.
	 *
	 * @return Mentioned role list.
	 */
	public List<String> getRawRoleMentions() {
		return roleMentions;
	}

	@Override
	public boolean mentionsEveryone() {
		return mentionsEveryone;
	}

	/**
	 * CACHES whether the message mentions everyone.
	 *
	 * @param mentionsEveryone True to mention everyone false if otherwise.
	 */
	public void setMentionsEveryone(boolean mentionsEveryone) {
		this.mentionsEveryone = mentionsEveryone;
	}

	@Override
	public void delete() throws MissingPermissionsException, RateLimitException, DiscordException {
		if (!getAuthor().equals(client.getOurUser())) {
			if (channel.isPrivate())
				throw new DiscordException("Cannot delete the other person's message in a private channel!");

			DiscordUtils.checkPermissions(client, getChannel(), EnumSet.of(Permissions.MANAGE_MESSAGES));
		}

		if (client.isReady()) {
			((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages/"+id,
					new BasicNameValuePair("authorization", client.getToken()));
		} else {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Bot has not signed in yet!");
		}
	}

	@Override
	public Optional<LocalDateTime> getEditedTimestamp() {
		return Optional.ofNullable(editedTimestamp);
	}

	/**
	 * This sets the CACHED edited timestamp.
	 *
	 * @param editedTimestamp The new timestamp.
	 */
	public void setEditedTimestamp(LocalDateTime editedTimestamp) {
		this.editedTimestamp = editedTimestamp;
	}

	@Override
	public boolean isPinned() {
		return isPinned;
	}

	/**
	 * This sets the CACHED isPinned value.
	 *
	 * @param pinned Whether the message is pinned.
	 */
	public void setPinned(boolean pinned) {
		isPinned = pinned;
	}

	@Override
	public IMessage copy() {
		return new Message(client, id, content, author, channel, timestamp, editedTimestamp,
				mentionsEveryone, mentions, roleMentions, attachments, isPinned);
	}

	@Override
	public IGuild getGuild() {
		return getChannel().getGuild();
	}

	@Override
	public IDiscordClient getClient() {
		return client;
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
		return other != null && this.getClass().isAssignableFrom(other.getClass()) && ((IMessage) other).getID().equals(getID());
	}
}
