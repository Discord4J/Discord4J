package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.VoiceChannelRequest;
import sx.blah.discord.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VoiceChannel extends Channel implements IVoiceChannel {

	protected int userLimit = 0;

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, int userLimit) {
		this(client, name, id, parent, topic, position, new ArrayList<>(), new HashMap<>(), new HashMap<>());
		this.userLimit = userLimit;
	}

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, List<IMessage> pinned, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, parent, topic, position, pinned, roleOverrides, userOverrides);
	}

	@Override
	public int getUserLimit() {
		return userLimit;
	}

	/**
	 * Sets the CACHED user limit.
	 *
	 * @param limit The new user limit.
	 */
	public void setUserLimit(int limit) {
		this.userLimit = limit;
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
					((User)client.getOurUser()).channels.add(this);
				} else if (!client.isBot() && client.getConnectedVoiceChannels().size() > 0)
					throw new UnsupportedOperationException("Must be a bot account to have multi-server voice support!");

				((DiscordClientImpl) client).ws.send(DiscordUtils.GSON.toJson(new VoiceChannelRequest(parent.getID(), id, false, false)));
				if (!client.getOurUser().getConnectedVoiceChannels().contains(this))
					client.getOurUser().getConnectedVoiceChannels().add(this);
			} else {
				Discord4J.LOGGER.info(LogMarkers.HANDLE, "Already connected to the voice channel!");
			}
		} else {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Bot has not signed in yet!");
		}
	}

	@Override
	public void leave() {
		if (client.getConnectedVoiceChannels().contains(this)) {
			((DiscordClientImpl) client).ws.send(DiscordUtils.GSON.toJson(new VoiceChannelRequest(parent.getID(), null, false, false)));
			if (((DiscordClientImpl) client).voiceConnections.containsKey(parent))
				((DiscordClientImpl) client).voiceConnections.get(parent).disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
		} else {
			Discord4J.LOGGER.warn(LogMarkers.HANDLE, "Attempted to leave and not joined voice channel! Ignoring the method call...");
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
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTopic() {
		throw new UnsupportedOperationException();
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
	public IMessage sendFile(File file) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void toggleTypingStatus() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean getTypingStatus() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void changeTopic(String topic) throws RateLimitException, DiscordException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IMessage> getPinnedMessages() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVoiceChannel copy() {
		return new VoiceChannel(client, name, id, parent, topic, position, userLimit);
	}

	@Override
	public List<IUser> getUsersHere() {
		return getConnectedUsers();
	}

	@Override
	public List<IUser> getConnectedUsers() {
		return parent.getUsers().stream().filter((user) -> user.getConnectedVoiceChannels().contains(this)).collect(Collectors.toList());
	}
}
