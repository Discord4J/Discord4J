package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.*;
import sx.blah.discord.api.internal.json.objects.ChannelObject;
import sx.blah.discord.api.internal.json.requests.ChannelEditRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceStateUpdateRequest;
import sx.blah.discord.handle.impl.events.ChannelUpdateEvent;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class VoiceChannel extends Channel implements IVoiceChannel {

	protected volatile int userLimit = 0;
	protected volatile int bitrate = 0;

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, int userLimit, int bitrate, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, parent, topic, position, roleOverrides, userOverrides);
		this.userLimit = userLimit;
		this.bitrate = bitrate;
	}

	@Override
	public int getUserLimit() {
		return userLimit;
	}

	@Override
	public int getBitrate() { return bitrate; }

	/**
	 * Sets the CACHED user limit.
	 *
	 * @param limit The new user limit.
	 */
	public void setUserLimit(int limit) {
		this.userLimit = limit;
	}

	/**
	 * Sets the CACHED bitrate.
	 *
	 * @param bitrate The new bitrate.
	 */
	public void setBitrate(int bitrate) { this.bitrate = bitrate; }

	@Override
	public void changeName(String name) throws MissingPermissionsException, DiscordException, RateLimitException {
		edit(Optional.of(name), Optional.empty(), Optional.empty(), Optional.empty());
	}

	@Override
	public void changePosition(int position) throws MissingPermissionsException, DiscordException, RateLimitException {
		edit(Optional.empty(), Optional.of(position), Optional.empty(), Optional.empty());
	}

	@Override
	public void changeUserLimit(int limit) throws MissingPermissionsException, DiscordException, RateLimitException {
		edit(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(limit));
	}

	@Override
	public void changeBitrate(int bitrate) throws MissingPermissionsException, DiscordException, RateLimitException {
		edit(Optional.empty(), Optional.empty(), Optional.of(bitrate), Optional.empty());
	}

	private void edit(Optional<String> name, Optional<Integer> position, Optional<Integer> bitrate, Optional<Integer> userLimit) throws MissingPermissionsException, DiscordException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		String newName = name.orElse(this.name);
		int newPosition = position.orElse(this.position);
		int newBitrate = bitrate.orElse(this.bitrate);
		int newUserLimit = userLimit.orElse(this.userLimit);

		if (newName == null || newName.length() < 2 || newName.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		if (newBitrate < 8000 || newBitrate > 128000)
			throw new DiscordException("Channel bitrate can only be between 8 and 128 kbps!");
		if (newUserLimit < 0 || newUserLimit > 99)
			throw new DiscordException("Channel user limit can only be between 0 and 99!");

		try {
			ChannelObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.CHANNELS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelEditRequest(newName, newPosition, newBitrate, newUserLimit)))),
					ChannelObject.class);

			IChannel oldChannel = copy();
			IChannel newChannel = DiscordUtils.getChannelFromJSON(getGuild(), response);

			client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, newChannel));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void join() throws MissingPermissionsException {
		if (client.isReady()) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.VOICE_CONNECT));
			if (!client.getOurUser().getConnectedVoiceChannels().contains(this)) {
				if (((DiscordClientImpl) client).voiceConnections.containsKey(parent)) {
					Discord4J.LOGGER.info(LogMarkers.HANDLE, "Attempting to join multiple channels in the same guild! Moving channels instead...");
					try {
						client.getOurUser().moveToVoiceChannel(this);
					} catch (DiscordException | RateLimitException | MissingPermissionsException e) {
						Discord4J.LOGGER.error(LogMarkers.HANDLE, "Unable to switch voice channels! Aborting join request...", e);
						return;
					}
				}

				((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE, new VoiceStateUpdateRequest(parent.getID(), id, false, false));
			} else {
				Discord4J.LOGGER.info(LogMarkers.HANDLE, "Already connected to the voice channel!");
			}
		} else {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Attempt to join voice channel before bot is ready!");
		}
	}

	@Override
	public void leave() {
		if (client.getConnectedVoiceChannels().contains(this)) {
			((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE, new VoiceStateUpdateRequest(parent.getID(), null, false, false));
			if (((DiscordClientImpl) client).voiceConnections.containsKey(parent)) {
				((DiscordClientImpl) client).voiceConnections.get(parent).disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
			}
		} else {
			Discord4J.LOGGER.warn(LogMarkers.HANDLE, "Attempted to leave a non-joined voice channel! Ignoring the method call...");
		}
	}

	@Override
	public boolean isConnected() {
		return client.getConnectedVoiceChannels().contains(this);
	}

	@Override
	public MessageList getMessages() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage getMessageByID(String messageID) {
		return null;
	}

	@Override
	public String getTopic() {
		return "";
	}

	@Override
	public void setTopic(String topic) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendMessage(String content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(File file) throws FileNotFoundException, RateLimitException, DiscordException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(String content, File file) throws FileNotFoundException, DiscordException, RateLimitException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) throws DiscordException, RateLimitException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void toggleTypingStatus() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean getTypingStatus() {
		return false;
	}

	@Override
	public void changeTopic(String topic) throws RateLimitException, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IMessage> getPinnedMessages() {
		return new ArrayList<>();
	}

	@Override
	public List<IWebhook> getWebhooks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook getWebhookByID(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name) throws MissingPermissionsException, DiscordException, RateLimitException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) throws MissingPermissionsException, DiscordException, RateLimitException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) throws MissingPermissionsException, DiscordException, RateLimitException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadWebhooks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVoiceChannel copy() {
		return new VoiceChannel(client, name, id, parent, topic, position, userLimit, bitrate, roleOverrides, userOverrides);
	}

	@Override
	public List<IUser> getUsersHere() {
		return getConnectedUsers();
	}

	@Override
	public List<IUser> getConnectedUsers() {
		return parent.getUsers().stream().filter((user) -> user.getConnectedVoiceChannels().contains(this)).collect(Collectors.toList());
	}

	@Override
	public String toString(){
		return getName();
	}

	@Override
	public boolean isDeleted() {
		return getGuild().getVoiceChannelByID(getID()) != this;
	}
}
