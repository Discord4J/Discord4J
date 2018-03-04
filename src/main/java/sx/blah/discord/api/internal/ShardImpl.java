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

package sx.blah.discord.api.internal;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.json.objects.ChannelObject;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.requests.PrivateChannelCreateRequest;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.obj.Presence;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.cache.Cache;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link IShard}.
 */
public class ShardImpl implements IShard {

	/**
	 * The gateway connection for this shard.
	 */
	public volatile DiscordWS ws;

	/**
	 * The gateway endpoint obtained for this shard.
	 */
	private final String gateway;
	/**
	 * The shard info.
	 * @see <a href=https://discordapp.com/developers/docs/topics/gateway#sharding>Sharding</a>
	 */
	private final int[] info;

	/**
	 * The client that manages this shard.
	 */
	private final DiscordClientImpl client;
	/**
	 * All of the guilds this shard manages.
	 */
	final Cache<IGuild> guildCache;
	/**
	 * The private channels on this shard. Only populated if this is shard 0.
	 */
	final Cache<IPrivateChannel> privateChannels;
	/**
	 * The voice gateways for guilds on this shard.
	 */
	public final Cache<DiscordVoiceWS> voiceWebSockets;

	private final PresenceUpdateRequest identifyPresence;

	ShardImpl(IDiscordClient client, String gateway, int[] info, PresenceUpdateRequest identifyPresence) {
		this.client = (DiscordClientImpl) client;
		this.gateway = gateway;
		this.info = info;
		this.identifyPresence = identifyPresence;
		this.guildCache = new Cache<>((DiscordClientImpl) client, IGuild.class);
		this.privateChannels = new Cache<>((DiscordClientImpl) client, IPrivateChannel.class);
		this.voiceWebSockets = new Cache<>((DiscordClientImpl) client, DiscordVoiceWS.class);
	}

	@Override
	public IDiscordClient getClient() {
		return this.client;
	}

	@Override
	public int[] getInfo() {
		return this.info;
	}

	@Override
	public void login() {
		Discord4J.LOGGER.trace(LogMarkers.API, "Shard logging in.");
		this.ws = new DiscordWS(this, gateway, client.maxMissedPings, identifyPresence);
		this.ws.connect();
	}

	@Override
	public void logout() {
		checkLoggedIn("logout");

		Discord4J.LOGGER.info(LogMarkers.API, "Shard {} logging out.", getInfo()[0]);
		getConnectedVoiceChannels().forEach(channel ->
			RequestBuffer.request(() -> {
				channel.leave();
				return channel;
			}).get()
		);
		getClient().getDispatcher().dispatch(new DisconnectedEvent(DisconnectedEvent.Reason.LOGGED_OUT, this));
		ws.shutdown();
	}

	@Override
	public boolean isReady() {
		return ws != null && ws.isReady;
	}

	@Override
	public boolean isLoggedIn() {
		return ws != null && ws.hasReceivedReady;
	}

	@Override
	public long getResponseTime() {
		return ws.heartbeatHandler.getAckResponseTime();
	}

	@Override
	public void changePresence(StatusType status, ActivityType activity, String text) {
		if (activity == ActivityType.STREAMING) throw new IllegalArgumentException("Invalid ActivityType");
		updatePresence(status, activity, text, null);
	}

	@Override
	public void changePresence(StatusType status) {
		updatePresence(status, null, null, null);
	}

	@Override
	public void changeStreamingPresence(StatusType status, String text, String streamUrl) {
		updatePresence(status, ActivityType.STREAMING, text, streamUrl);
	}

	private void updatePresence(StatusType status, ActivityType type, String text, String streamUrl) {
		checkLoggedIn("update presence");

		if (streamUrl != null) {
			if (!DiscordUtils.STREAM_URL_PATTERN.matcher(streamUrl).matches()) {
				throw new IllegalArgumentException("Stream URL must be a twitch.tv url.");
			}
		}

		IUser ourUser = getClient().getOurUser();
		IPresence oldPresence = ourUser.getPresence();
		IPresence newPresence = new Presence(text, streamUrl, status, type);

		if (!newPresence.equals(oldPresence)) {
			((User) ourUser).setPresence(newPresence);
			getClient().getDispatcher().dispatch(new PresenceUpdateEvent(ourUser, oldPresence, newPresence));
		}

		ws.send(GatewayOps.STATUS_UPDATE, new PresenceUpdateRequest(status, type, text, streamUrl));
	}

	@Override
	public List<IChannel> getChannels(boolean includePrivate) {
		List<IChannel> channels = guildCache.stream()
				.flatMap(g -> g.getChannels().stream())
				.collect(Collectors.toList());

		if (includePrivate)
			channels.addAll(privateChannels.values());

		return channels;
	}

	@Override
	public List<IChannel> getChannels() {
		return getChannels(false);
	}

	@Override
	public IChannel getChannelByID(long id) {
		IChannel channel = guildCache.findResult((guildID, guild) -> guild.getChannelByID(id));

		return channel == null ? privateChannels.get(id) : channel;
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return guildCache.stream()
				.map(IGuild::getVoiceChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return getClient().getConnectedVoiceChannels().stream().filter(vc -> vc.getShard().equals(this)).collect(Collectors.toList());
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(long id) {
		return guildCache.findResult((guildID, guild) -> guild.getVoiceChannelByID(id));
	}

	@Override
	public List<IGuild> getGuilds() {
		return new LinkedList<>(guildCache.values());
	}

	@Override
	public IGuild getGuildByID(long guildID) {
		return guildCache.get(guildID);
	}

	@Override
	public List<IUser> getUsers() {
		List<IUser> guildUserList = guildCache.stream()
				.map(IGuild::getUsers)
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());

		if (client.getOurUser() != null && !guildUserList.contains(client.getOurUser()))
			guildUserList.add(client.getOurUser());

		return guildUserList;
	}

	@Override
	public IUser getUserByID(long userID) {
		IUser ourUser = getClient().getOurUser();
		if (ourUser != null && userID == ourUser.getLongID()) return ourUser;
		return guildCache.findResult((guildID, guild) -> guild.getUserByID(userID));
	}

	@Override
	public IUser fetchUser(long id) {
		IUser cached = getUserByID(id);
		return cached == null ? DiscordUtils.getUserFromJSON(this, client.REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + Long.toUnsignedString(id), UserObject.class)) : cached;
	}

	@Override
	public List<IRole> getRoles() {
		return guildCache.stream()
				.map(IGuild::getRoles)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IRole getRoleByID(long roleID) {
		return guildCache.findResult((guildID, guild) -> guild.getRoleByID(roleID));
	}

	@Override
	public List<IMessage> getMessages(boolean includePrivate) {
		return getChannels(includePrivate).stream()
				.map(IChannel::getMessageHistory)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IMessage> getMessages() {
		return getMessages(false);
	}

	@Override
	public IMessage getMessageByID(long messageID) {
		IMessage message = guildCache.findResult((guildID, guild) -> guild.getMessageByID(messageID));
		if (message == null)
			message = privateChannels.findResult((channelID, channel) -> channel.getMessageByID(messageID));
		return message;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) {
		checkReady("get PM channel");

		if (user.equals(getClient().getOurUser()))
			throw new DiscordException("Cannot PM yourself!");

		IPrivateChannel channel = privateChannels.get(user.getLongID());
		if (channel != null)
			return channel;

		ChannelObject pmChannel = client.REQUESTS.POST.makeRequest(
				DiscordEndpoints.USERS+getClient().getOurUser().getStringID()+"/channels",
				new PrivateChannelCreateRequest(user.getStringID()),
				ChannelObject.class);
		channel = (IPrivateChannel) DiscordUtils.getChannelFromJSON(this, null, pmChannel);
		privateChannels.put(channel);
		return channel;
	}

	@Override
	public List<ICategory> getCategories() {
		return getGuilds().stream()
				.map(IGuild::getCategories)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public ICategory getCategoryByID(long categoryID) {
		return guildCache.findResult((guildId, guild) -> guild.getCategoryByID(categoryID));
	}
}
