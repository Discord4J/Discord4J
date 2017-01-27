package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.*;
import sx.blah.discord.api.internal.json.requests.VoiceChannelEditRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceStateUpdateRequest;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class VoiceChannel extends Channel implements IVoiceChannel {

	protected volatile int userLimit = 0;
	protected volatile int bitrate = 0;

	public VoiceChannel(IDiscordClient client, String name, String id, IGuild guild, String topic, int position, int userLimit, int bitrate, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		super(client, name, id, guild, topic, position, roleOverrides, userOverrides);
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
	public void edit(String name, int position, int bitrate, int userLimit) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new IllegalArgumentException("Channel name must be between 2 and 100 characters!");
		if (bitrate < 8000 || bitrate > 128000)
			throw new IllegalArgumentException("Channel bitrate must be between 8 and 128 kbps!");
		if (userLimit < 0 || userLimit > 99)
			throw new IllegalArgumentException("Channel user limit must be between 0 and 99!");

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.CHANNELS + id,
				new VoiceChannelEditRequest(name, position, bitrate, userLimit));
	}

	@Override
	public void changeName(String name) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(name, getPosition(), getBitrate(), getUserLimit());
	}

	@Override
	public void changePosition(int position) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(getName(), position, getBitrate(), getUserLimit());
	}

	@Override
	public void changeBitrate(int bitrate) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(getName(), getPosition(), bitrate, getUserLimit());
	}

	@Override
	public void changeUserLimit(int limit) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(getName(), getPosition(), getBitrate(), limit);
	}

	@Override
	public void join() throws DiscordException, RateLimitException, MissingPermissionsException {
		getShard().checkReady("join voice channel");
		DiscordUtils.checkPermissions(getClient().getOurUser(), this, EnumSet.of(Permissions.VOICE_CONNECT));

		IVoiceState voiceState = getClient().getOurUser().getVoiceStateForGuild(getGuild());
		boolean isMuted = voiceState != null && voiceState.isMuted();
		boolean isDeafened = voiceState != null && voiceState.isDeafened();
		((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE,
				new VoiceStateUpdateRequest(getGuild().getID(), getID(), isMuted, isDeafened));
	}

	@Override
	public void leave() {
		getShard().checkReady("leave voice channel");
		if (!isConnected()) return;

		IVoiceState voiceState = getClient().getOurUser().getVoiceStateForGuild(getGuild());
		boolean isMuted = voiceState != null && voiceState.isMuted();
		boolean isDeafened = voiceState != null && voiceState.isDeafened();

		((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE,
				new VoiceStateUpdateRequest(getGuild().getID(), null, isMuted, isDeafened));

		DiscordVoiceWS vWS = ((ShardImpl) getShard()).voiceWebSockets.get(getGuild());
		if (vWS != null) {
			vWS.disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
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
	public IMessage sendFile(File file) throws FileNotFoundException, DiscordException, RateLimitException {
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
	public void changeTopic(String topic) throws DiscordException, RateLimitException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void edit(String name, int position, String topic) throws DiscordException, RateLimitException, MissingPermissionsException {
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
	public IWebhook createWebhook(String name) throws DiscordException, RateLimitException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) throws DiscordException, RateLimitException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) throws DiscordException, RateLimitException, MissingPermissionsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadWebhooks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVoiceChannel copy() {
		return new VoiceChannel(client, name, id, guild, topic, position, userLimit, bitrate, roleOverrides, userOverrides);
	}

	@Override
	public List<IUser> getUsersHere() {
		return getConnectedUsers();
	}

	@Override
	public List<IUser> getConnectedUsers() {
		return guild.getUsers().stream().filter(u -> u.getVoiceStateForGuild(guild).getChannel().equals(this)).collect(Collectors.toList());
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
