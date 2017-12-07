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

import sx.blah.discord.api.internal.*;
import sx.blah.discord.api.internal.json.requests.VoiceChannelEditRequest;
import sx.blah.discord.api.internal.json.requests.voice.VoiceStateUpdateRequest;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.cache.Cache;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link IVoiceChannel}.
 */
public class VoiceChannel extends Channel implements IVoiceChannel {

	/**
	 * The maximum number of users allowed in the voice channel at once.
	 */
	protected volatile int userLimit = 0;
	/**
	 * The bitrate of the voice channel.
	 */
	protected volatile int bitrate = 0;

	public VoiceChannel(DiscordClientImpl client, String name, long id, IGuild guild, String topic, int position, boolean isNSFW,
						int userLimit, int bitrate, long categoryID,
						Cache<sx.blah.discord.handle.obj.PermissionOverride> roleOverrides,
						Cache<sx.blah.discord.handle.obj.PermissionOverride> userOverrides) {
		super(client, name, id, guild, topic, position, isNSFW, categoryID, roleOverrides, userOverrides);
		this.userLimit = userLimit;
		this.bitrate = bitrate;
	}

	@Override
	public int getUserLimit() {
		return userLimit;
	}

	@Override
	public int getBitrate() { return bitrate; }

	@Override
	public int getPosition() {
		return getGuild().getVoiceChannels().indexOf(this);
	}

	/**
	 * Sets the CACHED user limit of the voice channel.
	 *
	 * @param limit The user limit.
	 */
	public void setUserLimit(int limit) {
		this.userLimit = limit;
	}

	/**
	 * Sets the CACHED bitrate of the voice channel.
	 *
	 * @param bitrate The bitrate.
	 */
	public void setBitrate(int bitrate) { this.bitrate = bitrate; }

	@Override
	public void edit(String name, int position, int bitrate, int userLimit) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS);

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
	public void changeName(String name) {
		edit(name, getPosition(), getBitrate(), getUserLimit());
	}

	@Override
	public void changePosition(int position) {
		edit(getName(), position, getBitrate(), getUserLimit());
	}

	@Override
	public void changeBitrate(int bitrate) {
		edit(getName(), getPosition(), bitrate, getUserLimit());
	}

	@Override
	public void changeUserLimit(int limit) {
		edit(getName(), getPosition(), getBitrate(), limit);
	}

	@Override
	public void join() {
		getShard().checkReady("join voice channel");

		if (isConnected()) return;
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.VOICE_CONNECT);

		IVoiceState voiceState = getClient().getOurUser().getVoiceStateForGuild(getGuild());
		boolean isSelfMuted = voiceState != null && voiceState.isSelfMuted();
		boolean isSelfDeafened = voiceState != null && voiceState.isSelfDeafened();

		((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE,
				new VoiceStateUpdateRequest(getGuild().getStringID(), getStringID(), isSelfMuted, isSelfDeafened));

		((Guild) guild).connectingVoiceChannelID = getLongID();
	}

	@Override
	public void leave() {
		getShard().checkReady("leave voice channel");
		if (!isConnected()) return;

		VoiceState voiceState = (VoiceState) getClient().getOurUser().getVoiceStateForGuild(getGuild());
		boolean isSelfMuted = voiceState.isSelfMuted();
		boolean isSelfDeafened = voiceState.isSelfDeafened();

		((ShardImpl) getShard()).ws.send(GatewayOps.VOICE_STATE_UPDATE,
				new VoiceStateUpdateRequest(getGuild().getStringID(), null, isSelfMuted, isSelfDeafened));

		DiscordVoiceWS vWS = ((ShardImpl) getShard()).voiceWebSockets.get(getGuild().getLongID());
		if (vWS != null) {
			vWS.disconnect(VoiceDisconnectedEvent.Reason.LEFT_CHANNEL);
		}

		voiceState.setChannel(null);
	}

	@Override
	public boolean isConnected() {
		return client.getConnectedVoiceChannels().contains(this);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(Instant startDate, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryTo(Instant endDate, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID, int maxCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistory(int messageCount) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryFrom(Instant startDate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryTo(Instant endDate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageHistory getFullMessageHistory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IMessage> bulkDelete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IMessage> bulkDelete(List<IMessage> messages) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxInternalCacheCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getInternalCacheCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage getMessageByID(long messageID) {
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
	public IMessage sendFile(File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(String content, File file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) {
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
	public void changeTopic(String topic) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void edit(String name, int position, String topic) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IMessage> getPinnedMessages() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IWebhook> getWebhooks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook getWebhookByID(long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadWebhooks() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVoiceChannel copy() {
		return new VoiceChannel(client, name, id, guild, topic, position, isNSFW, userLimit, bitrate, categoryID, roleOverrides.copy(), userOverrides.copy());
	}

	@Override
	public List<IUser> getUsersHere() {
		return getConnectedUsers();
	}

	@Override
	public List<IUser> getConnectedUsers() {
		return guild.getUsers().stream().filter(u -> this.equals(u.getVoiceStateForGuild(guild).getChannel())).collect(Collectors.toList());
	}

	@Override
	public String toString(){
		return getName();
	}

	@Override
	public boolean isDeleted() {
		return getGuild().getVoiceChannelByID(getLongID()) != this;
	}
}
