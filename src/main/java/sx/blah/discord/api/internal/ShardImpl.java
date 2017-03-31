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
import sx.blah.discord.api.internal.json.objects.PrivateChannelObject;
import sx.blah.discord.api.internal.json.objects.UserObject;
import sx.blah.discord.api.internal.json.requests.PresenceUpdateRequest;
import sx.blah.discord.api.internal.json.requests.PrivateChannelCreateRequest;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.obj.PresenceImpl;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ShardImpl implements IShard {

	public volatile DiscordWS ws;

	private final String gateway;
	private final int[] info;

	private final DiscordClientImpl client;
	final List<IGuild> guildList = new CopyOnWriteArrayList<>();
	final List<IPrivateChannel> privateChannels = new CopyOnWriteArrayList<>();
	public final Map<String, DiscordVoiceWS> voiceWebSockets = new ConcurrentHashMap<>();

	ShardImpl(IDiscordClient client, String gateway, int[] info) {
		this.client = (DiscordClientImpl) client;
		this.gateway = gateway;
		this.info = info;
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
		this.ws = new DiscordWS(this, gateway, client.maxMissedPings);
		this.ws.connect();
	}

	@Override
	public void logout() {
		checkLoggedIn("logout");

		Discord4J.LOGGER.info(LogMarkers.API, "Shard {} logging out.", getInfo()[0]);
		getConnectedVoiceChannels().forEach(channel -> {
			RequestBuffer.RequestFuture<IVoiceChannel> request = RequestBuffer.request(() -> {
				channel.leave();
				return channel;
			});
			request.get();
		});
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
	public void changePlayingText(String playingText) {
		updatePresence(getClient().getOurUser().getPresence().getStatus(), playingText,
				getClient().getOurUser().getPresence().getStreamingUrl().orElse(null));
	}

	@Override
	public void online(String playingText) {
		updatePresence(StatusType.ONLINE, playingText);
	}

	@Override
	public void online() {
		online(getClient().getOurUser().getPresence().getPlayingText().orElse(null));
	}

	@Override
	public void idle(String playingText) {
		updatePresence(StatusType.IDLE, playingText);
	}

	@Override
	public void idle() {
		idle(getClient().getOurUser().getPresence().getPlayingText().orElse(null));
	}

	@Override
	public void streaming(String playingText, String streamingUrl) {
		updatePresence(StatusType.STREAMING, playingText, streamingUrl);
	}

	@Override
	@Deprecated
	public void changeStatus(Status status) {
		// old functionality just in case
		if (status.getType() == Status.StatusType.STREAM) {
			streaming(status.getStatusMessage(), status.getUrl().orElse(null));
		} else {
			changePlayingText(status.getStatusMessage());
		}
	}

	@Override
	@Deprecated
	public void changePresence(boolean isIdle) {
		// old functionality just in case
		if (isIdle)
			idle();
		else
			online();
	}

	private void updatePresence(StatusType status, String playing) {
		updatePresence(status, playing, null);
	}

	private void updatePresence(StatusType status, String playing, String streamUrl) {
		final boolean isIdle = status == StatusType.IDLE; // temporary until v6
		IUser ourUser = getClient().getOurUser();

		IPresence oldPresence = ourUser.getPresence();
		IPresence newPresence = new PresenceImpl(Optional.ofNullable(playing), Optional.ofNullable(streamUrl), status);

		if (!newPresence.equals(oldPresence)) {
			((User) ourUser).setPresence(newPresence);
			getClient().getDispatcher().dispatch(new PresenceUpdateEvent(ourUser, oldPresence, newPresence));
		}

		ws.send(GatewayOps.STATUS_UPDATE,
				new PresenceUpdateRequest(isIdle ? System.currentTimeMillis() : null, ourUser.getPresence()));
	}

	@Override
	public List<IChannel> getChannels(boolean priv) {
		List<IChannel> channels = guildList.stream()
				.map(IGuild::getChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		if (priv)
			channels.addAll(privateChannels);
		return channels;
	}

	@Override
	public List<IChannel> getChannels() {
		return getChannels(false);
	}

	@Override
	public IChannel getChannelByID(String id) {
		return getChannels(true).stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return guildList.stream()
				.map(IGuild::getVoiceChannels)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getConnectedVoiceChannels() {
		return getClient().getConnectedVoiceChannels().stream().filter(vc -> vc.getShard().equals(this)).collect(Collectors.toList());
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(String id) {
		return getVoiceChannels().stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IGuild> getGuilds() {
		return guildList;
	}

	@Override
	public IGuild getGuildByID(String guildID) {
		return guildList.stream()
				.filter(g -> g.getID().equalsIgnoreCase(guildID))
				.findAny().orElse(null);
	}

	@Override
	public List<IUser> getUsers() {
		return guildList.stream()
				.map(IGuild::getUsers)
				.flatMap(List::stream)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public IUser getUserByID(String userID) {
		IGuild guild = guildList.stream()
				.filter(g -> g.getUserByID(userID) != null)
				.findFirst()
				.orElse(null);

		IUser user = guild != null ? guild.getUserByID(userID) : null;
		IUser ourUser = getClient().getOurUser();

		user = ourUser != null && ourUser.getID().equals(userID) ? ourUser : user; // List of users doesn't include the bot user. Check if the id is that of the bot.

		if (user == null && isReady() && isLoggedIn())
			user = DiscordUtils.getUserFromJSON(null, client.REQUESTS.GET.makeRequest(DiscordEndpoints.USERS + userID, UserObject.class)); //This user isn't present in any shard so give it a null shard

		return user;
	}

	@Override
	public List<IRole> getRoles() {
		return guildList.stream()
				.map(IGuild::getRoles)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public IRole getRoleByID(String roleID) {
		return getRoles().stream()
				.filter(r -> r.getID().equalsIgnoreCase(roleID))
				.findAny().orElse(null);
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
	public IMessage getMessageByID(String messageID) {
		for (IGuild guild : guildList) {
			IMessage message = guild.getMessageByID(messageID);
			if (message != null)
				return message;
		}

		for (IPrivateChannel privateChannel : privateChannels) {
			IMessage message = privateChannel.getMessageByID(messageID);
			if (message != null)
				return message;
		}

		return null;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel(IUser user) {
		checkReady("get PM channel");

		if (user.equals(getClient().getOurUser()))
			throw new DiscordException("Cannot PM yourself!");

		Optional<IPrivateChannel> opt = privateChannels.stream()
				.filter(c -> c.getRecipient().getID().equalsIgnoreCase(user.getID()))
				.findAny();
		if (opt.isPresent())
			return opt.get();

		PrivateChannelObject pmChannel = client.REQUESTS.POST.makeRequest(
				DiscordEndpoints.USERS+getClient().getOurUser().getID()+"/channels",
				new PrivateChannelCreateRequest(user.getID()),
				PrivateChannelObject.class);
		IPrivateChannel channel = DiscordUtils.getPrivateChannelFromJSON(this, pmChannel);
		privateChannels.add(channel);
		return channel;
	}
}
